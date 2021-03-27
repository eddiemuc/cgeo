package cgeo.geocaching.ui;

import cgeo.geocaching.CacheDetailActivity;
import cgeo.geocaching.R;
import cgeo.geocaching.databinding.CacheslistItemBinding;
import cgeo.geocaching.enumerations.CacheListType;
import cgeo.geocaching.filter.IFilter;
import cgeo.geocaching.filters.GeocacheFilterUtils;
import cgeo.geocaching.filters.IGeocacheFilter;
import cgeo.geocaching.list.AbstractList;
import cgeo.geocaching.location.Geopoint;
import cgeo.geocaching.models.Geocache;
import cgeo.geocaching.sensors.GeoData;
import cgeo.geocaching.sensors.Sensors;
import cgeo.geocaching.settings.Settings;
import cgeo.geocaching.sorting.CacheComparator;
import cgeo.geocaching.sorting.DistanceComparator;
import cgeo.geocaching.sorting.EventDateComparator;
import cgeo.geocaching.sorting.InverseComparator;
import cgeo.geocaching.sorting.SeriesNameComparator;
import cgeo.geocaching.sorting.VisitComparator;
import cgeo.geocaching.utils.AngleUtils;
import cgeo.geocaching.utils.CalendarUtils;
import cgeo.geocaching.utils.Formatter;
import cgeo.geocaching.utils.Log;
import cgeo.geocaching.utils.MapMarkerUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Paint;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.SectionIndexer;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

public class CacheListAdapter extends ArrayAdapter<Geocache> implements SectionIndexer {

    private LayoutInflater inflater = null;
    private static CacheComparator cacheComparator = null;
    private Geopoint coords;
    private float azimuth = 0;
    private long lastSort = 0L;
    private boolean selectMode = false;
    private IFilter currentFilter = null;
    private IGeocacheFilter currentGeocacheFilter = null;
    private List<Geocache> originalList = null;
    private final boolean isLiveList = Settings.isLiveList();

    private final Set<CompassMiniView> compasses = new LinkedHashSet<>();
    private final Set<DistanceView> distances = new LinkedHashSet<>();
    private final CacheListType cacheListType;
    private List<AbstractList> storedLists = null;
    private String currentListTitle = "";
    private final Resources res;
    /** Resulting list of caches */
    private final List<Geocache> list;
    private boolean eventsOnly;
    private boolean inverseSort = false;
    /**
     * {@code true} if the caches in this list are a complete series and should be sorted by name instead of distance
     */
    private boolean series = false;

    private static final int SWIPE_MIN_DISTANCE = 60;
    private static final int SWIPE_MAX_OFF_PATH = 100;
    /**
     * time in milliseconds after which the list may be resorted due to position updates
     */
    private static final int PAUSE_BETWEEN_LIST_SORT = 1000;

    private static final int[] RATING_BACKGROUND = new int[3];
    /**
     * automatically order cache series by name, if they all have a common suffix or prefix at least these many
     * characters
     */
    private static final int MIN_COMMON_CHARACTERS_SERIES = 4;
    static {
        if (Settings.isLightSkin()) {
            RATING_BACKGROUND[0] = R.drawable.favorite_background_red_light;
            RATING_BACKGROUND[1] = R.drawable.favorite_background_orange_light;
            RATING_BACKGROUND[2] = R.drawable.favorite_background_green_light;
        } else {
            RATING_BACKGROUND[0] = R.drawable.favorite_background_red_dark;
            RATING_BACKGROUND[1] = R.drawable.favorite_background_orange_dark;
            RATING_BACKGROUND[2] = R.drawable.favorite_background_green_dark;
        }
    }

    // variables for section indexer
    private HashMap<String, Integer> mapFirstPosition;
    private HashMap<String, Integer> mapSection;
    private String[] sections;

    /**
     * view holder for the cache list adapter
     *
     */
    public static class ViewHolder extends AbstractViewHolder {
        private CacheListType cacheListType;
        public Geocache cache = null;
        private final CacheslistItemBinding binding;

        public ViewHolder(final View view) {
            super(view);
            binding = CacheslistItemBinding.bind(view);
        }
    }

    public CacheListAdapter(final Activity activity, final List<Geocache> list, final CacheListType cacheListType) {
        super(activity, 0, list);
        final GeoData currentGeo = Sensors.getInstance().currentGeo();
        coords = currentGeo.getCoords();
        this.res = activity.getResources();
        this.list = list;
        this.cacheListType = cacheListType;
        checkSpecialSortOrder();
        buildFastScrollIndex();
    }

    public void setStoredLists(final List<AbstractList> storedLists) {
        this.storedLists = storedLists;
    }

    public void setCurrentListTitle(final String currentListTitle) {
        this.currentListTitle = currentListTitle;
    }

    /**
     * change the sort order
     *
     */
    public void setComparator(final CacheComparator comparator) {
        cacheComparator = comparator;
        forceSort();
    }

    public void resetInverseSort() {
        inverseSort = false;
    }

    public void toggleInverseSort() {
        inverseSort = !inverseSort;
    }

    /**
     * Set the inverseSort order.
     *
     * @param inverseSort
     *          True if sort is inverted
     */
    public void setInverseSort(final boolean inverseSort) {
        this.inverseSort = inverseSort;
    }

    /**
     * Obtain the current inverseSort order.
     *
     * @return
     *          True if sort is inverted
     */
    public boolean getInverseSort() {
        return inverseSort;
    }

    public CacheComparator getCacheComparator() {
        if (isHistory()) {
            return VisitComparator.singleton;
        }
        if (cacheComparator == null && eventsOnly) {
            return EventDateComparator.INSTANCE;
        }
        if (cacheComparator == null && series) {
            return SeriesNameComparator.INSTANCE;
        }
        if (cacheComparator == null) {
            return DistanceComparator.INSTANCE;
        }
        return cacheComparator;
    }

    private boolean isHistory() {
        return cacheListType == CacheListType.HISTORY;
    }

    public Geocache findCacheByGeocode(final String geocode) {
        for (int i = 0; i < getCount(); i++) {
            if (getItem(i).getGeocode().equalsIgnoreCase(geocode)) {
                return getItem(i);
            }
        }

        return null;
    }
    /**
     * Called when a new page of caches was loaded.
     */
    public void reFilter() {
        if (currentFilter != null || currentGeocacheFilter != null) {
            // Back up the list again
            originalList = new ArrayList<>(list);

            performFiltering();
        }
    }

    /**
     * Called after a user action on the filter menu.
     */
    public void setFilter(final IFilter filter, final String advancedFilter) {

        IGeocacheFilter gcFilter = null;
        if (advancedFilter != null) {
            try {
                gcFilter = GeocacheFilterUtils.createFilter(advancedFilter);
            } catch (ParseException pe) {
                Log.w("Could not parse filter: " + advancedFilter, pe);
            }
        }

        // Backup current caches list if it isn't backed up yet
        if (originalList == null) {
            originalList = new ArrayList<>(list);
        }

        // If there is already a filter in place, this is a request to change or clear the filter, so we have to
        // replace the original cache list
        if (currentFilter != null || currentGeocacheFilter != null) {
            list.clear();
            list.addAll(originalList);
        }

        currentFilter = filter;
        currentGeocacheFilter = gcFilter;

        performFiltering();

        notifyDataSetChanged();
    }

    private void performFiltering() {
        // Do the filtering or clear it
        if (currentFilter != null) {
            currentFilter.filter(list);
        }
        if (currentGeocacheFilter != null) {
            final List<Geocache> itemsToKeep = new ArrayList<>();
            for (final Geocache item : list) {
                final Boolean fr = currentGeocacheFilter.filter(item);
                if (fr == null || fr) {
                    itemsToKeep.add(item);
                }
            }

            list.clear();
            //note that since both "list" and "itemsToKeep" are ArrayLists, the addAll-operation is very fast (two arraycopies of the references)
            list.addAll(itemsToKeep);
        }
    }

    public boolean isFiltered() {
        return currentFilter != null || currentGeocacheFilter != null;
    }

    public String getFilterName() {
        return (currentFilter == null ? "-" : currentFilter.getName()) + "|" +
            (currentGeocacheFilter == null ? "-" : GeocacheFilterUtils.getFilterConfig(currentGeocacheFilter));
    }

    public int getCheckedCount() {
        int checked = 0;
        for (final Geocache cache : list) {
            if (cache.isStatusChecked()) {
                checked++;
            }
        }
        return checked;
    }

    public void setSelectMode(final boolean selectMode) {
        this.selectMode = selectMode;

        if (!selectMode) {
            for (final Geocache cache : list) {
                cache.setStatusChecked(false);
            }
        }
        notifyDataSetChanged();
    }

    public boolean isSelectMode() {
        return selectMode;
    }

    public void switchSelectMode() {
        setSelectMode(!isSelectMode());
    }

    public void invertSelection() {
        for (final Geocache cache : list) {
            cache.setStatusChecked(!cache.isStatusChecked());
        }
        notifyDataSetChanged();
    }

    public void forceSort() {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        if (isSortedByDistance()) {
            lastSort = 0;
            updateSortByDistance();
        } else {
            Collections.sort(list, getPotentialInversion(getCacheComparator()));
        }

        notifyDataSetChanged();
    }

    public void setActualCoordinates(@NonNull final Geopoint coords) {
        this.coords = coords;
        updateSortByDistance();

        for (final DistanceView distance : distances) {
            distance.update(coords);
        }
        for (final CompassMiniView compass : compasses) {
            compass.updateCurrentCoords(coords);
        }
    }

    private void updateSortByDistance() {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        if ((System.currentTimeMillis() - lastSort) <= PAUSE_BETWEEN_LIST_SORT) {
            return;
        }
        if (!isSortedByDistance()) {
            return;
        }
        if (coords == null) {
            return;
        }
        final List<Geocache> oldList = new ArrayList<>(list);
        Collections.sort(list, getPotentialInversion(new DistanceComparator(coords, list)));

        // avoid an update if the list has not changed due to location update
        if (list.equals(oldList)) {
            return;
        }
        notifyDataSetChanged();
        lastSort = System.currentTimeMillis();
    }

    private Comparator<? super Geocache> getPotentialInversion(final CacheComparator comparator) {
        if (inverseSort) {
            return new InverseComparator(comparator);
        }
        return comparator;
    }

    private boolean isSortedByDistance() {
        final CacheComparator comparator = getCacheComparator();
        return comparator == null || comparator instanceof DistanceComparator;
    }

    private boolean isSortedByEvent() {
        final CacheComparator comparator = getCacheComparator();
        return comparator == null || comparator instanceof EventDateComparator;
    }

    private boolean isSortedBySeries() {
        final CacheComparator comparator = getCacheComparator();
        return comparator == null || comparator instanceof SeriesNameComparator;
    }

    public void setActualHeading(final float direction) {
        if (Math.abs(AngleUtils.difference(azimuth, direction)) < 5) {
            return;
        }

        azimuth = direction;
        for (final CompassMiniView compass : compasses) {
            compass.updateAzimuth(azimuth);
        }
    }

    public static void updateViewHolder(final ViewHolder holder, final Geocache cache, final Resources res) {
        if (cache.isFound() && cache.hasLogOffline()) {
            holder.binding.logStatusMark.setImageResource(R.drawable.mark_green_orange);
        } else if (cache.isFound()) {
            holder.binding.logStatusMark.setImageResource(R.drawable.mark_green_more);
        } else if (cache.hasLogOffline()) {
            holder.binding.logStatusMark.setImageResource(R.drawable.mark_orange);
        } else if (cache.isDNF()) {
            holder.binding.logStatusMark.setImageResource(R.drawable.mark_red);
        } else {
            holder.binding.logStatusMark.setImageResource(R.drawable.mark_transparent);
        }
        holder.binding.textIcon.setImageDrawable(MapMarkerUtils.getCacheMarker(res, cache, holder.cacheListType).getDrawable());
    }

    @Override
    public View getView(final int position, final View rowView, @NonNull final ViewGroup parent) {
        if (inflater == null) {
            inflater = LayoutInflater.from(getContext());
        }

        if (position > getCount()) {
            Log.w("CacheListAdapter.getView: Attempt to access missing item #" + position);
            return null;
        }

        final Geocache cache = getItem(position);

        View v = rowView;

        final ViewHolder holder;
        if (v == null) {
            v = inflater.inflate(R.layout.cacheslist_item, parent, false);
            holder = new ViewHolder(v);
        } else {
            holder = (ViewHolder) v.getTag();
        }
        holder.cache = cache;

        final boolean lightSkin = Settings.isLightSkin();

        final TouchListener touchListener = new TouchListener(cache, this);
        v.setOnClickListener(touchListener);
        v.setOnLongClickListener(touchListener);
        v.setOnTouchListener(touchListener);

        holder.binding.checkbox.setVisibility(selectMode ? View.VISIBLE : View.GONE);
        holder.binding.checkbox.setChecked(cache.isStatusChecked());
        holder.binding.checkbox.setOnClickListener(new SelectionCheckBoxListener(cache));

        distances.add(holder.binding.distance);
        holder.binding.distance.setContent(cache.getCoords());
        compasses.add(holder.binding.direction);
        holder.binding.direction.setTargetCoords(cache.getCoords());

        if (cache.isDisabled() || cache.isArchived() || CalendarUtils.isPastEvent(cache)) { // strike
            holder.binding.text.setPaintFlags(holder.binding.text.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.binding.text.setPaintFlags(holder.binding.text.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        }
        if (cache.isArchived()) { // red color
            holder.binding.text.setTextColor(ContextCompat.getColor(getContext(), R.color.archived_cache_color));
        } else {
            holder.binding.text.setTextColor(ContextCompat.getColor(getContext(), lightSkin ? R.color.text_light : R.color.text_dark));
        }

        holder.binding.text.setText(cache.getName(), TextView.BufferType.NORMAL);
        holder.cacheListType = cacheListType;
        updateViewHolder(holder, cache, res);

        final int inventorySize = cache.getInventoryItems();
        if (inventorySize > 0) {
            holder.binding.inventory.setText(String.format(Locale.getDefault(), "%d", inventorySize));
            holder.binding.inventory.setVisibility(View.VISIBLE);
        } else {
            holder.binding.inventory.setVisibility(View.GONE);
        }

        if (cache.getDistance() != null) {
            holder.binding.distance.setDistance(cache.getDistance());
        }

        if (cache.getCoords() != null && coords != null) {
            holder.binding.distance.update(coords);
        }

        // only show the direction if this is enabled in the settings
        if (isLiveList) {
            if (cache.getCoords() != null) {
                holder.binding.direction.setVisibility(View.VISIBLE);
                holder.binding.dirimg.setVisibility(View.GONE);
                holder.binding.direction.updateAzimuth(azimuth);
                if (coords != null) {
                    holder.binding.direction.updateCurrentCoords(coords);
                }
            } else if (cache.getDirection() != null) {
                holder.binding.direction.setVisibility(View.VISIBLE);
                holder.binding.dirimg.setVisibility(View.GONE);
                holder.binding.direction.updateAzimuth(azimuth);
                holder.binding.direction.updateHeading(cache.getDirection());
            } else if (StringUtils.isNotBlank(cache.getDirectionImg())) {
                holder.binding.dirimg.setVisibility(View.INVISIBLE);
                holder.binding.direction.setVisibility(View.GONE);
                DirectionImage.fetchDrawable(cache.getDirectionImg()).observeOn(AndroidSchedulers.mainThread()).subscribe(bitmapDrawable -> {
                    if (cache == holder.cache) {
                        holder.binding.dirimg.setImageDrawable(bitmapDrawable);
                        holder.binding.dirimg.setVisibility(View.VISIBLE);
                    }
                });
            } else {
                holder.binding.dirimg.setVisibility(View.GONE);
                holder.binding.direction.setVisibility(View.GONE);
            }
        }

        final int favCount = cache.getFavoritePoints();
        holder.binding.favorite.setText(Formatter.formatFavCount(favCount));

        int favoriteBack;
        // set default background, neither vote nor rating may be available
        if (lightSkin) {
            favoriteBack = R.drawable.favorite_background_light;
        } else {
            favoriteBack = R.drawable.favorite_background_dark;
        }
        final float rating = cache.getRating();
        if (rating >= 3.5) {
            favoriteBack = RATING_BACKGROUND[2];
        } else if (rating >= 2.1) {
            favoriteBack = RATING_BACKGROUND[1];
        } else if (rating > 0.0) {
            favoriteBack = RATING_BACKGROUND[0];
        }
        holder.binding.favorite.setBackgroundResource(favoriteBack);

        if (isHistory() && cache.getVisitedDate() > 0) {
            holder.binding.info.setText(Formatter.formatCacheInfoHistory(cache));
        } else {
            holder.binding.info.setText(Formatter.formatCacheInfoLong(cache));
        }

        // optionally show list infos
        if (null != storedLists) {
            final List<String> infos = new ArrayList<>();
            final Set<Integer> lists = cache.getLists();
            for (final AbstractList temp : storedLists) {
                if (lists.contains(temp.id) && !temp.title.equals(currentListTitle)) {
                    infos.add(temp.title);
                }
            }
            if (!infos.isEmpty()) {
                holder.binding.info.append("\n" + StringUtils.join(infos, Formatter.SEPARATOR));
            }
        }

        return v;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        distances.clear();
        compasses.clear();
        buildFastScrollIndex();
    }

    private static class SelectionCheckBoxListener implements View.OnClickListener {

        private final Geocache cache;

        SelectionCheckBoxListener(final Geocache cache) {
            this.cache = cache;
        }

        @Override
        public void onClick(final View view) {
            final boolean checkNow = ((CheckBox) view).isChecked();
            cache.setStatusChecked(checkNow);
        }
    }

    private static class TouchListener implements View.OnClickListener, View.OnLongClickListener, View.OnTouchListener {

        private final Geocache cache;
        private final GestureDetector gestureDetector;
        @NonNull private final WeakReference<CacheListAdapter> adapterRef;

        TouchListener(final Geocache cache, @NonNull final CacheListAdapter adapter) {
            this.cache = cache;
            gestureDetector = new GestureDetector(adapter.getContext(), new FlingGesture(cache, adapter));
            adapterRef = new WeakReference<>(adapter);
        }

        // Tap on item
        @Override
        public void onClick(final View view) {
            final CacheListAdapter adapter = adapterRef.get();
            if (adapter == null) {
                return;
            }
            if (adapter.isSelectMode()) {
                cache.setStatusChecked(!cache.isStatusChecked());
                adapter.notifyDataSetChanged();
            } else {
                CacheDetailActivity.startActivity(adapter.getContext(), cache.getGeocode(), cache.getName());
            }
        }

        // Long tap on item
        @Override
        public boolean onLongClick(final View view) {
            view.showContextMenu();
            return true;
        }

        // Swipe on item
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(final View view, final MotionEvent event) {
            return gestureDetector.onTouchEvent(event);

        }
    }

    private static class FlingGesture extends GestureDetector.SimpleOnGestureListener {

        private final Geocache cache;
        @NonNull private final WeakReference<CacheListAdapter> adapterRef;

        FlingGesture(final Geocache cache, @NonNull final CacheListAdapter adapter) {
            this.cache = cache;
            adapterRef = new WeakReference<>(adapter);
        }

        @Override
        public boolean onFling(final MotionEvent e1, final MotionEvent e2, final float velocityX, final float velocityY) {
            try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) {
                    return false;
                }
                final CacheListAdapter adapter = adapterRef.get();
                if (adapter == null) {
                    return false;
                }

                // horizontal swipe
                if (Math.abs(velocityX) > Math.abs(velocityY)) {

                    // left to right swipe
                    if ((e2.getX() - e1.getX()) > SWIPE_MIN_DISTANCE) {
                        if (!adapter.selectMode) {
                            adapter.switchSelectMode();
                            cache.setStatusChecked(true);
                        }
                        return true;
                    }

                    // right to left swipe
                    if ((e1.getX() - e2.getX()) > SWIPE_MIN_DISTANCE) {
                        if (adapter.selectMode) {
                            adapter.switchSelectMode();
                        }
                        return true;
                    }
                }
            } catch (final Exception e) {
                Log.w("CacheListAdapter.FlingGesture.onFling", e);
            }

            return false;
        }
    }

    public List<Geocache> getFilteredList() {
        return list;
    }

    public List<Geocache> getCheckedCaches() {
        final List<Geocache> result = new ArrayList<>();
        for (final Geocache cache : list) {
            if (cache.isStatusChecked()) {
                result.add(cache);
            }
        }
        return result;
    }

    public List<Geocache> getCheckedOrAllCaches() {
        final List<Geocache> result = getCheckedCaches();
        if (!result.isEmpty()) {
            return result;
        }
        return new ArrayList<>(list);
    }

    public int getCheckedOrAllCount() {
        final int checked = getCheckedCount();
        if (checked > 0) {
            return checked;
        }
        return list.size();
    }

    public void checkSpecialSortOrder() {
        checkEvents();
        checkSeries();
        if (!eventsOnly && isSortedByEvent()) {
            setComparator(DistanceComparator.INSTANCE);
        }
        if (!series && isSortedBySeries()) {
            setComparator(DistanceComparator.INSTANCE);
        }
    }

    private void checkEvents() {
        eventsOnly = true;
        for (final Geocache cache : list) {
            if (!cache.isEventCache()) {
                eventsOnly = false;
                return;
            }
        }
    }

    /**
     * detect whether all caches in this list belong to a series with similar names
     */
    private void checkSeries() {
        series = false;
        if (list.size() < 3 || list.size() > 50) {
            return;
        }
        final ArrayList<String> names = new ArrayList<>();
        final ArrayList<String> reverseNames = new ArrayList<>();
        for (final Geocache cache : list) {
            final String name = cache.getName();
            names.add(name);
            reverseNames.add(StringUtils.reverse(name));
        }
        final String commonPrefix = StringUtils.getCommonPrefix(names.toArray(new String[names.size()]));
        if (StringUtils.length(commonPrefix) >= MIN_COMMON_CHARACTERS_SERIES) {
            series = true;
        } else {
            final String commonSuffix = StringUtils.getCommonPrefix(reverseNames.toArray(new String[reverseNames.size()]));
            if (StringUtils.length(commonSuffix) >= MIN_COMMON_CHARACTERS_SERIES) {
                series = true;
            }
        }
        if (series) {
            setComparator(new SeriesNameComparator());
        }
    }

    public boolean isEventsOnly() {
        return eventsOnly;
    }

    // methods for section indexer

    private void buildFastScrollIndex() {
        mapFirstPosition = new LinkedHashMap<>();
        final ArrayList<String> sectionList = new ArrayList<>();
        String lastComparable = null;
        for (int x = 0; x < list.size(); x++) {
            final String comparable = getComparable(x);
            if (!StringUtils.equals(lastComparable, comparable)) {
                mapFirstPosition.put(comparable, x);
                sectionList.add(comparable);
                lastComparable = comparable;
            }
        }
        sections = new String[sectionList.size()];
        sectionList.toArray(sections);
        mapSection = new LinkedHashMap<>();
        for (int x = 0; x < sections.length; x++) {
            mapSection.put(sections[x], x);
        }
    }

    public int getPositionForSection(final int section) {
        if (sections == null || sections.length == 0) {
            return 0;
        }
        final Integer position = mapFirstPosition.get(sections[Math.max(0, Math.min(section, sections.length - 1))]);
        return null == position ? 0 : position;
    }

    public int getSectionForPosition(final int position) {
        final Integer section = mapSection.get(getComparable(position));
        return null == section ? 0 : section;
    }

    public Object[] getSections() {
        return sections;
    }

    @NonNull
    private String getComparable(final int position) {
        try {
            return getCacheComparator().getSortableSection(list.get(position));
        } catch (NullPointerException e) {
            return " ";
        }
    }

}
