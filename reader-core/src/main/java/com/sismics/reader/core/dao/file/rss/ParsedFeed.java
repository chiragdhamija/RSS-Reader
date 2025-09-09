package com.sismics.reader.core.dao.file.rss;

import com.sismics.reader.core.model.jpa.Article;
import com.sismics.reader.core.model.jpa.Feed;
import java.util.Collections;
import java.util.List;

public class ParsedFeed {
    private final Feed feed;
    private final List<Article> articles;

    public ParsedFeed(Feed feed, List<Article> articles) {
        this.feed = feed;
        this.articles = Collections.unmodifiableList(articles); // Defensive copy
    }

    public Feed getFeed() { return feed; }
    public List<Article> getArticles() { return articles; }
}