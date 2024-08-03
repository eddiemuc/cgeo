package cgeo.geocaching.utils;

import cgeo.geocaching.files.InvalidXMLCharacterFilterReader;

import android.util.Xml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xmlpull.v1.XmlSerializer;

public final class XmlUtils {

    private XmlUtils() {
        // Do not instantiate
    }

    /**
     * Insert an attribute-less tag with enclosed text in a XML serializer output.
     *
     * @param serializer an XML serializer
     * @param prefix     an XML prefix, see {@link XmlSerializer#startTag(String, String)}
     * @param tag        an XML tag
     * @param text       some text to insert, or <tt>null</tt> to omit completely this tag
     */
    public static void simpleText(final XmlSerializer serializer, final String prefix, final String tag, final String text) throws IOException {
        if (text != null) {
            serializer.startTag(prefix, tag);
            serializer.text(text);
            serializer.endTag(prefix, tag);
        }
    }

    /**
     * Insert pairs of attribute-less tags and enclosed texts in a XML serializer output
     *
     * @param serializer an XML serializer
     * @param prefix     an XML prefix, see {@link XmlSerializer#startTag(String, String)} shared by all tags
     * @param tagAndText an XML tag, the corresponding text, another XML tag, the corresponding text. <tt>null</tt> texts
     *                   will be omitted along with their respective tag.
     */
    public static void multipleTexts(final XmlSerializer serializer, final String prefix, final String... tagAndText) throws IOException {
        for (int i = 0; i < tagAndText.length; i += 2) {
            simpleText(serializer, prefix, tagAndText[i], tagAndText[i + 1]);
        }
    }

    public static void parseXml(final String xmlName, final Reader xmlReader, final ContentHandler contentHandler, final boolean ignoreNamespaces, final boolean failQuietly, final boolean closeStream) throws IllegalStateException {
        Reader reader = null;
        try {
            reader = new InvalidXMLCharacterFilterReader(xmlReader);

            Xml.parse(reader, ignoreNamespaces ? new NamespaceMaskingHandler(contentHandler) : contentHandler);
        } catch (IOException | SAXException e) {
            final String message = "XMLUtils: problem parsing XML stream '" + xmlName + "'";
            Log.w(message, e);
            if (!failQuietly) {
                throw new IllegalStateException(message, e);
            }
        } finally {
            if (closeStream) {
                IOUtils.closeQuietly(reader);
            }
        }
    }

    /** A contenthandler delegate which masks parsed namespace Uris from the delegate - thus effectively blocking namespace processing */
    private static class NamespaceMaskingHandler extends DefaultHandler {

        private final ContentHandler delegate;

        public NamespaceMaskingHandler(final ContentHandler delegate) {
            this.delegate = delegate;
        }

        @Override
        public void setDocumentLocator(Locator locator) {
            delegate.setDocumentLocator(locator);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            delegate.startElement("", localName, qName, attributes);
        }

        @Override
        public void characters(char[] buffer, int start, int length) throws SAXException {
            delegate.characters(buffer, start, length);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            delegate.endElement("", localName, qName);
        }
    }
}
