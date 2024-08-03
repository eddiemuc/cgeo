package cgeo.geocaching.utils;

import cgeo.org.kxml2.io.KXmlSerializer;

import android.sax.Element;
import android.sax.RootElement;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.Attributes;
import org.xmlpull.v1.XmlSerializer;
import static org.assertj.core.api.Java6Assertions.assertThat;

public class XmlUtilsTest {

    private XmlSerializer xml;

    private StringWriter stringWriter;

    @Before
    public void setUp() throws Exception {
        stringWriter = new StringWriter();
        xml = new KXmlSerializer();
        xml.setOutput(stringWriter);
        xml.startDocument(StandardCharsets.UTF_8.name(), null);
    }

    @Test
    public void testSimpleText() throws Exception {
        XmlUtils.simpleText(xml, "", "tag", "text");
        assertXmlEquals("<tag>text</tag>");
    }

    @Test
    public void testSimpleTextWithPrefix() throws Exception {
        XmlUtils.simpleText(xml, "prefix", "tag", "text");
        assertXmlEquals("<n0:tag xmlns:n0=\"prefix\">text</n0:tag>");
    }

    @Test
    public void testXmlParseIgnoringNamespaces() {
        final String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n<gpx xmlns=\"nsgpx\" xmlns:gc=\"nsgc\" creator=\"eddie\"><gc:coord lat=\"1\" lon=\"2\"></gc:coord><name>myname</name></gpx>";
        final InputStream xmlStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        final StringBuilder sb = new StringBuilder();

        final RootElement gpx = new RootElement("gpx");
        addDefaultListeners(gpx, "gpx", sb, false);

        addDefaultListeners(gpx.getChild("coord"), "coord", sb, true);
        addDefaultListeners(gpx.getChild("name"), "name", sb, true);

        XmlUtils.parseXml("test", new InputStreamReader(xmlStream, StandardCharsets.UTF_8), gpx.getContentHandler(), true, false, true);
        assertThat(sb.toString()).isEqualTo("<gpx{creator=eddie;}><coord{lat=1;lon=2;}>##</coord><name{}>#myname#</name></gpx>");
    }

    private static void addDefaultListeners(final Element element, final String name, final StringBuilder sb, final boolean addTextListener) {
        element.setStartElementListener(atts -> sb.append("<").append(name).append("{").append(attToString(atts)).append("}>"));
        if (addTextListener) {
            element.setEndTextElementListener(text -> sb.append("#").append(text).append("#</").append(name).append(">"));
        } else {
            element.setEndElementListener(() -> sb.append("</").append(name).append(">"));
        }
    }

    private static String attToString(final Attributes atts) {
        final StringBuilder sb = new StringBuilder();
        for(int i=0; i<atts.getLength();i++) {
            sb.append(atts.getQName(i)).append("=").append(atts.getValue(i)).append(";");
        }
        return sb.toString();

    }

    private void assertXmlEquals(final String expected) throws IOException {
        xml.endDocument();
        xml.flush();
        assertThat(stringWriter.toString()).isEqualTo("<?xml version='1.0' encoding='UTF-8' ?>" + expected);
    }

    @Test
    public void testMultipleTexts() throws Exception {
        XmlUtils.multipleTexts(xml, "", "tag1", "text1", "tag2", "text2");
        assertXmlEquals("<tag1>text1</tag1><tag2>text2</tag2>");
    }

    @Test
    public void testSkipIllegalChars() throws Exception {
        XmlUtils.simpleText(xml, "", "tag", "Vom\u0001 Gasthaus\u000f zur \u000bPyramide\u0020aus \u0018Glas\u0009");
        assertXmlEquals("<tag>Vom Gasthaus zur Pyramide\u0020aus Glas\u0009</tag>");
    }

}
