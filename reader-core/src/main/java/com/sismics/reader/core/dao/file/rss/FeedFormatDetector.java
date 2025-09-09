package com.sismics.reader.core.dao.file.rss;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.sismics.reader.core.util.StreamUtil;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

/**
 * Detects the feed format (RSS, Atom, RDF) from the XML input.
 */
public class FeedFormatDetector {

    private String rootElement=null;
    private String namespace=null;

    public String[] detectFormat(InputStream is) throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setFeature("http://apache.org/xml/features/continue-after-fatal-error", true);
        factory.setNamespaceAware(true);
        SAXParser saxParser = factory.newSAXParser();
        
        Reader reader = new XmlReader(StreamUtil.detectGzip(is), "UTF-8");
        InputSource source = new InputSource(reader);
        saxParser.parse(source, new DefaultHandler() {
            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                // rootElement = localName;
                if(rootElement==null){
                    rootElement = localName;
                    namespace = uri;
                    // throw new SAXException("Stop Parsing"); // Stop parsing after the first element
                }
            }
        });

        return new String[]{rootElement, namespace};
    }
}
