package cgeo.geocaching.filters;

import java.util.List;

public abstract class AbstractGeocacheFilter implements IGeocacheFilter {

    private String typeId;

    public AbstractGeocacheFilter(final String typeId) {
        this.typeId = typeId;
    }

    @Override
    public String getTypeId() {
        return typeId;
    }


    @Override
    public void addChild(final IGeocacheFilter child) {
        //no children

    }

    @Override
    public List<IGeocacheFilter> getChildren() {
        //no children
        return null;
    }
}
