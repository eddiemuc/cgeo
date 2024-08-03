package cgeo.geocaching.models;

import cgeo.geocaching.location.Geopoint;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

public class SimpleGeoObject implements IGeoObject, Parcelable {

    private Geopoint coord;
    private String name;
    private String geocode;
    private float elevation;

    public SimpleGeoObject() {
        //empty on purpose
    }

    public void setCoords(final Geopoint coord) {
        this.coord = coord;
    }

    public void setElevation(final float elevation) {
        this.elevation = elevation;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public Geopoint getCoords() {
        return coord;
    }

    @Nullable
    @Override
    public String getName() {
        return name;
    }

    @Override
    public float getElevation() {
        return elevation;
    }

    public void setGeocode(final String geocode) {
        this.geocode = geocode;
    }

    @NonNull
    public String getGeocode() {
        return geocode == null ? "" : geocode;
    }



    //hashCode / equals

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final SimpleGeoObject that = (SimpleGeoObject) o;
        return Float.compare(elevation, that.elevation) == 0 &&
                Objects.equals(coord, that.coord) &&
                Objects.equals(name, that.name) &&
                Objects.equals(geocode, that.geocode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(coord, name, elevation);
    }

    //Percelable

    public static final Parcelable.Creator<SimpleGeoObject> CREATOR = new Parcelable.Creator<SimpleGeoObject>() {

        @Override
        public SimpleGeoObject createFromParcel(final Parcel source) {
            return new SimpleGeoObject(source);
        }

        @Override
        public SimpleGeoObject[] newArray(final int size) {
            return new SimpleGeoObject[size];
        }

    };

    private SimpleGeoObject(final Parcel parcel) {
        coord = parcel.readParcelable(SimpleGeoObject.class.getClassLoader());
        name = parcel.readString();
        elevation = parcel.readFloat();
        geocode = parcel.readString();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull final Parcel dest, final int flags) {
        dest.writeParcelable(coord, flags);
        dest.writeString(name);
        dest.writeFloat(elevation);
        dest.writeString(geocode);
    }

}
