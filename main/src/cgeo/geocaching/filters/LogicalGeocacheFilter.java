package cgeo.geocaching.filters;

public abstract class LogicalGeocacheFilter implements IGeocacheFilter {

    private IGeocacheFilter childLeft = null;
    private IGeocacheFilter childRight = null;

    @Override
    public void setConfig(final String value) {
        //logical filter has no config
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Override
    public void addChildren(final IGeocacheFilter left, final IGeocacheFilter right) {
        this.childLeft = left;
        this.childRight = right;
    }

    @Override
    public IGeocacheFilter getChildLeft() {
        return childLeft;
    }

    @Override
    public IGeocacheFilter getChildRight() {
        return childRight;
    }


}
