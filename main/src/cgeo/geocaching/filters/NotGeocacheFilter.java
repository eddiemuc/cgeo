package cgeo.geocaching.filters;

import cgeo.geocaching.models.Geocache;

public class NotGeocacheFilter extends AndGeocacheFilter {

    @Override
    public String getTypeId() {
        return "NOT";
    }

    @Override
    public Boolean filter(final Geocache cache) {
        final Boolean superResult = super.filter(cache);
        return superResult == null ? null : !superResult;
    }

}
