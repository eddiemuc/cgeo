package cgeo.geocaching.models;

import cgeo.geocaching.enumerations.CoordinatesType;
import cgeo.geocaching.enumerations.WaypointType;

import androidx.annotation.Nullable;

public interface IWaypoint extends IGeoObject {

    /**
     * Return an unique waypoint id.
     *
     * @return a non-negative id if set, -1 if unset
     */
    int getId();

    WaypointType getWaypointType();

    CoordinatesType getCoordType();

    @Nullable
    @Override
    default String getName() {
        return null;
    }
}
