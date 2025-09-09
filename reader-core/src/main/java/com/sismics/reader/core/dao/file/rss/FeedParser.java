package com.sismics.reader.core.dao.file.rss;

import com.sismics.reader.core.model.jpa.Feed;
import java.io.InputStream;
import java.util.List;
import com.sismics.reader.core.model.jpa.Article;

/**
 * Interface for feed parsers.
 */
public interface FeedParser {
    void parse(InputStream is) throws ParseException;
    Feed getFeed();
    List<Article> getArticles();
}
