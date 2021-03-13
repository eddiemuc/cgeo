package cgeo.geocaching.filters;

import cgeo.geocaching.models.Geocache;

public abstract class StringGeocacheFilter extends AbstractGeocacheFilter {

    private String value;

    public StringGeocacheFilter(final String typeId) {
        super(typeId);
    }

    protected abstract String getValue(Geocache cache);

    @Override
    public Boolean filter(final Geocache cache) {
        if (cache == null) {
            return null;
        }
        final String gcValue = getValue(cache);
        if (gcValue == null) {
            return null;
        }
        return gcValue.contains(value);
    }

    @Override
    public void setConfig(final String value) {
        this.value = value;
    }

    @Override
    public String getConfig() {
        return value;
    }
}
