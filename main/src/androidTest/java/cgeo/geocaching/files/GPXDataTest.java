package cgeo.geocaching.files;

import cgeo.geocaching.location.Geopoint;
import cgeo.geocaching.test.CgeoTestUtils;

import androidx.annotation.RawRes;

import java.io.InputStream;

import org.junit.Test;
import static org.assertj.core.api.Java6Assertions.assertThat;

public class GPXDataTest {

    @Test
    public void singleWaypoint()  {
        final GPXData gpxData = parse(cgeo.geocaching.test.R.raw.gc31j2h2_bad_cacheid);
        assertThat(gpxData.getWptSeries().getPoints()).hasSize(1);
        //lat="49.3187" lon="8.54565"
        assertThat(gpxData.getWptSeries().getPoints().get(0).getCoords()).isEqualTo(new Geopoint(49.3187, 8.54565));
        assertThat(gpxData.getRouteSeries()).isEmpty();
        assertThat(gpxData.getTrackSeries()).isEmpty();
    }

    @Test
    public void routeAndTrack()  {
        final GPXData gpxData = parse(cgeo.geocaching.test.R.raw.uberruhr_public);

        assertThat(gpxData.getWptSeries().getPoints()).hasSize(10);
        //assertThat(gpxPointData.getWptSeries().getElevations()).hasSize(10);
        assertThat(gpxData.getWptSeries().getName()).isNull();

        assertThat(gpxData.getRouteSeries()).hasSize(1);
        final GPXData.GPXPointSeries route = gpxData.getRouteSeries().get(0);
        assertThat(route.getPoints()).hasSize(1);
        //assertThat(route.getElevations()).hasSize(1);
        assertThat(route.getName()).isEqualTo("002 bis 002");

        assertThat(gpxData.getTrackSeries()).hasSize(1);
        final GPXData.GPXPointSeries track = gpxData.getTrackSeries().get(0);
        assertThat(track.getPoints()).hasSize(285);
        //assertThat(track.getElevations()).hasSize(285);
        assertThat(track.getName()).isEqualTo("Überruhr");
    }

    @Test
    public void readTerracaching()  {
        final GPXData gpxData = parse(cgeo.geocaching.test.R.raw.terracaching_gpx);
        assertThat(gpxData.getWptSeries().getPoints()).hasSize(55);
        //lat="53.4112333333333" lon="7.4628"
        assertThat(gpxData.getWptSeries().getPoints().get(0).getCoords()).isEqualTo(new Geopoint(53.4112333333333, 7.4628));
        assertThat(gpxData.getRouteSeries()).isEmpty();
        assertThat(gpxData.getTrackSeries()).isEmpty();
    }

    @Test
    public void readOpencaching()  {
        final GPXData gpxData = parse(cgeo.geocaching.test.R.raw.oc120f5_gpx);
        assertThat(gpxData.getWptSeries().getPoints()).hasSize(55);
        //lat="53.4112333333333" lon="7.4628"
        assertThat(gpxData.getWptSeries().getPoints().get(0).getCoords()).isEqualTo(new Geopoint(53.4112333333333, 7.4628));
        assertThat(gpxData.getRouteSeries()).isEmpty();
        assertThat(gpxData.getTrackSeries()).isEmpty();
    }

    @Test
    public void readModifiedGsak()  {
        final GPXData gpxData = parse(cgeo.geocaching.test.R.raw.modified_gsak);
        assertThat(gpxData.getWptSeries().getPoints()).hasSize(55);
        //lat="53.4112333333333" lon="7.4628"
        assertThat(gpxData.getWptSeries().getPoints().get(0).getCoords()).isEqualTo(new Geopoint(53.4112333333333, 7.4628));
        assertThat(gpxData.getRouteSeries()).isEmpty();
        assertThat(gpxData.getTrackSeries()).isEmpty();
    }


    private GPXData parse(@RawRes final int resourceId) {
        final InputStream instream = CgeoTestUtils.getResourceStream(resourceId);
        return GPXData.parse("test", instream);
    }
}
