package cgeo.geocaching.filters;

import cgeo.geocaching.models.Geocache;

public  class OwnerGeocacheFilter extends StringGeocacheFilter {

    public OwnerGeocacheFilter() {
        super("owner");
    }

    public String getValue(final Geocache cache) {
        return cache.getOwnerDisplayName();
    }

}
