package cgeo.geocaching.filters;

import cgeo.geocaching.models.Geocache;

public class OrGeocacheFilter extends LogicalGeocacheFilter {

    @Override
    public String getId() {
        return "OR";
    }

    @Override
    public Boolean filter(final Geocache cache) {
        boolean isInconclusive = false;
//        for (IGeocacheFilter child : getChildren()) {
//            final Boolean childResult = child.filter(cache);
//            if (childResult == null) {
//                isInconclusive = true;
//            } else if (childResult) {
//                return true;
//            }
//        }
        return isInconclusive ? null : false;
    }
}
