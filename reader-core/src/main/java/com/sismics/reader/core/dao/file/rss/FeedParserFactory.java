// package com.sismics.reader.core.dao.file.rss;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import javax.xml.parsers.SAXParserFactory;
// // import java.io.InputStream;
// import java.util.Map;
// import java.util.concurrent.ConcurrentHashMap;

// public class FeedParserFactory {
//     private static final Logger log = LoggerFactory.getLogger(FeedParserFactory.class);
//     private static final Map<FeedFormat, FeedParser> parsers = new ConcurrentHashMap<>();

//     static {
//         parsers.put(FeedFormat.RSS, new RssFeedParser());
//         parsers.put(FeedFormat.ATOM, new AtomFeedParser());
//         parsers.put(FeedFormat.RDF, new RdfFeedParser());
//     }

//     public static FeedParser getParser(FeedFormat format) throws ParseException {
//         FeedParser parser = parsers.get(format);
//         if (parser == null) {
//             throw new ParseException("Unsupported feed format: " + format);
//         }
//         return parser;
//     }

    // public static SAXParserFactory createSecureParserFactory() throws ParseException {
    //     try {
    //         SAXParserFactory factory = SAXParserFactory.newInstance();
    //         factory.setNamespaceAware(true);
    //         factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    //         factory.setFeature("http://apache.org/xml/features/continue-after-fatal-error", true);
    //         return factory;
    //     } catch (Exception e) {
    //         throw new ParseException("Failed to create secure SAX parser factory", e);
    //     }
    // }
// }

package com.sismics.reader.core.dao.file.rss;

import com.sismics.reader.core.model.jpa.Feed;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import java.io.InputStream;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.StringReader;

public class FeedParserFactory {
    
    public static ВaseParser getParser(InputStream is) throws ParseException {
        try {
            // Step 1: Detect root element and namespace
            FeedFormatDetector detector = new FeedFormatDetector();
            String[] formatInfo = detector.detectFormat(is);
            String rootElement = formatInfo[0];
            String namespace = formatInfo[1];

            // Step 2: Get the appropriate parser
            ВaseParser parser = getParser(rootElement, namespace);
            // parser.parse(is);

            // Step 3: Return the parsed feed
            // return parser.getFeed();
            return parser;
        } catch (Exception e) {
            throw new ParseException("Failed to parse feed", e);
        }
    }

    private static ВaseParser getParser(String rootElement, String namespace) {
        if ("rss".equalsIgnoreCase(rootElement)) {
            System.out.println("SKIBDI RSS");
            return new RssFeedParser();
        } else if ("feed".equalsIgnoreCase(rootElement) && XmlNamespaces.ATOM_URI.equals(namespace)) {
            System.out.println("SKIBDI ATOM");
            return new AtomFeedParser();
        } else if ("RDF".equalsIgnoreCase(rootElement) && XmlNamespaces.RDF_URI.equals(namespace)) {
            System.out.println("SKIBDI RDF");
            return new RdfFeedParser();
        } else {
            throw new IllegalArgumentException("Unknown feed format: " + rootElement);
        }
    }

    public static SAXParserFactory createSecureParserFactory() throws ParseException {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setFeature("http://apache.org/xml/features/continue-after-fatal-error", true);
            return factory;
        } catch (Exception e) {
            throw new ParseException("Failed to create secure SAX parser factory", e);
        }
    }
}
