package cgeo.geocaching.connector.tc;

import cgeo.geocaching.log.LogType;

import androidx.annotation.NonNull;

import java.util.Locale;

/**
 * adapter for terracaching log types
 */
public final class TerraCachingLogType {

    private TerraCachingLogType() {
        // utility class
    }

    @NonNull
    public static LogType getLogType(@NonNull final String logtype) {
        if (logtype == null) {
            return LogType.UNKNOWN;
        }
        switch (logtype.toLowerCase(Locale.US)) {
            case "found it!":
            case "find":
                return LogType.FOUND_IT;
            case "missing?":
                return LogType.DIDNT_FIND_IT;
            case "note":
                return LogType.NOTE;
            case "repair required":
                return LogType.NEEDS_MAINTENANCE;
        }
        return LogType.UNKNOWN;
    }
}
