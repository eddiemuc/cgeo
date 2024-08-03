package cgeo.geocaching.models;

import cgeo.geocaching.location.Geopoint;
import cgeo.geocaching.maps.routing.Routing;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class RouteSegment implements Parcelable {
    private final RouteItem item;
    private float distance;
    private ArrayList<Geopoint> points;
    private ArrayList<Float> elevation;
    private boolean linkToPreviousSegment = true;
    private final boolean routable;

    public RouteSegment(final RouteItem item, final ArrayList<Geopoint> points, final boolean linkToPreviousSegment, final boolean routable) {
        this(item, points, linkToPreviousSegment, routable, null);
    }

    public RouteSegment(final RouteItem item, final ArrayList<Geopoint> points, final boolean linkToPreviousSegment, final boolean routable, final ArrayList<Float> elevation) {
        this.item = item;
        distance = 0.0f;
        this.points = points;
        this.linkToPreviousSegment = linkToPreviousSegment;
        this.elevation = elevation;
        this.routable = routable;
    }

    public float calculateDistance() {
        distance = 0.0f;
        if (points.size() > 0) {
            Geopoint last = points.get(0);
            for (Geopoint point : points) {
                distance += last.distanceTo(point);
                last = point;
            }
        }
        return distance;
    }

    public RouteItem getItem() {
        return item;
    }

    public float getDistance() {
        return distance;
    }

    public ArrayList<Geopoint> getPoints() {
        if (null == points || points.size() == 0) {
            this.points = new ArrayList<>();
            final Geopoint point = item.getPoint();
            if (null != point) {
                this.points.add(point);
            }
        }
        return points;
    }

    public int getSize() {
        return points.size();
    }

    public Geopoint getPoint() {
        return item.getPoint();
    }

    public boolean hasPoint() {
        return null != item.getPoint();
    }

    public void addPoint(final Geopoint geopoint) {
        addPoint(geopoint, Float.NaN);
    }

    public void addPoint(final Geopoint geopoint, final float elevation) {
        if (this.elevation != null && this.elevation.size() == points.size()) {
            this.elevation.add(elevation);
        }
        points.add(geopoint);
    }

    public void recalculateRoute(final Geopoint fromPoint) {
        if (!routable || item == null || item.getPoint() == null || fromPoint == null) {
            return;
        }
        // clear info for current segment
        resetPoints();
        // calculate route for segment between current point and its predecessor
        final ArrayList<Float> elevation = new ArrayList<>();
        final Geopoint[] temp = Routing.getTrackNoCaching(fromPoint, item.getPoint(), elevation);
        for (Geopoint geopoint : temp) {
            addPoint(geopoint);
        }
        setElevation(elevation);
    }

    private void resetPoints() {
        points = new ArrayList<>();
        elevation = new ArrayList<>();
        distance = 0.0f;
    }

    private void setElevation(final ArrayList<Float> elevation) {
        this.elevation.clear();
        this.elevation.addAll(elevation);
    }

    public ArrayList<Float> getElevation() {
        return elevation;
    }

    public boolean getLinkToPreviousSegment() {
        return linkToPreviousSegment;
    }

    // Parcelable methods

    public static final Parcelable.Creator<RouteSegment> CREATOR = new Parcelable.Creator<RouteSegment>() {

        @Override
        public RouteSegment createFromParcel(final Parcel source) {
            return new RouteSegment(source);
        }

        @Override
        public RouteSegment[] newArray(final int size) {
            return new RouteSegment[size];
        }

    };

    private RouteSegment(final Parcel parcel) {
        item = parcel.readParcelable(RouteItem.class.getClassLoader());
        distance = parcel.readFloat();
        points = parcel.readArrayList(Geopoint.class.getClassLoader());
        elevation = parcel.readArrayList(Float.TYPE.getClassLoader());
        routable = parcel.readInt() != 0;
        linkToPreviousSegment = parcel.readInt() != 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeParcelable(item, flags);
        dest.writeFloat(distance);
        dest.writeList(points);
        dest.writeList(elevation);
        dest.writeInt(routable ? 1 : 0);
        dest.writeInt(linkToPreviousSegment ? 1 : 0);
    }

}
