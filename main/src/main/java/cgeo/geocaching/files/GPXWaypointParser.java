package cgeo.geocaching.files;

import cgeo.geocaching.connector.ConnectorFactory;
import cgeo.geocaching.connector.IConnector;
import cgeo.geocaching.connector.gc.GCUtils;
import cgeo.geocaching.enumerations.WaypointType;
import cgeo.geocaching.models.Geocache;
import cgeo.geocaching.models.IGeoObject;
import cgeo.geocaching.models.SimpleGeoObject;
import cgeo.geocaching.models.Waypoint;
import cgeo.geocaching.utils.MatcherWrapper;

import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class GPXWaypointParser {

    public static final String WPT_GPX = "gpx";
    public static final String WPT_GPX_COORDINATE = WPT_GPX + ".latlon";
    public static final String WPT_GPX_NAME = WPT_GPX + ".name";
    public static final String WPT_GPX_ELEVATION = WPT_GPX + ".ele";
    public static final String WPT_GPX_TIME = WPT_GPX + ".time";
    public static final String WPT_GPX_DESCRIPTION = WPT_GPX + ".desc";
    public static final String WPT_GPX_CMT = WPT_GPX + ".cmt";
    public static final String WPT_GPX_TYPE = WPT_GPX + ".type";
    public static final String WPT_GPX_SUBTYPE = WPT_GPX + ".subtype";
    public static final String WPT_GPX_SYM = WPT_GPX + ".sym";
    public static final String WPT_GPX_URL = WPT_GPX + ".url";
    public static final String WPT_GPX_URLNAME = WPT_GPX + ".urlname";

    public static final String WPT_CGEO = "cgeo";
    public static final String WPT_CGEO_VISITED = WPT_CGEO + ".visited";
    public static final String WPT_CGEO_USERDEFINED = WPT_CGEO + ".userdefined";
    public static final String WPT_CGEO_ORIGINAL_COORDS_EMPTY = WPT_CGEO + ".originalCoordsEmpty";
    public static final String WPT_CGEO_ASSIGNED_EMOJI = WPT_CGEO + ".assignedEmoji";

    public static final String WPT_OC = "opencaching";
    public static final String WPT_OC_REQUIRES_PASSWORD = WPT_OC + ".requires_password";
    public static final String WPT_OC_OTHER_CODE = WPT_OC + ".other_code";
    public static final String WPT_OC_SIZE = WPT_OC + ".size";

    public static final String WPT_GSAK = "gsak";
    public static final String WPT_GSAK_WATCH = WPT_GSAK + ".watch";
    public static final String WPT_GSAK_USER_DATA_1 = WPT_GSAK + ".UserData1";
    public static final String WPT_GSAK_USER_DATA_2 = WPT_GSAK + ".UserData2";
    public static final String WPT_GSAK_USER_DATA_3 = WPT_GSAK + ".UserData3";
    public static final String WPT_GSAK_USER_DATA_4 = WPT_GSAK + ".UserData4";
    public static final String WPT_GSAK_PARENT = WPT_GSAK + ".Parent";
    public static final String WPT_GSAK_FAVORITE_POINTS = WPT_GSAK + ".FavPoints";
    public static final String WPT_GSAK_GC_NOTE = WPT_GSAK + ".GcNote";
    public static final String WPT_GSAK_IS_PREMIUM = WPT_GSAK + ".IsPremium";
    public static final String WPT_GSAK_LAT_BEFORE_CORRECT = WPT_GSAK + ".LatBeforeCorrect";
    public static final String WPT_GSAK_LON_BEFORE_CORRECT = WPT_GSAK + ".LonBeforeCorrect";
    public static final String WPT_GSAK_CODE = WPT_GSAK + ".Code";
    public static final String WPT_GSAK_DNF = WPT_GSAK + ".DNF";
    public static final String WPT_GSAK_DNF_DATE = WPT_GSAK + ".DNFDate";
    public static final String WPT_GSAK_USER_FOUND = WPT_GSAK + ".UserFound";
    public static final String WPT_GSAK_CHILD_BY_GSAK = WPT_GSAK + ".Child_ByGSAK";

    public static final String WPT_GC = "groundspeak";
    public static final String WPT_GC_ID = WPT_GC + ".id";
    public static final String WPT_GC_ARCHIVED = WPT_GC + ".archived";
    public static final String WPT_GC_AVAILABLE = WPT_GC + ".available";
    public static final String WPT_GC_ATTRIBUTES = WPT_GC + ".attributes";
    public static final String WPT_GC_INVENTORY = WPT_GC + ".inventory";
    public static final String WPT_GC_LOGS = WPT_GC + ".logs";
    public static final String WPT_GC_NAME = WPT_GC + ".name";
    public static final String WPT_GC_OWNER = WPT_GC + ".owner";
    public static final String WPT_GC_PLACED_BY = WPT_GC + ".placed_by";
    public static final String WPT_GC_TYPE = WPT_GC + ".type";
    public static final String WPT_GC_CONTAINER = WPT_GC + ".container";
    public static final String WPT_GC_DIFFICULTY = WPT_GC + ".difficulty";
    public static final String WPT_GC_TERRAIN = WPT_GC + ".terrain";
    public static final String WPT_GC_COUNTRY = WPT_GC + ".country";
    public static final String WPT_GC_STATE = WPT_GC + ".state";
    public static final String WPT_GC_ENCODED_HINTS = WPT_GC + ".encoded_hints";
    public static final String WPT_GC_SHORT_DESCRIPTION = WPT_GC + ".short_description";
    public static final String WPT_GC_LONG_DESCRIPTION = WPT_GC + ".long_description";

    public static final String WPT_TERRA = "terracaching";
    public static final String WPT_TERRA_LOGS = WPT_TERRA + ".logs";
    public static final String WPT_TERRA_NAME = WPT_TERRA + ".name";
    public static final String WPT_TERRA_OWNER = WPT_TERRA + ".owner";
    public static final String WPT_TERRA_STYLE = WPT_TERRA + ".style";
    public static final String WPT_TERRA_SIZE = WPT_TERRA + ".size";
    public static final String WPT_TERRA_COUNTRY = WPT_TERRA + ".country";
    public static final String WPT_TERRA_STATE = WPT_TERRA + ".state";
    public static final String WPT_TERRA_DESCRIPTION = WPT_TERRA + ".description";
    public static final String WPT_TERRA_HINT = WPT_TERRA + ".hint";

    private static final Pattern PATTERN_GEOCODE = Pattern.compile("\\s([A-Z]{2}[0-9A-Z]{3,})\\s");

    private enum WpType { NONE, PLAIN, GEOCACHE, WAYPOINT }

    private final GPXData gpxData;

    GPXWaypointParser(final GPXData gpxData) {
        this.gpxData = gpxData;
    }

    private boolean isExtremCaching() {
        return StringUtils.containsIgnoreCase(gpxData.getUrl(), "extremcaching");
    }

    private boolean isTerraCaching() {
        return StringUtils.containsIgnoreCase(gpxData.getUrl(), "terracaching");
    }

    public IGeoObject toGeoObject(final Map<String, Object> xmlData) {

        final Pair<WpType, String> typeParent = calculateType(xmlData);
        switch(typeParent.first) {
            case GEOCACHE:
                return toGeocache(xmlData);
            case WAYPOINT:
                final Waypoint wp = toWaypoint(xmlData, typeParent.second);
                final Geocache cache = gpxData.getGeocacheFor(typeParent.second);
                if (cache != null) {
                    cache.addOrChangeWaypoint(wp, false);
                }
                return wp;
            case PLAIN:
                return toSimpleGeoObject(xmlData);
            default:
                return null;
        }
    }

    private SimpleGeoObject toSimpleGeoObject(final Map<String, Object> xmlData) {
        final SimpleGeoObject result = new SimpleGeoObject();
        result.setCoords(getValue(xmlData, WPT_GPX_COORDINATE));
        result.setGeocode(getValue(xmlData, WPT_GPX_NAME));
        result.setName(getValue(xmlData, WPT_GPX_NAME));
        result.setElevation(getValue(xmlData, WPT_GPX_ELEVATION, 0));
        return result;
    }

    private Geocache toGeocache(final Map<String, Object> xmlData) {
        final Geocache result = new Geocache();
        result.setCoords(getValue(xmlData, WPT_GPX_COORDINATE));
        result.setGeocode(getValue(xmlData, WPT_GPX_NAME));
        result.setName(getValue(xmlData, WPT_GPX_NAME));
        return result;
    }

    private Waypoint toWaypoint(final Map<String, Object> xmlData, final String parentCode) {
        final Waypoint result = new Waypoint("", WaypointType.fromGPXString(getValue(xmlData, WPT_GPX_SYM, ""), getValue(xmlData, WPT_GPX_SUBTYPE, "")), false);
        result.setCoords(getValue(xmlData, WPT_GPX_COORDINATE));
        result.setGeocode(getValue(xmlData, WPT_GPX_NAME));
        result.setName(getValue(xmlData, WPT_GPX_NAME));
        return result;
    }

    private Pair<WpType, String> calculateType(final Map<String, Object> xmlData) {
        if (getValue(xmlData, WPT_GPX_COORDINATE) == null) {
            return new Pair<>(WpType.NONE, null);
        }
        final String type = getValue(xmlData, WPT_GPX_TYPE, "".trim());
        final String name = getValue(xmlData, WPT_GPX_NAME, "").trim();
        final String sym = getValue(xmlData, WPT_GPX_SYM, "").trim();
        if (StringUtils.isBlank(name) || (StringUtils.isBlank(type) && StringUtils.isBlank(sym))) {
            return new Pair<>(WpType.PLAIN, null);
        }

        if (isTerraCaching()) {
            return "GC_WayPoint1".equals(getValue(xmlData, WPT_GPX_DESCRIPTION)) ? new Pair<>(WpType.GEOCACHE, null) :
                    new Pair<>(WpType.WAYPOINT, name.substring(0, name.length() - 1));
        }

        final String checkString = StringUtils.toRootLowerCase(type + "|" + sym);
        if (StringUtils.containsAny(checkString, "geocache", "waymark")) {
            return new Pair<>(WpType.GEOCACHE, null);
        }
        if (StringUtils.containsAny(checkString, "waypoint")) {
            String parentCode = getValue(xmlData, WPT_GSAK_PARENT);
            if (parentCode == null) {
                parentCode = isExtremCaching() ? name.substring(2) : "GC" + name.substring(2).toUpperCase(Locale.US);
            }
            return new Pair<>(WpType.WAYPOINT, parentCode);
        }

        return new Pair<>(WpType.PLAIN, null);

    }

    private static String getGeocode(@NonNull final Map<String, Object> xmlData) {
        final Long geocodeid = getValue(xmlData, WPT_GC_ID);
        if (geocodeid != null) {
            return GCUtils.gcIdToGcCode(geocodeid);
        }
        String geocode = getValue(xmlData, WPT_GSAK_CODE);
        if (geocode != null) {
            return geocode;
        }
        geocode = findGeocode(getValue(xmlData, WPT_GPX_NAME));
        return geocode;
    }

    private static String findGeocode(final String text) {
        final MatcherWrapper matcher = new MatcherWrapper(PATTERN_GEOCODE, " " + text + " ");
        while (matcher.find()) {
            final String candidate = matcher.group(1);
            final IConnector con = ConnectorFactory.getConnector(candidate);
            if (!con.equals(ConnectorFactory.UNKNOWN_CONNECTOR)) {
                return candidate;
            }
        }
        return null;

    }

    private static <T> T getValue(final Map<String, Object> map, final String key) {
        return getValue(map, key, null);
    }

    @SuppressWarnings("unchecked")
    private static <T> T getValue(final Map<String, Object> map, final String key, final T defaultValue) {
        final T value = (T) map.get(key);
        return value == null ? defaultValue : value;
    }
}


