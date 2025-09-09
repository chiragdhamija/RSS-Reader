package com.sismics.reader.core.dao.file.rss;

import com.sismics.reader.core.model.jpa.Article;

import org.apache.commons.lang.StringUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import static com.sismics.reader.core.dao.file.rss.ВaseParser.log;

import java.util.*;
import com.sismics.reader.core.model.jpa.Feed;
import com.sismics.util.DateUtil;

public class RssFeedParser extends ВaseParser {
    private static final String ELEMENT_RSS = "rss";
    private static final String ELEMENT_CHANNEL = "channel";
    private static final String ELEMENT_ITEM = "item";
    private static final String ELEMENT_TITLE = "title";
    private static final String ELEMENT_LINK = "link";
    private static final String ELEMENT_DESCRIPTION = "description";
    private static final String ELEMENT_PUBDATE = "pubDate";
    private static final String ELEMENT_GUID = "guid";
    private static final String ELEMENT_COMMENTS = "comments";
    private static final String ELEMENT_CREATOR = "creator";
    private static final String ELEMENT_DATE = "date";
    private static final String ELEMENT_ENCODED = "encoded";
    private static final String ELEMENT_ENCLOSURE = "enclosure";
    private static final String ELEMENT_LANGUAGE = "language";

    private boolean inChannel = false;
    private boolean feedInitialized = false;

    public static final DateTimeFormatter RSS_DATE_FORMATS = new DateTimeFormatterBuilder()
            .append(null, new DateTimeParser[] {
                    DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm zzz").getParser(),
                    DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss Z").getParser(),
                    DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss zzz").getParser(),
                    DateTimeFormat.forPattern("EEE,  d MMM yyyy HH:mm:ss zzz").getParser(),
                    DateTimeFormat.forPattern("dd MMM yyyy HH:mm:ss Z").getParser(),
                    DateTimeFormat.forPattern("yyyy-mm-dd HH:mm:ss").getParser(),
                    DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss").getParser(),
                    DateTimeFormat.forPattern("dd MMM yyyy HH:mm:ss zzz").getParser(),
                    DateTimeFormat.forPattern("EEE MMM dd yyyy HH:mm:ss 'GMT'Z Z").getParser()
            }).toFormatter().withOffsetParsed().withLocale(Locale.ENGLISH);

    public static final DateTimeFormatter DC_DATE_FORMATS = new DateTimeFormatterBuilder()
            .append(null, new DateTimeParser[] {
                    DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ").getParser(),
                    DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ").getParser()
            }).toFormatter().withOffsetParsed().withLocale(Locale.ENGLISH);

    @Override
    protected void initializeParser() {
        super.initializeParser();
        inChannel = false;
        feedInitialized = false;
    }

    @Override
    public boolean canHandle(String rootElement, String namespace) {
        return ELEMENT_RSS.equals(rootElement);
    }

    @Override
    protected void handleStartElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
        String elementName = localName.toLowerCase();
        pushElement(elementName);
        if (ELEMENT_RSS.equals(elementName)) {
            feedInitialized = true;
        } else if (ELEMENT_CHANNEL.equals(elementName)) {
            inChannel = true;
        } else if (ELEMENT_ITEM.equals(elementName)) {
            currentArticle = new Article();
            articles.add(currentArticle);
        } else if (ELEMENT_ENCLOSURE.equals(elementName) && currentArticle != null) {
            String enclosureUrl = attributes.getValue("url");
            if (!StringUtils.isBlank(enclosureUrl)) {
                currentArticle.setEnclosureUrl(enclosureUrl.trim());
                String length = attributes.getValue("length");
                if (!StringUtils.isBlank(length)) {
                    try {
                        currentArticle.setEnclosureLength(Integer.valueOf(length.trim()));
                    } catch (NumberFormatException e) {
                        // Unable to parse length
                    }
                }
                currentArticle.setEnclosureType(attributes.getValue("type"));
            }
        }
    }

    @Override
    protected void handleEndElement(String uri, String localName, String qName) throws SAXException {
        String element = getCurrentElement();
        String content = getContent();

        if (ELEMENT_TITLE.equals(element)) {
            if (currentArticle == null) {
                feed.setTitle(content);
            } else {
                currentArticle.setTitle(content);
            }
        } else if (ELEMENT_LINK.equals(element)) {
            if (currentArticle == null) {
                feed.setUrl(content);
            } else {
                currentArticle.setUrl(content);
            }
        } else if (ELEMENT_DESCRIPTION.equals(element)) {
            if (currentArticle == null) {
                feed.setDescription(content);
            } else {
                currentArticle.setDescription(content);
            }
        } else if (ELEMENT_LANGUAGE.equals(element) && inChannel) {
            feed.setLanguage(content);
        } else if(ELEMENT_DATE.equals(element) && currentArticle!=null) {
            Date pubDate = DateUtil.parseDate(content, DC_DATE_FORMATS);
            currentArticle.setPublicationDate(pubDate);
        }
         else if (ELEMENT_PUBDATE.equals(element) && currentArticle != null) {
            Date pubDate = DateUtil.parseDate(content, RSS_DATE_FORMATS);
            currentArticle.setPublicationDate(pubDate);
        } else if (ELEMENT_ITEM.equals(element)) {
            currentArticle = null;
        } else if (ELEMENT_GUID.equals(element) && currentArticle != null) {
            currentArticle.setGuid(content);
        } else if (ELEMENT_COMMENTS.equals(element) && currentArticle != null && !XmlNamespaces.SLASH_URI.equals(uri)) {
            currentArticle.setCommentUrl(content);
        } else if (ELEMENT_COMMENTS.equals(element) && currentArticle != null && XmlNamespaces.SLASH_URI.equals(uri)) {
            try {
                currentArticle.setCommentCount(Integer.parseInt(content));
            } catch (NumberFormatException e) {
                log.warn("Error parsing comment count: " + content);
            }
        } else if (ELEMENT_CREATOR.equals(element) && XmlNamespaces.DC_URI.equals(uri) && currentArticle != null) {
            currentArticle.setCreator(content);
        } else if (ELEMENT_ENCODED.equals(element) && XmlNamespaces.CONTENT_URI.equals(uri) && currentArticle != null) {
            currentArticle.setDescription(content);
        } else if (ELEMENT_LANGUAGE.equals(element) && currentArticle == null) {
            feed.setLanguage(content);
        } else if (ELEMENT_ITEM.equals(element)) {
            currentArticle = null;
        }

        popElement();
    }
}
// package com.sismics.reader.core.dao.rss;

// import com.sismics.reader.core.model.jpa.Article;

// import org.apache.commons.lang.StringUtils;
// import org.joda.time.format.DateTimeFormat;
// import org.joda.time.format.DateTimeFormatter;
// import org.joda.time.format.DateTimeFormatterBuilder;
// import org.joda.time.format.DateTimeParser;
// import org.xml.sax.Attributes;
// import org.xml.sax.SAXException;

// import java.util.*;
// import com.sismics.util.DateUtil;

// public class RssFeedParser extends BaseParser {
//     // XML namespaces
//     private static final String URI_SLASH = "http://purl.org/rss/1.0/modules/slash/";
//     private static final String URI_DC = "http://purl.org/dc/elements/1.1/";
//     private static final String URI_CONTENT = "http://purl.org/rss/1.0/modules/content/";
    
//     // RSS element names
//     private static final String ELEMENT_RSS = "rss";
//     private static final String ELEMENT_CHANNEL = "channel";
//     private static final String ELEMENT_ITEM = "item";
//     private static final String ELEMENT_TITLE = "title";
//     private static final String ELEMENT_LINK = "link";
//     private static final String ELEMENT_DESCRIPTION = "description";
//     private static final String ELEMENT_PUBDATE = "pubDate";
//     private static final String ELEMENT_GUID = "guid";
//     private static final String ELEMENT_COMMENTS = "comments";
//     private static final String ELEMENT_CREATOR = "creator";
//     private static final String ELEMENT_DATE = "date";
//     private static final String ELEMENT_ENCODED = "encoded";
//     private static final String ELEMENT_ENCLOSURE = "enclosure";
//     private static final String ELEMENT_LANGUAGE = "language";

//     private boolean inChannel = false;
//     private boolean inItem = false;

//     /**
//      * Date formatters for RSS feed dates.
//      */
//     public static final DateTimeFormatter RSS_DATE_FORMATS = new DateTimeFormatterBuilder()
//             .append(null, new DateTimeParser[] {
//                     DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm zzz").getParser(),
//                     DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss Z").getParser(),
//                     DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss zzz").getParser(),
//                     DateTimeFormat.forPattern("EEE,  d MMM yyyy HH:mm:ss zzz").getParser(),
//                     DateTimeFormat.forPattern("dd MMM yyyy HH:mm:ss Z").getParser(),
//                     DateTimeFormat.forPattern("yyyy-mm-dd HH:mm:ss").getParser(),
//                     DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss").getParser(),
//                     DateTimeFormat.forPattern("dd MMM yyyy HH:mm:ss zzz").getParser(),
//                     DateTimeFormat.forPattern("EEE MMM dd yyyy HH:mm:ss 'GMT'Z Z").getParser()
//             }).toFormatter().withOffsetParsed().withLocale(Locale.ENGLISH);

//     /**
//      * Date formatters for Dublin Core dates.
//      */
//     public static final DateTimeFormatter DC_DATE_FORMATS = new DateTimeFormatterBuilder()
//             .append(null, new DateTimeParser[] {
//                     DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ").getParser(),
//                     DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ").getParser()
//             }).toFormatter().withOffsetParsed().withLocale(Locale.ENGLISH);

//     @Override
//     protected void initializeParser() {
//         super.initializeParser();
//         inChannel = false;
//         inItem = false;
//     }

//     @Override
//     public boolean canHandle(String rootElement, String namespace) {
//         return ELEMENT_RSS.equals(rootElement);
//     }

//     @Override
//     protected void handleStartElement(String uri, String localName, String qName, Attributes attributes)
//             throws SAXException {
//         String elementName = StringUtils.isNotBlank(localName) ? localName : qName;
//         pushElement(elementName.toLowerCase());
        
//         if (ELEMENT_RSS.equals(elementName.toLowerCase())) {
//             // Initialize feed for RSS
//             feed = new Feed();
//             articles = new ArrayList<>();
//         } else if (ELEMENT_CHANNEL.equals(elementName.toLowerCase())) {
//             inChannel = true;
//         } else if (ELEMENT_ITEM.equals(elementName.toLowerCase())) {
//             inItem = true;
//             currentArticle = new Article();
//             articles.add(currentArticle);
//         } else if (ELEMENT_ENCLOSURE.equals(elementName.toLowerCase()) && currentArticle != null) {
//             String enclosureUrl = StringUtils.trim(attributes.getValue("url"));
//             if (!StringUtils.isBlank(enclosureUrl)) {
//                 currentArticle.setEnclosureUrl(enclosureUrl);
//                 String length = attributes.getValue("length");
//                 if (!StringUtils.isBlank(length)) {
//                     try {
//                         currentArticle.setEnclosureLength(Integer.valueOf(length.trim()));
//                     } catch (NumberFormatException e) {
//                         log.warn("Error parsing enclosure length: " + length);
//                     }
//                 }
//                 currentArticle.setEnclosureType(StringUtils.trim(attributes.getValue("type")));
//             }
//         }
//     }

//     @Override
//     protected void handleEndElement(String uri, String localName, String qName) throws SAXException {
//         String elementName = StringUtils.isNotBlank(localName) ? localName : qName;
//         String element = elementName.toLowerCase();
//         String content = getContent();

//         if (ELEMENT_TITLE.equals(element)) {
//             if (!inItem) {
//                 feed.setTitle(content);
//             } else {
//                 currentArticle.setTitle(content);
//             }
//         } else if (ELEMENT_LINK.equals(element)) {
//             if (!inItem) {
//                 feed.setUrl(content);
//             } else {
//                 currentArticle.setUrl(content);
//             }
//         } else if (ELEMENT_DESCRIPTION.equals(element)) {
//             if (!inItem) {
//                 feed.setDescription(content);
//             } else {
//                 // Use encoded:content (full content) if available, otherwise description
//                 if (currentArticle.getDescription() == null) {
//                     currentArticle.setDescription(content);
//                 }
//             }
//         } else if (ELEMENT_LANGUAGE.equals(element) && inChannel) {
//             feed.setLanguage(content);
//         } else if (ELEMENT_DATE.equals(element) && URI_DC.equals(uri) && currentArticle != null) {
//             Date pubDate = DateUtil.parseDate(content, DC_DATE_FORMATS);
//             currentArticle.setPublicationDate(pubDate);
//         } else if (ELEMENT_PUBDATE.equals(element) && currentArticle != null) {
//             Date pubDate = DateUtil.parseDate(content, RSS_DATE_FORMATS);
//             currentArticle.setPublicationDate(pubDate);
//         } else if (ELEMENT_GUID.equals(element) && currentArticle != null) {
//             currentArticle.setGuid(content);
//         } else if (ELEMENT_COMMENTS.equals(element) && currentArticle != null && !URI_SLASH.equals(uri)) {
//             currentArticle.setCommentUrl(content);
//         } else if (ELEMENT_COMMENTS.equals(element) && currentArticle != null && URI_SLASH.equals(uri)) {
//             try {
//                 currentArticle.setCommentCount(Integer.parseInt(content));
//             } catch (NumberFormatException e) {
//                 log.warn("Error parsing comment count: " + content);
//             }
//         } else if (ELEMENT_CREATOR.equals(element) && URI_DC.equals(uri) && currentArticle != null) {
//             currentArticle.setCreator(content);
//         } else if (ELEMENT_ENCODED.equals(element) && URI_CONTENT.equals(uri) && currentArticle != null) {
//             // Content:encoded takes precedence over description
//             currentArticle.setDescription(content);
//         } else if (ELEMENT_CHANNEL.equals(element)) {
//             inChannel = false;
//         } else if (ELEMENT_ITEM.equals(element)) {
//             inItem = false;
//             currentArticle = null;
//         }

//         popElement();
//     }

//     @Override
//     protected void validateParsedFeed() throws ParseException {
//         if (feed == null || StringUtils.isBlank(feed.getTitle())) {
//             throw new ParseException("Invalid RSS feed: missing required fields");
//         }
//     }
// }