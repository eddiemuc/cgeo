package cgeo.geocaching.utils;

import cgeo.geocaching.utils.functions.Func1;
import cgeo.org.kxml2.io.KXmlSerializer;

import android.util.Pair;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;
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

    @Test
    public void calculateDrawableStatistics() {
        final Map<String, List<String>> fileMap = new HashMap<>();
        final File root = new File("C:/private/geocaching/github/cgeo/main/res");
        calcStats(root, root, Pattern.compile("^drawable-.*.png$"), dir -> dir.substring(9), fileMap);

        final Map<String, List<String>> dircombiMap = new HashMap<>();
        final Map<String, Integer> dircombiCountMap = new HashMap<>();
        for (Map.Entry<String, List<String>> e : fileMap.entrySet()) {
            final List<String> sorted = new ArrayList<>(e.getValue());
            Collections.sort(sorted);
            final String dircombi = CollectionStream.of(sorted).toJoinedString("-");

            if (!dircombiMap.containsKey(dircombi)) {
                dircombiMap.put(dircombi, new ArrayList<>());
                dircombiCountMap.put(dircombi, e.getValue().size());
            }
            dircombiMap.get(dircombi).add(e.getKey());
        }

        //stats print
        System.out.println(fileMap.size() + " different png-images in total");
        for (int i = 20; i >= 0; i--) {
            boolean first = true;
            for (String key : dircombiMap.keySet()) {
                if (dircombiCountMap.get(key) == i) {
                    if (first) {
                        System.out.println("Images with copies in " + i + " drawable-directories:");
                        first = false;
                    }
                    final List<String> files = dircombiMap.get(key);
                    System.out.println(" Dirs: " + key + ": (" + files.size() + ") " + CollectionStream.of(files).toJoinedString(","));
                }
            }
        }
    }

    private void calcStats(final File root, final File candidate, final Pattern filePattern, final Func1<String, String> dirMapper, final Map<String, List<String>> fileMap) {
        if (candidate.isFile()) {
            String relPath = candidate.getAbsolutePath().substring(root.getAbsolutePath().length()+1);
            if (filePattern.matcher(relPath).matches()) {
                String fileName = candidate.getName();
                String fileDir = relPath.substring(0, relPath.length() - fileName.length() - 1);
                if (dirMapper!=null) {
                    fileDir = dirMapper.call(fileDir);
                }
                if (!fileMap.containsKey(fileName)) {
                    fileMap.put(fileName, new ArrayList<>());
                }
                fileMap.get(fileName).add(fileDir);
            }
        }
        if (candidate.isDirectory()) {
            for (File child : candidate.listFiles()) {
                calcStats(root, child, filePattern, dirMapper, fileMap);
            }
        }
    }

}
