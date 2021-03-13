package cgeo.geocaching.filters;

import cgeo.geocaching.utils.expressions.ExpressionParser;

import androidx.annotation.NonNull;

import java.text.ParseException;

public class GeocacheFilterUtils {

    private static final ExpressionParser<IGeocacheFilter> FILTER_PARSER = new ExpressionParser<IGeocacheFilter>()
        .register(() -> new AndGeocacheFilter())
        .register(() -> new OrGeocacheFilter())
        .register(() -> new NotGeocacheFilter())
        .register(() -> new NameGeocacheFilter())
        .register(() -> new OwnerGeocacheFilter());

    private GeocacheFilterUtils() {
        //no instane shall be created
    }

    public static IGeocacheFilter createFilter(@NonNull final String filterConfig) throws ParseException {
        return FILTER_PARSER.create(filterConfig);
    }

    public static String getFilterConfig(@NonNull final IGeocacheFilter filter) {
        return FILTER_PARSER.getConfig(filter);
    }

}
