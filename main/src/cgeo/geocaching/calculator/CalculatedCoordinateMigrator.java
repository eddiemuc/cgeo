package cgeo.geocaching.calculator;

import cgeo.geocaching.models.CalculatedCoordinateType;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * An instance of this class represents the state of an OLD calculated waypoint
 */
public class CalculatedCoordinateMigrator {

//    format = Settings.CoordInputFormatEnum.fromInt(json.optInt("format", Settings.CoordInputFormatEnum.DEFAULT_INT_VALUE));
//    plainLat = json.optString("plainLat");
//    plainLon = json.optString("plainLon");
//    latHemisphere = (char) json.optInt("latHemisphere", ERROR_CHAR);
//    lonHemisphere = (char) json.optInt("lonHemisphere", ERROR_CHAR);
//    buttons       = createJSONAbleList(json.optJSONArray("buttons"),       new ButtonDataFactory());
//    equations     = createJSONAbleList(json.optJSONArray("equations"),     new VariableDataFactory());
//    freeVariables = createJSONAbleList(json.optJSONArray("freeVariables"), new VariableDataFactory());
//    variableBank = new ArrayList<>(); // "variableBank" intentionally not loaded.

    private static final CalculatedCoordinateType[] CALC_TYPES = new CalculatedCoordinateType[] {
        CalculatedCoordinateType.PLAIN, CalculatedCoordinateType.DEGREE, CalculatedCoordinateType.DEGREE_MINUTE, CalculatedCoordinateType.DEGREE_MINUTE_SEC
    };

    private enum ButtonType { INPUT, AUTO, BLANK, CUSTOM };

    private CalculatedCoordinateType type;
    private String latPattern;
    private String lonPattern;
    private final Map<String, String> variables = new HashMap<>();


    private CalculatedCoordinateMigrator() {

    }

    public static CalculatedCoordinateMigrator createFromJson(final String jsonString) throws JSONException {
        final JSONObject json = new JSONObject(jsonString);
        final CalculatedCoordinateMigrator ccm = new CalculatedCoordinateMigrator();
        ccm.type = CALC_TYPES[json.optInt("format", 2)];
        if (ccm.type == CalculatedCoordinateType.PLAIN) {
            ccm.latPattern = json.optString("plainLat");
            ccm.lonPattern = json.optString("plainLon");
        } else {
            ccm.latPattern = "" + (char) json.optInt("latHemisphere") + parseButtonDataFromJson(json.optJSONArray("buttons"));
            ccm.lonPattern = "" + (char) json.optInt("lonHemisphere") + parseButtonDataFromJson(json.optJSONArray("buttons"));
        }

        addVariablesFromJson(ccm.variables, json.optJSONArray("equations"));
        addVariablesFromJson(ccm.variables, json.optJSONArray("freeVariables"));

        return ccm;
    }

    private static String parseButtonDataFromJson(final JSONArray ja) throws JSONException {
        if (ja == null) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ja.length(); i++) {
            final JSONObject jo = ja.getJSONObject(i);
            final ButtonType bt = ButtonType.values()[jo.getInt("type")];
            final char c;
            switch (bt) {
                case INPUT:
                    c = ((char) jo.getInt("inputVal"));
                    break;
                case AUTO:
                    c = ((char) jo.getInt("autoChar"));
                    break;
                case CUSTOM:
                    c = ((char) jo.getInt("customChar"));
                    break;
                case BLANK:
                default:
                    c = '_';
                    break;
            }
            sb.append(c);
        }
        return sb.toString();
    }

    private static void addVariablesFromJson(final Map<String, String> varMap, final JSONArray ja) throws JSONException {
        if (ja == null) {
            return;
        }
        for (int i = 0; i < ja.length(); i++) {
            final JSONObject jo = ja.getJSONObject(i);
            final String varName = "" + ((char) jo.getInt("name"));
            final String formula = jo.getString("expression");
            varMap.put(varName, formula);
        }
    }

    public CalculatedCoordinateType getType() {
        return type;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public String getLatPattern() {
        return latPattern;
    }

    public String getLonPattern() {
        return lonPattern;
    }

    @NonNull
    @Override
    public String toString() {
        return "Type:" + type + ",lat:" + latPattern + ",lon:" + lonPattern + ", vars:" + variables;
    }
}
