package com.sismics.reader.core.dao.file.rss;

import com.sismics.reader.core.model.jpa.Article;
import com.sismics.reader.core.model.jpa.Feed;
import com.sismics.reader.core.util.StreamUtil;

// import com.sismics.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParser;
import java.io.InputStream;
import java.util.*;
import java.io.Reader;

public abstract class ВaseParser extends DefaultHandler implements FeedParser {
    protected static final Logger log = LoggerFactory.getLogger(BaseParser.class);
    private static final int MAX_RECOVERY_ATTEMPTS = 100;

    protected Feed feed;
    protected Article currentArticle;
    protected List<Article> articles;
    protected StringBuilder content;
    protected Deque<String> elementStack;
    protected int recoveryAttempts;

    public ВaseParser() {
        this.content = new StringBuilder();
        this.elementStack = new ArrayDeque<>();
    }

    // @Override
    public void parse(InputStream is) throws ParseException {
        try {
            initializeParser();
            SAXParser parser = FeedParserFactory.createSecureParserFactory().newSAXParser();
            Reader reader = new XmlReader(StreamUtil.detectGzip(is), "UTF-8");
            InputSource source = new InputSource(reader);

            try {
                parser.parse(source, this);
            } catch (InternalError e) {
                throw new Exception(e);
            }

            validateParsedFeed();
            fixGuid();
        } catch (Exception e) {
            throw new ParseException("Failed to parse feed", e);
        }
    }

    protected void initializeParser() {
        feed = new Feed();
        articles = new ArrayList<>();
        content.setLength(0);
        elementStack.clear();
        recoveryAttempts = 0;
    }

    protected void validateParsedFeed() throws ParseException {
        if (feed == null) {
            throw new ParseException("Invalid feed: missing required fields");
        }
    }

    protected void fixGuid() {
        if (articles != null) {
            for (Article article : articles) {
                GuidFixer.fixGuid(article);
            }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        // content.append(ch, start, length);
        if (content != null) {
            content.append(ch, start, length);
        }
    }

    protected String getContent() {
        String result = content.toString().trim();
        content.setLength(0);
        return result;
    }

    protected void pushElement(String element) {
        elementStack.push(element);
    }

    protected String popElement() {
        return elementStack.isEmpty() ? null : elementStack.pop();
    }

    protected String getCurrentElement() {
        return elementStack.isEmpty() ? null : elementStack.peek();
    }

    @Override
    public void fatalError(org.xml.sax.SAXParseException e) throws SAXException {
        log.warn("Attempting to recover from fatal parsing error", e);
        recoveryAttempts++;
        if (recoveryAttempts >= MAX_RECOVERY_ATTEMPTS) {
            throw new SAXException("Too many recovery attempts", e);
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
        // Reset content for new element
        content.setLength(0);
        handleStartElement(uri, localName, qName, attributes);
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        handleEndElement(uri, localName, qName);
    }

    public Feed getFeed() {
        return feed;
    }

    public List<Article> getArticles() {
        return articles;
    }

    protected abstract void handleStartElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException;

    protected abstract void handleEndElement(String uri, String localName, String qName) throws SAXException;

    public abstract boolean canHandle(String rootElement, String namespace);
}
