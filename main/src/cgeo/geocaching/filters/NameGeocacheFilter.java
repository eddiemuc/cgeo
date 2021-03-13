package cgeo.geocaching.filters;

import cgeo.geocaching.models.Geocache;

public  class NameGeocacheFilter extends StringGeocacheFilter {

    public NameGeocacheFilter() {
        super("name");
    }

    public String getValue(final Geocache cache) {
        return cache.getName();
    }

}
