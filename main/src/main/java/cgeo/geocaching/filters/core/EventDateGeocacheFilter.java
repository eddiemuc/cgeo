package cgeo.geocaching.filters.core;

import cgeo.geocaching.models.Geocache;

import java.util.Date;

/**
 * Filter for event dates.
 * Note: This currently uses the same implementation as HiddenGeocacheFilter since events
 * store their date in the 'hidden' field. However, this separate class exists for semantic
 * clarity and to allow for future enhancements specific to event filtering (e.g., filtering
 * by event start/end times, which are stored separately from the hidden date).
 */
public class EventDateGeocacheFilter extends DateRangeGeocacheFilter {

    @Override
    protected Date getDate(final Geocache cache) {
        return cache.getHiddenDate();
    }

    @Override
    protected String getSqlColumnName() {
        return "hidden";
    }
}
