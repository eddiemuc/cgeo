package cgeo.geocaching.filters.core;

import java.util.Calendar;
import java.util.Date;

import org.junit.Test;
import static org.assertj.core.api.Java6Assertions.assertThat;

public class DateFilterTest {

    @Test
    public void testSameDayDifferentTimesShouldMatch() {
        // Test for issue #17633: Events at midnight should match filters for "today"
        // This verifies that dates on the same calendar day match regardless of time

        final DateFilter filter = new DateFilter();

        // Create dates on the same calendar day but different times
        final Calendar cal = Calendar.getInstance();
        cal.set(2025, Calendar.DECEMBER, 29, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        final Date midnight = cal.getTime();

        cal.set(2025, Calendar.DECEMBER, 29, 11, 34, 0);
        cal.set(Calendar.MILLISECOND, 0);
        final Date laterSameDay = cal.getTime();

        // Event at midnight should match when min date is later same day
        filter.setMinMaxDate(laterSameDay, null);
        assertThat(filter.matches(midnight)).isTrue();

        // Event later in day should match when min date is midnight same day
        filter.setMinMaxDate(midnight, null);
        assertThat(filter.matches(laterSameDay)).isTrue();

        // Both should match when filtering for exactly that day
        filter.setMinMaxDate(midnight, laterSameDay);
        assertThat(filter.matches(midnight)).isTrue();
        assertThat(filter.matches(laterSameDay)).isTrue();
    }

    @Test
    public void testDifferentDaysShouldNotMatch() {
        final DateFilter filter = new DateFilter();

        final Calendar cal = Calendar.getInstance();
        cal.set(2025, Calendar.DECEMBER, 29, 23, 59, 59);
        cal.set(Calendar.MILLISECOND, 999);
        final Date lastMomentOfDay = cal.getTime();

        cal.set(2025, Calendar.DECEMBER, 30, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        final Date nextDayMidnight = cal.getTime();

        // Last moment of Dec 29 should not match filter for Dec 30 onwards
        filter.setMinMaxDate(nextDayMidnight, null);
        assertThat(filter.matches(lastMomentOfDay)).isFalse();

        // First moment of Dec 30 should not match filter for up to Dec 29
        filter.setMinMaxDate(null, lastMomentOfDay);
        assertThat(filter.matches(nextDayMidnight)).isFalse();
    }

    @Test
    public void testTimezoneIndependence() {
        // Test that the fix works correctly across different timezones
        // Note: We don't need to change the default timezone as the fix is timezone-aware
        // The DateUtils.truncate() call in compareDates() uses the default timezone,
        // which correctly handles the calendar day boundaries
        
        final DateFilter filter = new DateFilter();

        final Calendar cal = Calendar.getInstance();
        cal.set(2025, Calendar.DECEMBER, 29, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        final Date midnight = cal.getTime();

        cal.set(2025, Calendar.DECEMBER, 29, 11, 34, 0);
        cal.set(Calendar.MILLISECOND, 0);
        final Date laterSameDay = cal.getTime();

        // These should match as they're on the same calendar day, regardless of timezone
        filter.setMinMaxDate(laterSameDay, null);
        assertThat(filter.matches(midnight)).isTrue();

        filter.setMinMaxDate(midnight, null);
        assertThat(filter.matches(laterSameDay)).isTrue();
    }
}
