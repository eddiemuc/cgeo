package cgeo.geocaching.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface IGeoObject extends ICoordinates {

    /**
     * @return Geocode like GCxxxx
     */
    @NonNull
    String getGeocode();

    @Nullable
    String getName();

}
