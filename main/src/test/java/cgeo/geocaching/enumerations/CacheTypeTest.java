package cgeo.geocaching.enumerations;

import cgeo.geocaching.models.Geocache;

import java.util.Locale;

import org.junit.Test;
import static org.assertj.core.api.Java6Assertions.assertThat;

public class CacheTypeTest {

    @Test
    public void testGetById() {
        assertThat(CacheType.getById("")).isEqualTo(CacheType.UNKNOWN);
        assertThat(CacheType.getById(null)).isEqualTo(CacheType.UNKNOWN);
        assertThat(CacheType.getById("random garbage")).isEqualTo(CacheType.UNKNOWN);
        assertThat(CacheType.getById("wherigo")).isEqualTo(CacheType.WHERIGO);
    }

    @Test
    public void testGetByGpxPattern() {
        assertThat(CacheType.getByGpxPattern("")).isEqualTo(CacheType.UNKNOWN);
        assertThat(CacheType.getByGpxPattern(null)).isEqualTo(CacheType.UNKNOWN);
        assertThat(CacheType.getByGpxPattern("random garbage")).isEqualTo(CacheType.UNKNOWN);
        assertThat(CacheType.getByGpxPattern("cache in trash out event")).isEqualTo(CacheType.CITO);
        assertThat(CacheType.getByGpxPattern("Locationless (Reverse) Cache")).isEqualTo(CacheType.LOCATIONLESS);
    }

    @Test
    public void testGetByIdComplete() {
        for (final CacheType type : CacheType.values()) {
            assertThat(CacheType.getById(type.id)).isEqualTo(type);
            assertThat(CacheType.getById(type.id.toLowerCase(Locale.US))).isEqualTo(type);
            assertThat(CacheType.getById(type.id.toUpperCase(Locale.US))).isEqualTo(type);
        }
    }

    @Test
    public void testGetByGpxPatternComplete() {
        for (final CacheType type : CacheType.values()) {
            assertThat(CacheType.getByGpxPattern(type.pattern)).isEqualTo(type);
            assertThat(CacheType.getByGpxPattern(type.pattern.toLowerCase(Locale.US))).isEqualTo(type);
            assertThat(CacheType.getByGpxPattern(type.pattern.toUpperCase(Locale.US))).isEqualTo(type);
        }
    }

    @Test
    public void testContainsCache() {
        final Geocache traditional = new Geocache();
        traditional.setType(CacheType.TRADITIONAL);

        assertThat(CacheType.ALL.contains(traditional)).isTrue();
        assertThat(CacheType.TRADITIONAL.contains(traditional)).isTrue();
        assertThat(CacheType.MYSTERY.contains(traditional)).isFalse();
    }

    @Test
    public void testEventCacheTypes() throws Exception {
        assertThat(CacheType.EVENT.isEvent()).isTrue();
        assertThat(CacheType.MEGA_EVENT.isEvent()).isTrue();
        assertThat(CacheType.GIGA_EVENT.isEvent()).isTrue();
        assertThat(CacheType.BLOCK_PARTY.isEvent()).isTrue();
        assertThat(CacheType.CITO.isEvent()).isTrue();
        assertThat(CacheType.COMMUN_CELEBRATION.isEvent()).isTrue();
        assertThat(CacheType.GPS_EXHIBIT.isEvent()).isTrue();
        assertThat(CacheType.GCHQ_CELEBRATION.isEvent()).isTrue();
        assertThat(CacheType.TRADITIONAL.isEvent()).isFalse();
        assertThat(CacheType.LOCATIONLESS.isEvent()).isFalse();
    }
}
