package cgeo.geocaching.filters;

import cgeo.geocaching.models.Geocache;
import cgeo.geocaching.utils.expressions.IExpression;

public interface IGeocacheFilter extends IExpression<IGeocacheFilter> {

    Boolean filter(Geocache cache);

}
