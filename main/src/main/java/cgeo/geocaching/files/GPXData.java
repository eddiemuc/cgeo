package cgeo.geocaching.files;

import cgeo.geocaching.connector.gc.GCUtils;
import cgeo.geocaching.connector.tc.TerraCachingLogType;
import cgeo.geocaching.connector.tc.TerraCachingType;
import cgeo.geocaching.enumerations.CacheAttribute;
import cgeo.geocaching.enumerations.CacheSize;
import cgeo.geocaching.location.Geopoint;
import cgeo.geocaching.log.LogEntry;
import cgeo.geocaching.log.LogType;
import cgeo.geocaching.models.Geocache;
import cgeo.geocaching.models.IGeoObject;
import cgeo.geocaching.models.SimpleGeoObject;
import cgeo.geocaching.utils.MatcherWrapper;
import cgeo.geocaching.utils.SynchronizedDateFormat;
import cgeo.geocaching.utils.XmlUtils;
import static cgeo.geocaching.files.GPXWaypointParser.WPT_GC_ARCHIVED;
import static cgeo.geocaching.files.GPXWaypointParser.WPT_GC_ATTRIBUTES;
import static cgeo.geocaching.files.GPXWaypointParser.WPT_GC_AVAILABLE;
import static cgeo.geocaching.files.GPXWaypointParser.WPT_GC_CONTAINER;
import static cgeo.geocaching.files.GPXWaypointParser.WPT_GC_COUNTRY;
import static cgeo.geocaching.files.GPXWaypointParser.WPT_GC_DIFFICULTY;
import static cgeo.geocaching.files.GPXWaypointParser.WPT_GC_ENCODED_HINTS;
import static cgeo.geocaching.files.GPXWaypointParser.WPT_GC_ID;
import static cgeo.geocaching.files.GPXWaypointParser.WPT_GC_INVENTORY;
import static cgeo.geocaching.files.GPXWaypointParser.WPT_GC_LOGS;
import static cgeo.geocaching.files.GPXWaypointParser.WPT_GC_LONG_DESCRIPTION;
import static cgeo.geocaching.files.GPXWaypointParser.WPT_GC_NAME;
import static cgeo.geocaching.files.GPXWaypointParser.WPT_GC_OWNER;
import static cgeo.geocaching.files.GPXWaypointParser.WPT_GC_PLACED_BY;
import static cgeo.geocaching.files.GPXWaypointParser.WPT_GC_SHORT_DESCRIPTION;
import static cgeo.geocaching.files.GPXWaypointParser.WPT_GC_STATE;
import static cgeo.geocaching.files.GPXWaypointParser.WPT_GC_TERRAIN;
import static cgeo.geocaching.files.GPXWaypointParser.WPT_GC_TYPE;
import static cgeo.geocaching.files.GPXWaypointParser.WPT_GPX_SUBTYPE;
import static cgeo.geocaching.files.GPXWaypointParser.WPT_GPX_TYPE;
import static cgeo.geocaching.files.GPXWaypointParser.WPT_GSAK_CHILD_BY_GSAK;
import static cgeo.geocaching.files.GPXWaypointParser.WPT_GSAK_CODE;
import static cgeo.geocaching.files.GPXWaypointParser.WPT_GSAK_DNF;
import static cgeo.geocaching.files.GPXWaypointParser.WPT_GSAK_DNF_DATE;
import static cgeo.geocaching.files.GPXWaypointParser.WPT_GSAK_FAVORITE_POINTS;
import static cgeo.geocaching.files.GPXWaypointParser.WPT_GSAK_GC_NOTE;
import static cgeo.geocaching.files.GPXWaypointParser.WPT_GSAK_IS_PREMIUM;
import static cgeo.geocaching.files.GPXWaypointParser.WPT_GSAK_LAT_BEFORE_CORRECT;
import static cgeo.geocaching.files.GPXWaypointParser.WPT_GSAK_LON_BEFORE_CORRECT;
import static cgeo.geocaching.files.GPXWaypointParser.WPT_GSAK_PARENT;
import static cgeo.geocaching.files.GPXWaypointParser.WPT_GSAK_USER_DATA_1;
import static cgeo.geocaching.files.GPXWaypointParser.WPT_GSAK_USER_DATA_2;
import static cgeo.geocaching.files.GPXWaypointParser.WPT_GSAK_USER_DATA_3;
import static cgeo.geocaching.files.GPXWaypointParser.WPT_GSAK_USER_DATA_4;
import static cgeo.geocaching.files.GPXWaypointParser.WPT_GSAK_USER_FOUND;
import static cgeo.geocaching.files.GPXWaypointParser.WPT_GSAK_WATCH;
import static cgeo.geocaching.files.GPXWaypointParser.WPT_OC_OTHER_CODE;
import static cgeo.geocaching.files.GPXWaypointParser.WPT_OC_REQUIRES_PASSWORD;
import static cgeo.geocaching.files.GPXWaypointParser.WPT_OC_SIZE;
import static cgeo.geocaching.files.GPXWaypointParser.WPT_TERRA_COUNTRY;
import static cgeo.geocaching.files.GPXWaypointParser.WPT_TERRA_DESCRIPTION;
import static cgeo.geocaching.files.GPXWaypointParser.WPT_TERRA_HINT;
import static cgeo.geocaching.files.GPXWaypointParser.WPT_TERRA_LOGS;
import static cgeo.geocaching.files.GPXWaypointParser.WPT_TERRA_NAME;
import static cgeo.geocaching.files.GPXWaypointParser.WPT_TERRA_OWNER;
import static cgeo.geocaching.files.GPXWaypointParser.WPT_TERRA_SIZE;
import static cgeo.geocaching.files.GPXWaypointParser.WPT_TERRA_STATE;
import static cgeo.geocaching.files.GPXWaypointParser.WPT_TERRA_STYLE;

import android.sax.Element;
import android.sax.RootElement;

import androidx.annotation.NonNull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

/** Parses series of points in GPX XML */
public class GPXData {

    //constants for date parsing
    private static final SynchronizedDateFormat DATE_FORMAT_SIMPLE = new SynchronizedDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US); // 2010-04-20T07:00:00
    private static final SynchronizedDateFormat DATE_FORMAT_SIMPLE_NOTIME = new SynchronizedDateFormat("yyyy-MM-dd", Locale.US); // 2010-04-20
    private static final SynchronizedDateFormat DATE_FORMAT_Z = new SynchronizedDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US); // 2010-04-20T07:00:00Z
    private static final SynchronizedDateFormat DATE_FORMAT_TIMEZONE = new SynchronizedDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US); // 2010-04-20T01:01:03-04:00
    private static final Pattern DATE_PATTERN_MILLISECONDS = Pattern.compile("\\.\\d{3,7}");

    private String creator;
    private String url;
    private String urlName;

    private final GPXPointSeries wptSeries = new GPXPointSeries();
    private final List<GPXPointSeries> routeSeries = new ArrayList<>();
    private final List<GPXPointSeries> trackSeries = new ArrayList<>();
    private final Map<String, Geocache> geocaches = new HashMap<>();

    private final GPXWaypointParser wpParser = new GPXWaypointParser(this);

    public static class GPXPointSeries {

        private String name;
        private int index;
        private final List<IGeoObject> points = new ArrayList<>();

        public String getName() {
            return name;
        }

        public int getIndex() {
            return index;
        }

        public List<IGeoObject> getPoints() {
            return points;
        }
    }

    private static void registerPointSeries(@NonNull final GPXData gpxData, @NonNull final Element pointElement, final Supplier<GPXPointSeries> seriesSupplier) {

        final Map<String, Object> currentPointData = new HashMap<>();

        //parse points
        pointElement.setStartElementListener(attrs -> {
            currentPointData.clear();
            final Double lat = parseDouble(attrs.getValue("lat"));
            final Double lon = parseDouble(attrs.getValue("lon"));
            if (lat != null && lon != null) {
                currentPointData.put(GPXWaypointParser.WPT_GPX_COORDINATE, new Geopoint(lat, lon));
            }
        });

        registerStandardPointTags(pointElement, () -> currentPointData);
        registerExtensionPointTags(pointElement, () -> currentPointData); //GPX1.0
        registerExtensionPointTags(pointElement.getChild("extensions"), () -> currentPointData); //GPX1.1

        pointElement.setEndElementListener(() -> {
            final GPXPointSeries series = seriesSupplier.get();
            final IGeoObject geoObject = toGeoObject(gpxData, currentPointData);
            if (geoObject != null) {
                if (geoObject instanceof Geocache) {
                    final Geocache cache = (Geocache) geoObject;
                    gpxData.geocaches.put(cache.getGeocode(), cache);
                }
                if (series != null) {
                    series.points.add(geoObject);
                }
            }
            currentPointData.clear();
        });
    }

    private static void registerStandardPointTags(final Element pointElement, final Supplier<Map<String, Object>> mapSupplier)  {
        //register standard GPX elements
        registerPointTag(pointElement, "name", GPXWaypointParser.WPT_GPX_NAME, mapSupplier);
        registerPointTag(pointElement, "ele", GPXWaypointParser.WPT_GPX_ELEVATION, mapSupplier, GPXData::parseFloat);
        registerPointTag(pointElement, "time", GPXWaypointParser.WPT_GPX_TIME, mapSupplier, GPXData::parseDate);
        registerPointTag(pointElement, "desc", GPXWaypointParser.WPT_GPX_DESCRIPTION, mapSupplier);
        registerPointTag(pointElement, "cmt", GPXWaypointParser.WPT_GPX_CMT, mapSupplier);
        registerText(pointElement.getChild("type"), typeText -> {
            final String[] content = StringUtils.split(typeText, '|');
            if (content.length > 0) {
                mapSupplier.get().put(WPT_GPX_TYPE, content[0].toLowerCase(Locale.US).trim());
                if (content.length > 1) {
                    mapSupplier.get().put(WPT_GPX_SUBTYPE, content[1].toLowerCase(Locale.US).trim());
                }
            }
        });
        registerPointTag(pointElement, "sym", GPXWaypointParser.WPT_GPX_SYM, mapSupplier);
        registerUrl(pointElement, url -> mapSupplier.get().put(GPXWaypointParser.WPT_GPX_URL, url), urlName -> mapSupplier.get().put(GPXWaypointParser.WPT_GPX_URLNAME, urlName));
    }

    private static void registerExtensionPointTags(final Element extensionElement, final Supplier<Map<String, Object>> mapSupplier) {
        registerGsakExtensions(extensionElement, mapSupplier);
        registerTerraCachingExtensions(extensionElement, mapSupplier);
        registerCgeoExtensions(extensionElement, mapSupplier);
        registerOpenCachingExtensions(extensionElement, mapSupplier);
        registerGroundspeakExtensions(extensionElement, mapSupplier);
    }

    private static void registerCgeoExtensions(final Element extensionElement, final Supplier<Map<String, Object>> mapSupplier) {
        registerPointTag(extensionElement, "visited", GPXWaypointParser.WPT_CGEO_VISITED, mapSupplier, GPXData::parseBoolean);
        registerPointTag(extensionElement, "userdefined", GPXWaypointParser.WPT_CGEO_USERDEFINED, mapSupplier, GPXData::parseBoolean);
        registerPointTag(extensionElement, "originalCoordsEmpty", GPXWaypointParser.WPT_CGEO_ORIGINAL_COORDS_EMPTY, mapSupplier, GPXData::parseBoolean);
        registerPointTag(extensionElement.getChild("cacheExtension"), GPXWaypointParser.WPT_CGEO_ASSIGNED_EMOJI, "assignedEmoji", mapSupplier, GPXData::parseInt);
    }

    private static void registerOpenCachingExtensions(final Element extensionElement, final Supplier<Map<String, Object>> mapSupplier) {
        final Element ocCache = extensionElement.getChild("cache");
        registerPointTag(ocCache, "requires_password", WPT_OC_REQUIRES_PASSWORD, mapSupplier, GPXData::parseBoolean);
        registerPointTag(ocCache, "other_code", WPT_OC_OTHER_CODE, mapSupplier);
        registerPointTag(ocCache, "size", WPT_OC_SIZE, mapSupplier, CacheSize::getById);
    }


    private static void registerGsakExtensions(final Element extensionElement, final Supplier<Map<String, Object>> mapSupplier) {
        final Element gsak = extensionElement.getChild("wptExtension");
        registerPointTag(gsak, "Watch", WPT_GSAK_WATCH, mapSupplier, GPXData::parseBoolean);
        registerPointTag(gsak, "UserData", WPT_GSAK_USER_DATA_1, mapSupplier, GPXData::parseUsername);
        registerPointTag(gsak, "User2", WPT_GSAK_USER_DATA_2, mapSupplier, GPXData::parseUsername);
        registerPointTag(gsak, "User3", WPT_GSAK_USER_DATA_3, mapSupplier, GPXData::parseUsername);
        registerPointTag(gsak, "User4", WPT_GSAK_USER_DATA_4, mapSupplier, GPXData::parseUsername);
        registerPointTag(gsak, "Parent", WPT_GSAK_PARENT, mapSupplier);
        registerPointTag(gsak, "FavPoints", WPT_GSAK_FAVORITE_POINTS, mapSupplier, GPXData::parseInt);
        registerPointTag(gsak, "GcNote", WPT_GSAK_GC_NOTE, mapSupplier);
        registerPointTag(gsak, "IsPremium", WPT_GSAK_IS_PREMIUM, mapSupplier, GPXData::parseBoolean);
        registerPointTag(gsak, "LatBeforeCorrect", WPT_GSAK_LAT_BEFORE_CORRECT, mapSupplier, GPXData::parseDouble);
        registerPointTag(gsak, "LonBeforeCorrect", WPT_GSAK_LON_BEFORE_CORRECT, mapSupplier, GPXData::parseDouble);
        registerPointTag(gsak, "Code", WPT_GSAK_CODE, mapSupplier);
        registerPointTag(gsak, "DNF", WPT_GSAK_DNF, mapSupplier, GPXData::parseBoolean);
        registerPointTag(gsak, "DNFDate", WPT_GSAK_DNF_DATE, mapSupplier, GPXData::parseDate);
        registerPointTag(gsak, "UserFound", WPT_GSAK_USER_FOUND, mapSupplier, GPXData::parseDate);
        registerPointTag(gsak, "Child_ByGSAK", WPT_GSAK_CHILD_BY_GSAK, mapSupplier, GPXData::parseBoolean);
    }

    private static void registerGroundspeakExtensions(final Element extensionElement, final Supplier<Map<String, Object>> mapSupplier) {
        final Element cache = extensionElement.getChild("cache");

        //placeholders for attributes, travelbugs and log entries
        final List<String> attributes = new ArrayList<>();
        final Map<String, String> travelBugs = new HashMap<>();
        final String[] currentTravelBugRef = new String[]{null};
        final List<LogEntry> logEntries = new ArrayList<>();
        final LogEntry.Builder[] logBuilder = new LogEntry.Builder[]{null};

        cache.setStartElementListener(attrs -> {
            mapSupplier.get().put(WPT_GC_ID, parseLong(attrs.getValue("id")));
            mapSupplier.get().put(WPT_GC_ARCHIVED, parseBoolean(attrs.getValue("archived")));
            mapSupplier.get().put(WPT_GC_AVAILABLE, parseBoolean(attrs.getValue("available")));

            attributes.clear();
            logEntries.clear();
            travelBugs.clear();
        });
        cache.setEndElementListener(() -> {
            if (!attributes.isEmpty()) {
                mapSupplier.get().put(WPT_GC_ATTRIBUTES, new ArrayList<>(attributes));
            }
            if (!logEntries.isEmpty()) {
                mapSupplier.get().put(WPT_GC_LOGS, new ArrayList<>(logEntries));
            }
            if (!travelBugs.isEmpty()) {
                mapSupplier.get().put(WPT_GC_INVENTORY, new HashMap<>(travelBugs));
            }
        });

        registerPointTag(cache, "name", WPT_GC_NAME, mapSupplier);
        registerPointTag(cache, "owner", WPT_GC_OWNER, mapSupplier, GPXData::parseUsername);
        registerPointTag(cache, "placed_by", WPT_GC_PLACED_BY, mapSupplier, GPXData::parseUsername);
        registerPointTag(cache, "type", WPT_GC_TYPE, mapSupplier);
        registerPointTag(cache, "container", WPT_GC_CONTAINER, mapSupplier, CacheSize::getById);
        registerPointTag(cache, "difficulty", WPT_GC_DIFFICULTY, mapSupplier, GPXData::parseFloat);
        registerPointTag(cache, "terrain", WPT_GC_TERRAIN, mapSupplier, GPXData::parseFloat);
        registerPointTag(cache, "country", WPT_GC_COUNTRY, mapSupplier);
        registerPointTag(cache, "state", WPT_GC_STATE, mapSupplier);
        registerPointTag(cache, "encoded_hints", WPT_GC_ENCODED_HINTS, mapSupplier);
        registerPointTag(cache, "short_description", WPT_GC_SHORT_DESCRIPTION, mapSupplier);
        registerPointTag(cache, "long_description", WPT_GC_LONG_DESCRIPTION, mapSupplier);

        //attributes
        // <groundspeak:attributes>
        //   <groundspeak:attribute id="32" inc="1">Bicycles</groundspeak:attribute>
        //   <groundspeak:attribute id="13" inc="1">Available at all times</groundspeak:attribute>
        // where inc = 0 => _no, inc = 1 => _yes
        // IDs see array CACHE_ATTRIBUTES
        final Element gcAttribute = cache.getChild("attributes").getChild("attribute");
        gcAttribute.setStartElementListener(attrs -> {
            final Integer id = parseInt(attrs.getValue("id"));
            final Integer inc = parseInt(attrs.getValue("inc"));
            if (id != null && inc != null) {
                final CacheAttribute ca = CacheAttribute.getById(id);
                if (ca != null) {
                    attributes.add(ca.getValue(inc != 0));
                }
            }
        });

        //logs
        // cache.logs.log
        final Element gcLog = cache.getChild("logs").getChild("log");

        gcLog.setStartElementListener(attrs -> {
            logBuilder[0] = new LogEntry.Builder();
            final Integer id = parseInt(attrs.getValue("id"));
            if (id != null) {
                logBuilder[0].setId(id);
                logBuilder[0].setServiceLogId(GCUtils.logIdToLogCode(id));
            }
        });

        gcLog.setEndElementListener(() -> {
            final LogEntry log = logBuilder[0].build();
            if (log.logType != LogType.UNKNOWN) {
                logEntries.add(log);
            }
            logBuilder[0] = null;
        });
        // waypoint.cache.logs.log.date
        registerText(gcLog.getChild("date"), txt -> {
            final Date date = parseDate(txt);
                logBuilder[0].setDate(date == null ? 0 : date.getTime());
        });
        // waypoint.cache.logs.log.getType()
        registerText(gcLog.getChild("type"), txt ->
                logBuilder[0].setLogType(LogType.getByType(txt)));
        // waypoint.cache.logs.log.finder
        registerText(gcLog.getChild("finder"), txt ->
                logBuilder[0].setAuthor(txt));
        // waypoint.cache.logs.log.text
        registerText(gcLog.getChild("text"), txt ->
                logBuilder[0].setLog(txt));

        //tb inventory
        // waypoint.cache.travelbugs.travelbug
        final Element gcTB = cache.getChild("travelbugs").getChild("travelbug");
        gcTB.setStartElementListener(attrs -> {
            final String ref = attrs.getValue("ref");
            if (ref != null) {
                travelBugs.put(ref, null);
                currentTravelBugRef[0] = ref;
            }
        });

        gcTB.setEndElementListener(() -> currentTravelBugRef[0] = null);
        // waypoint.cache.travelbugs.travelbug.getName()
        registerText(gcTB.getChild("name"), text -> {
            if (currentTravelBugRef[0] != null) {
                travelBugs.put(currentTravelBugRef[0], text);
            }
        });
    }

    private static void registerTerraCachingExtensions(final Element extensionElement, final Supplier<Map<String, Object>> mapSupplier) {
        final Element terraCache = extensionElement.getChild("terracache");

        //placeholders for log entries
        final List<LogEntry> logEntries = new ArrayList<>();
        final LogEntry.Builder[] logBuilder = new LogEntry.Builder[]{null};

        terraCache.setStartElementListener(attrs -> {
            logEntries.clear();
        });
        terraCache.setEndElementListener(() -> {
            if (!logEntries.isEmpty()) {
                mapSupplier.get().put(WPT_TERRA_LOGS, new ArrayList<>(logEntries));
            }
        });

        registerPointTag(terraCache, "name", WPT_TERRA_NAME, mapSupplier);
        registerPointTag(terraCache, "owner", WPT_TERRA_OWNER, mapSupplier, GPXData::parseUsername);
        registerPointTag(terraCache, "style", WPT_TERRA_STYLE, mapSupplier, TerraCachingType::getCacheType);
        registerPointTag(terraCache, "size", WPT_TERRA_SIZE, mapSupplier, CacheSize::getById);
        registerPointTag(terraCache, "country", WPT_TERRA_COUNTRY, mapSupplier);
        registerPointTag(terraCache, "state", WPT_TERRA_STATE, mapSupplier);
        registerPointTag(terraCache, "description", WPT_TERRA_DESCRIPTION, mapSupplier);
        registerPointTag(terraCache, "hint", WPT_TERRA_HINT, mapSupplier);

        final Element terraLog = terraCache.getChild("logs").getChild("log");

        terraLog.setStartElementListener(attrs -> {
            logBuilder[0] = new LogEntry.Builder();
            final Integer id = parseInt(attrs.getValue("id"));
            if (id != null) {
                logBuilder[0].setId(id);
            }
        });

        terraLog.setEndElementListener(() -> {
            final LogEntry log = logBuilder[0].build();
            if (log.logType != LogType.UNKNOWN) {
                logEntries.add(log);
            }
            logBuilder[0] = null;
        });
        // waypoint.cache.logs.log.date
        registerText(terraLog.getChild("date"), txt -> {
            final Date date = parseDate(txt);
            logBuilder[0].setDate(date == null ? 0 : date.getTime());
        });
        // waypoint.cache.logs.log.getType()
        registerText(terraLog.getChild("type"), txt ->
                logBuilder[0].setLogType(TerraCachingLogType.getLogType(txt)));
        // waypoint.cache.logs.log.finder
        registerText(terraLog.getChild("user"), txt ->
                logBuilder[0].setAuthor(txt));
        // waypoint.cache.logs.log.text
        registerText(terraLog.getChild("entry"), txt ->
                logBuilder[0].setLog(txt));
    }



    private static IGeoObject toGeoObject(final GPXData gpxData, final Map<String, Object> xmlData) {
        return gpxData.wpParser.toGeoObject(xmlData);
    }

    @SuppressWarnings("unchecked")
    private static <T> T getValue(final Map<String, Object> map, final String key, final T defaultValue) {
        final T value = (T) map.get(key);
        return value == null ? defaultValue : value;
    }

    private static void registerHeaderData(final Element root, final GPXData data) {
        root.setStartElementListener(attrs -> {
            data.creator = attrs.getValue("creator");
        });
        registerUrl(root, url -> data.url = url, urlName -> data.urlName = urlName);
        registerUrl(root.getChild("metadata"), url -> data.url = url, urlName -> data.urlName = urlName);
    }

    private static void registerWpt(final Element root, final GPXData data) {
        registerPointSeries(data, root.getChild("wpt"), () -> data.wptSeries);
    }

    private static void registerRoute(final Element root, final GPXData data) {
        final Element routeElement = root.getChild("rte");
        final List<GPXPointSeries> series = data.routeSeries;

        final GPXPointSeries[] currentSeries = new GPXPointSeries[]{null};


        routeElement.setStartElementListener(attrs -> {
            currentSeries[0] = new GPXPointSeries();
        });
        routeElement.getChild("name").setEndTextElementListener(text -> {
            if (currentSeries[0] != null) {
                currentSeries[0].name = text;
            }
        });
        registerPointSeries(data, routeElement.getChild("rtept"), () -> currentSeries[0]);
        routeElement.setEndElementListener(() -> {
            if (currentSeries[0] != null && !currentSeries[0].points.isEmpty()) {
                series.add(currentSeries[0]);
            }
            currentSeries[0] = null;
        });
    }

    private static void registerTrack(final Element root, final GPXData data) {
        final Element trackElement = root.getChild("trk");
        final Element trackSegmentElement = trackElement.getChild("trkseg");
        final List<GPXPointSeries> series = data.trackSeries;

        final String[] trackName = new String[]{null};
        final int[] trackIdx = new int[]{0};
        final GPXPointSeries[] currentSeries = new GPXPointSeries[]{null};

        //track element
        trackElement.setStartElementListener(attrs -> {
            trackName[0] = null;
            trackIdx[0] = 0;
        });
        trackElement.getChild("name").setEndTextElementListener(text -> {
            trackName[0] = text;
        });
        trackElement.setEndElementListener(() -> {
            trackName[0] = null;
            trackIdx[0] = 0;
        });

        //track segment element
        trackSegmentElement.setStartElementListener(attrs -> {
            currentSeries[0] = new GPXPointSeries();
            currentSeries[0].name = trackName[0];
            currentSeries[0].index = trackIdx[0];
        });
        registerPointSeries(data, trackSegmentElement.getChild("trkpt"), () -> currentSeries[0]);
        trackSegmentElement.setEndElementListener(() -> {
            if (currentSeries[0] != null && !currentSeries[0].points.isEmpty()) {
                series.add(currentSeries[0]);
            }
            currentSeries[0] = null;
        });
    }

    public static GPXData parse(final String xmlName, final InputStream stream) {
        return parse(xmlName, new InputStreamReader(stream, StandardCharsets.UTF_8));
    }

    public static GPXData parse(final String xmlName, final Reader xmlReader) {

        final GPXData gpxData = new GPXData();
        final RootElement root = new RootElement("gpx");

        registerHeaderData(root, gpxData);
        registerWpt(root, gpxData);
        registerRoute(root, gpxData);
        registerTrack(root, gpxData);

        XmlUtils.parseXml(xmlName, xmlReader, root.getContentHandler(), true, false, true);
        return gpxData;
    }

    public String getCreator() {
        return creator;
    }

    public Collection<Geocache> getGeocaches() {
        return geocaches.values();
    }

    public Geocache getGeocacheFor(final String geocode) {
        return geocaches.get(geocode);
    }

    public String getUrl() {
        return url;
    }

    public String getUrlName() {
        return urlName;
    }

    public GPXPointSeries getWptSeries() {
        return wptSeries;
    }

    public List<GPXPointSeries> getRouteSeries() {
        return routeSeries;
    }

    public List<GPXPointSeries> getTrackSeries() {
        return trackSeries;
    }

    private static void registerUrl(final Element element, final Consumer<String> urlConsumer, final Consumer<String> urlNameConsumer) {
        element.getChild("link").setStartElementListener(attrs -> {
            if (attrs.getIndex("href") > -1) {
                urlConsumer.accept(attrs.getValue("href"));
            }
        });
        registerText(element.getChild("href"), urlConsumer);
        registerText(element.getChild("url"), urlConsumer);
        registerText(element.getChild("urlname"), urlNameConsumer);
        registerText(element.getChild("text"), urlNameConsumer);
    }

    private static void registerPointTag(final Element parent, final String tagName, final String key, final Supplier<Map<String, Object>> data) {
        registerPointTag(parent, tagName, key, data, null);
    }

    private static void registerPointTag(final Element parent, final String tagName, final String key, final Supplier<Map<String, Object>> data, final Function<String, Object> mapper) {
        registerText(parent.getChild(tagName), text -> {
            final Object obj = mapper == null ? text : mapper.apply(text);
            if (obj != null) {
                data.get().put(key, obj);
            }
        });
    }

    private static void registerText(final Element element, final Consumer<String> consumer) {
        element.setEndTextElementListener(text -> {
            if (text != null) {
                consumer.accept(text);
            }
        });
    }

    private static Boolean parseBoolean(final String value) {
        return BooleanUtils.toBoolean(value == null ? null : value.trim());
    }

    private static Integer parseInt(final String value) {
        final int i = NumberUtils.toInt(value, Integer.MIN_VALUE);
        return i == Integer.MIN_VALUE ? null : i;
    }

    private static Long parseLong(final String value) {
        final long i = NumberUtils.toLong(value, Long.MIN_VALUE);
        return i == Long.MIN_VALUE ? null : i;
    }

    private static Double parseDouble(final String value) {
        final double d = NumberUtils.toDouble(value, Double.NaN);
        return Double.isNaN(d) ? null : d;
    }

    private static Float parseFloat(final String value) {
        final float d = NumberUtils.toFloat(value, Float.NaN);
        return Float.isNaN(d) ? null : d;
    }

    private static Date parseDate(final String inputUntrimmed) {
        // remove milliseconds to reduce number of needed patterns
        final MatcherWrapper matcher = new MatcherWrapper(DATE_PATTERN_MILLISECONDS, inputUntrimmed.trim());
        final String input = matcher.replaceFirst("");
        try {
            if (input.contains("Z")) {
                return DATE_FORMAT_Z.parse(input);
            }
            if (StringUtils.countMatches(input, ":") == 3) {
                final String removeColon = input.substring(0, input.length() - 3) + input.substring(input.length() - 2);
                return DATE_FORMAT_TIMEZONE.parse(removeColon);
            }
            if (input.contains("T")) {
                return DATE_FORMAT_SIMPLE.parse(input);
            }
            return DATE_FORMAT_SIMPLE_NOTIME.parse(input);
        } catch (ParseException pe) {
            return null;
        }
    }

    private static String parseUsername(final String input) {
        if ("nil".equalsIgnoreCase(input)) {
            return "";
        }
        return input.trim();
    }

}
