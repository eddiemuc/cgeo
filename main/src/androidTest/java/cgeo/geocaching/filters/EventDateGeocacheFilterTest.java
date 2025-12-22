package cgeo.geocaching.filters;

import cgeo.geocaching.filters.core.EventDateGeocacheFilter;
import cgeo.geocaching.filters.core.GeocacheFilterType;
import cgeo.geocaching.models.Geocache;
import cgeo.geocaching.utils.functions.Action1;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;
import static org.assertj.core.api.Java6Assertions.assertThat;

/**
 * Tests for EventDateGeocacheFilter with focus on relative date filtering
 */
public class EventDateGeocacheFilterTest {

    private static final DateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    @Test
    public void testRelativeDateToday() {
        // Test that events happening "today" are included when filtering from "0T" (today)
        final Date today = DateUtils.truncate(new Date(), Calendar.DATE);
        final Date eventEarlierToday = new Date(today.getTime() + 3 * 60 * 60 * 1000); // 3 hours from start of day
        final Date eventLaterToday = new Date(today.getTime() + 20 * 60 * 60 * 1000); // 20 hours from start of day

        // Create filter for "today and future" (0T - >1M)
        final EventDateGeocacheFilter filter = GeocacheFilterType.EVENT_DATE.create();
        filter.setRelativeMinMaxDays(0, 30);

        // Create test caches with events today
        final Geocache cache1 = createCacheWithEventDate(eventEarlierToday);
        final Geocache cache2 = createCacheWithEventDate(eventLaterToday);

        // Both should match
        assertThat(filter.filter(cache1)).as("Event earlier today should be included").isTrue();
        assertThat(filter.filter(cache2)).as("Event later today should be included").isTrue();
    }

    @Test
    public void testRelativeDateTodayExcludesYesterday() {
        // Test that events from yesterday are excluded when filtering from "0T" (today)
        final Date today = DateUtils.truncate(new Date(), Calendar.DATE);
        final Date yesterday = DateUtils.addDays(today, -1);

        // Create filter for "today and future" (0T - >1M)
        final EventDateGeocacheFilter filter = GeocacheFilterType.EVENT_DATE.create();
        filter.setRelativeMinMaxDays(0, 30);

        // Create test cache with event yesterday
        final Geocache cache = createCacheWithEventDate(yesterday);

        // Should not match
        assertThat(filter.filter(cache)).as("Event yesterday should be excluded").isFalse();
    }

    @Test
    public void testRelativeDateTomorrow() {
        // Test that events happening tomorrow are included when filtering from "0T" (today)
        final Date today = DateUtils.truncate(new Date(), Calendar.DATE);
        final Date tomorrow = DateUtils.addDays(today, 1);

        // Create filter for "today and future" (0T - >1M)
        final EventDateGeocacheFilter filter = GeocacheFilterType.EVENT_DATE.create();
        filter.setRelativeMinMaxDays(0, 30);

        // Create test cache with event tomorrow
        final Geocache cache = createCacheWithEventDate(tomorrow);

        // Should match
        assertThat(filter.filter(cache)).as("Event tomorrow should be included").isTrue();
    }

    @Test
    public void testRelativeDatePast() {
        // Test that past events are excluded when filtering from "0T" (today)
        final Date today = DateUtils.truncate(new Date(), Calendar.DATE);
        final Date lastWeek = DateUtils.addDays(today, -7);

        // Create filter for "today and future" (0T - >1M)
        final EventDateGeocacheFilter filter = GeocacheFilterType.EVENT_DATE.create();
        filter.setRelativeMinMaxDays(0, 30);

        // Create test cache with event last week
        final Geocache cache = createCacheWithEventDate(lastWeek);

        // Should not match
        assertThat(filter.filter(cache)).as("Past event should be excluded").isFalse();
    }

    @Test
    public void testRelativeDatePastToToday() {
        // Test filtering for past events up to and including today
        final Date today = DateUtils.truncate(new Date(), Calendar.DATE);
        final Date yesterday = DateUtils.addDays(today, -1);
        final Date eventToday = new Date(today.getTime() + 10 * 60 * 60 * 1000); // 10 hours from start of day

        // Create filter for "past to today" (past - 0T)
        final EventDateGeocacheFilter filter = GeocacheFilterType.EVENT_DATE.create();
        filter.setRelativeMinMaxDays(-100000, 0);

        // Create test caches
        final Geocache cacheToday = createCacheWithEventDate(eventToday);
        final Geocache cacheYesterday = createCacheWithEventDate(yesterday);

        // Both should match
        assertThat(filter.filter(cacheToday)).as("Event today should be included in past-to-today filter").isTrue();
        assertThat(filter.filter(cacheYesterday)).as("Event yesterday should be included in past-to-today filter").isTrue();
    }

    private Geocache createCacheWithEventDate(final Date eventDate) {
        final Geocache cache = new Geocache();
        cache.setHidden(eventDate);
        return cache;
    }
}
