package com.sismics.reader.core.dao.file.rss;
import java.util.List;
import com.sismics.reader.core.model.jpa.Article;
import com.sismics.reader.core.model.jpa.Feed;

public interface FeedParserAdapter {
    Feed getFeed() throws Exception;
    List<Article> getArticles() throws Exception;
}

