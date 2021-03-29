package cgeo.geocaching.filters;

public abstract class AbstractGeocacheFilter implements IGeocacheFilter {

    private String typeId;

    public AbstractGeocacheFilter(final String typeId) {
        this.typeId = typeId;
    }

    @Override
    public String getId() {
        return typeId;
    }

    @Override
    public void addChildren(final IGeocacheFilter left, final IGeocacheFilter right) {

    }

    @Override
    public IGeocacheFilter getChildLeft() {
        return null;
    }

    @Override
    public IGeocacheFilter getChildRight() {
        return null;
    }


}
