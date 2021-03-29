package cgeo.geocaching.filters;

import cgeo.geocaching.models.Geocache;

public class AndGeocacheFilter extends LogicalGeocacheFilter {

    @Override
    public String getId() {
        return "AND";
    }

    @Override
    public Boolean filter(final Geocache cache) {
        boolean isInconclusive = false;
//        for (IGeocacheFilter child : getChildren()) {
//            final Boolean childResult = child.filter(cache);
//            if (childResult == null) {
//                isInconclusive = true;
//            } else if (!childResult) {
//                return false;
//            }
//        }
        return isInconclusive ? null : true;
    }

}
