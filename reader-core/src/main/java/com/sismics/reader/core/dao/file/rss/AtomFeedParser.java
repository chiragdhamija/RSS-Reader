package com.sismics.reader.core.dao.file.rss;

import com.sismics.reader.core.model.jpa.Article;
import com.sismics.util.DateUtil;
import com.sismics.util.UrlUtil;
import org.apache.commons.lang.StringUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import java.net.MalformedURLException;
import java.util.*;

public class AtomFeedParser extends ВaseParser {
    private static final String ELEMENT_FEED = "feed";
    private static final String ELEMENT_TITLE = "title";
    private static final String ELEMENT_SUBTITLE = "subtitle";
    private static final String ELEMENT_LINK = "link";
    private static final String ELEMENT_ENTRY = "entry";
    private static final String ELEMENT_ID = "id";
    private static final String ELEMENT_UPDATED = "updated";
    private static final String ELEMENT_SUMMARY = "summary";
    private static final String ELEMENT_CONTENT = "content";
    private static final String ELEMENT_AUTHOR = "author";
    private static final String ELEMENT_NAME = "name";

    private List<AtomLink> feedLinks;
    private List<AtomLink> entryLinks;

    public static final DateTimeFormatter ATOM_DATE_FORMATS = new DateTimeFormatterBuilder()
            .append(null, new DateTimeParser[] {
                    DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ").getParser(),
                    DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ").getParser()
            }).toFormatter().withOffsetParsed().withLocale(Locale.ENGLISH);

    @Override
    protected void initializeParser() {
        super.initializeParser();
        feedLinks = new ArrayList<>();
        entryLinks = new ArrayList<>();
    }

    @Override
    public boolean canHandle(String rootElement, String namespace) {
        return ELEMENT_FEED.equals(rootElement) && XmlNamespaces.ATOM_URI.equals(namespace);
    }

    @Override
    protected void handleStartElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
        pushElement(localName.toLowerCase());

        if (ELEMENT_FEED.equals(localName)) {
            String lang = StringUtils.trimToNull(attributes.getValue(XmlNamespaces.XML_URI, "lang"));
            feed.setLanguage(lang);
            String baseUri = StringUtils.trimToNull(attributes.getValue(XmlNamespaces.XML_URI, "base"));
            feed.setBaseUri(baseUri);
        } else if (ELEMENT_LINK.equals(localName)) {
            String rel = StringUtils.trimToNull(attributes.getValue("rel"));
            String type = StringUtils.trimToNull(attributes.getValue("type"));
            String href = StringUtils.trimToNull(attributes.getValue("href"));

            if (currentArticle == null) {
                feedLinks.add(new AtomLink(rel, type, href));
            } else {
                entryLinks.add(new AtomLink(rel, type, href));
            }
        } else if (ELEMENT_ENTRY.equals(localName)) {
            currentArticle = new Article();
            articles.add(currentArticle);
            entryLinks = new ArrayList<>();

            String baseUri = StringUtils.trimToNull(attributes.getValue(XmlNamespaces.XML_URI, "base"));
            if (baseUri != null) {
                currentArticle.setBaseUri(baseUri);
            }
        } else if (ELEMENT_CONTENT.equals(localName)) {
            String baseUri = StringUtils.trimToNull(attributes.getValue(XmlNamespaces.XML_URI, "base"));
            if (baseUri != null && currentArticle != null) {
                currentArticle.setBaseUri(baseUri);
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
        } else if (ELEMENT_SUBTITLE.equals(element)) {
            feed.setDescription(content);
        } else if (ELEMENT_ID.equals(element)) {
            if (currentArticle != null) {
                currentArticle.setGuid(content);
            }
        } else if (ELEMENT_UPDATED.equals(element)) {
            if (currentArticle != null) {
                Date updatedDate = DateUtil.parseDate(content, ATOM_DATE_FORMATS);
                currentArticle.setPublicationDate(updatedDate);
            }
        } else if (ELEMENT_SUMMARY.equals(element)) {
            if (currentArticle != null && currentArticle.getDescription() == null) {
                currentArticle.setDescription(content);
            }
        } else if (ELEMENT_CONTENT.equals(element)) {
            if (currentArticle != null) {
                currentArticle.setDescription(content);
            }
        } else if (ELEMENT_NAME.equals(element) && ELEMENT_AUTHOR.equals(elementStack.peek())) {
            if (currentArticle != null) {
                currentArticle.setCreator(content);
            }
        } else if (ELEMENT_ENTRY.equals(element)) {
            if (currentArticle != null) {
                // String url = new AtomUrlGuesserStrategy().guess(entryLinks);
                // currentArticle.setUrl(url);
                // String commentUrl = new AtomCommentUrlGuesserStrategy().guess(entryLinks);
                // currentArticle.setCommentUrl(commentUrl);
                String url = new AtomArticleUrlGuesserStrategy().guess(entryLinks);
                currentArticle.setUrl(url);
                String commentUrl = new AtomArticleCommentUrlGuesserStrategy().guess(entryLinks);
                currentArticle.setCommentUrl(commentUrl);
            }
            currentArticle = null;
            entryLinks = null;
        } else if (ELEMENT_FEED.equals(element)) {
            AtomUrlGuesserStrategy strategy = new AtomUrlGuesserStrategy();
            String siteUrl = strategy.guessSiteUrl(feedLinks);
            feed.setUrl(siteUrl);

            if (feed.getBaseUri() == null) {
                String feedBaseUri = strategy.guessFeedUrl(feedLinks);
                if (feedBaseUri != null) {
                    try {
                        feed.setBaseUri(UrlUtil.getBaseUri(feedBaseUri));
                    } catch (MalformedURLException e) {
                        // NOP
                    }
                }
            }
        }

        popElement();
    }
}