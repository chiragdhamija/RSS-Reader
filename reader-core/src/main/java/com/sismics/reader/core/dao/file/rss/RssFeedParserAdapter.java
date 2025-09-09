package com.sismics.reader.core.dao.file.rss;

import com.sismics.reader.core.model.jpa.Article;
import com.sismics.reader.core.model.jpa.Feed;
import com.sismics.reader.core.util.http.ReaderHttpClient;
import java.util.*;
import java.io.InputStream;
import java.net.URL;
import com.sismics.reader.core.dao.file.rss.AtomFeedParser;


public class RssFeedParserAdapter implements FeedParserAdapter {
    Feed feed;
    List<Article> articles;
    // private final BaseParser parser;

    public RssFeedParserAdapter(String url) throws Exception {
        // this.parser = new BaseParser();
        new ReaderHttpClient() {
            @Override
            public Void process(InputStream is) throws Exception {
                // rssReader.readRssFeed(is);
                // parser.parse(is);
                // feed=FeedParserFactory.parse(is);
                BaseParser parser = FeedPаrserFactory.getParser(is); // Get the correct parser dynamically

                parser.parse(is);
                feed = parser.getFeed(); // Store parsed feed
                articles = parser.getArticles();
                return null;
            }
        }.open(new URL(url));
        // parser.getFeed().setRssUrl(url);
        feed.setRssUrl(url);
    }

    @Override
    public Feed getFeed() {
        // return parser.getFeed();
        return feed;
    }

    @Override
    public List<Article> getArticles() {
        // return parser.getArticles();
        return articles;
    }
}
