package com.sismics.reader.core.dao.file.rss;

import com.sismics.reader.core.model.jpa.Article;
import com.sismics.util.DateUtil;
import org.apache.commons.lang.StringUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import java.util.*;

public class RdfFeedParser extends ВaseParser{
    private static final String ELEMENT_RDF = "RDF";
    private static final String ELEMENT_CHANNEL = "channel";
    private static final String ELEMENT_TITLE = "title";
    private static final String ELEMENT_LINK = "link";
    private static final String ELEMENT_DESCRIPTION = "description";
    private static final String ELEMENT_ITEM = "item";
    private static final String ELEMENT_CREATOR = "creator";
    private static final String ELEMENT_DATE = "date";
    private static final String ELEMENT_ENCODED = "encoded";

    public static final DateTimeFormatter DC_DATE_FORMATS = new DateTimeFormatterBuilder()
            .append(null, new DateTimeParser[] {
                    DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ").getParser(),
                    DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ").getParser()
            }).toFormatter().withOffsetParsed().withLocale(Locale.ENGLISH);

    public boolean canHandle(String rootElement, String namespace) {
        return ELEMENT_RDF.equals(rootElement) && XmlNamespaces.RDF_URI.equals(namespace);
    }

    @Override
    protected void handleStartElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
        pushElement(localName.toLowerCase());

        if (ELEMENT_ITEM.equals(localName)) {
            currentArticle = new Article();
            articles.add(currentArticle);

            String about = StringUtils.trim(attributes.getValue(XmlNamespaces.RDF_URI, "about"));
            if (!StringUtils.isBlank(about)) {
                currentArticle.setGuid(about);
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
        } else if (ELEMENT_LINK.equals(element) && !XmlNamespaces.ATOM_URI.equals(uri)) {
            if (currentArticle == null) {
                feed.setUrl(content);
            } else {
                currentArticle.setUrl(content);
            }
        } else if (ELEMENT_DESCRIPTION.equals(element)) {
            if (currentArticle == null) {
                feed.setDescription(content);
            } else if (currentArticle.getDescription() == null) {
                currentArticle.setDescription(content);
            }
        } else if (ELEMENT_CREATOR.equals(element) && XmlNamespaces.DC_URI.equals(uri)) {
            if (currentArticle != null) {
                currentArticle.setCreator(content);
            }
        } else if (ELEMENT_DATE.equals(element) && XmlNamespaces.DC_URI.equals(uri)) {
            if (currentArticle != null) {
                Date publicationDate = DateUtil.parseDate(content, DC_DATE_FORMATS);
                currentArticle.setPublicationDate(publicationDate);
            }
        } else if (ELEMENT_ENCODED.equals(element) && XmlNamespaces.CONTENT_URI.equals(uri)) {
            if (currentArticle != null) {
                currentArticle.setDescription(content);
            }
        } else if (ELEMENT_ITEM.equals(element)) {
            currentArticle = null;
        }

        popElement();
    }
}
