package com.sismics.reader.core.service;

// import com.google.common.base.Strings;
import com.sismics.reader.core.dao.file.html.FeedChooserStrategy;
import com.sismics.reader.core.dao.file.html.RssExtractor;
import com.sismics.reader.core.dao.file.rss.FeedParser;
import com.sismics.reader.core.dao.file.rss.FeedParserAdapter;
import com.sismics.reader.core.dao.file.rss.HtmlToRssAdapter;
import com.sismics.reader.core.dao.file.rss.RssFeedParser;
import com.sismics.reader.core.dao.file.rss.RssFeedParserAdapter;
// import com.sismics.reader.core.dao.file.rss.RssReader;
import com.sismics.reader.core.dao.file.rss.ВaseParser;
import com.sismics.reader.core.dao.jpa.FeedDao;
import com.sismics.reader.core.model.jpa.Feed;
// import com.sismics.reader.core.dao.jpa.criteria.ArticleCriteria;
// import com.sismics.reader.core.dao.jpa.dto.FeedDto;
// import com.sismics.reader.core.event.FaviconUpdateRequestedEvent;
// import com.sismics.reader.core.model.context.AppContext;
import com.sismics.reader.core.model.jpa.Article;
import com.sismics.reader.core.util.EntityManagerUtil;
import com.sismics.reader.core.util.http.ReaderHttpClient;
// import com.sismics.util.UrlUtil;
import org.apache.commons.lang.StringUtils;
// import org.joda.time.DateTime;
// import org.joda.time.Days;
// import org.joda.time.DurationFieldType;
// import org.joda.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

public class FeedSyncProcessor {
    private static final Logger log = LoggerFactory.getLogger(FeedSyncProcessor.class);

    private ArticleManager articleManager = new ArticleManager();
    private FaviconManager faviconManager = new FaviconManager();

    /**
     * Synchronizes a single feed.
     */
    public Feed synchronize(String url, String query, boolean parseAsHtml) throws Exception {
        // String url = feedDto.getRssUrl();
        long startTime = System.currentTimeMillis();

        // Parse the feed from the RSS URL or an HTML page.
        // FeedParser parser = parseFeedOrPage(url, true);
        // boolean parseAsHtml = !url.endsWith(".xml") && !url.contains("rss");
        FeedParserAdapter parser = parseFeedOrPage(url, query, false,parseAsHtml);
        Feed newFeed = parser.getFeed();
        List<Article> articleList = parser.getArticles();
        
        // Complete any missing article data.
        articleManager.completeArticleList(articleList);

        // Handle deletions of articles that were removed from the feed.
        List<Article> articlesToRemove = articleManager.getArticlesToRemove(articleList);
        if (!articlesToRemove.isEmpty()) {
            articleManager.handleArticleRemovals(articlesToRemove);
        }
        // Update or create the feed metadata.
        FeedDao feedDao = new FeedDao();
        Feed feed = feedDao.getByRssUrl(newFeed.getRssUrl());
        if (feed == null) {
            feed = new Feed();
            feed.setUrl(newFeed.getUrl());
            feed.setBaseUri(newFeed.getBaseUri());
            feed.setRssUrl(newFeed.getRssUrl());
            feed.setTitle(StringUtils.abbreviate(newFeed.getTitle(), 100));
            feed.setLanguage(newFeed.getLanguage() != null && newFeed.getLanguage().length() <= 10 ? newFeed.getLanguage() : null);
            feed.setDescription(StringUtils.abbreviate(newFeed.getDescription(), 4000));
            feed.setLastFetchDate(new Date());
            feedDao.create(feed);
            EntityManagerUtil.flush();
            // Request the feed’s favicon.
            faviconManager.requestFaviconUpdate(feed);
        } else {
            boolean updateFavicon = faviconManager.shouldUpdateFavicon(feed);
            feed.setUrl(newFeed.getUrl());
            feed.setBaseUri(newFeed.getBaseUri());
            feed.setTitle(StringUtils.abbreviate(newFeed.getTitle(), 100));
            feed.setLanguage(newFeed.getLanguage() != null && newFeed.getLanguage().length() <= 10 ? newFeed.getLanguage() : null);
            feed.setDescription(StringUtils.abbreviate(newFeed.getDescription(), 4000));
            feed.setLastFetchDate(new Date());
            feedDao.update(feed);

            if (updateFavicon) {
                faviconManager.requestFaviconUpdate(feed);
            }
        }

        // Process article updates and new articles.
        articleManager.processArticles(feed, articleList);

        long endTime = System.currentTimeMillis();
        log.info(MessageFormat.format("Synchronized feed at URL {0} in {1}ms", url, endTime - startTime));
        return feed;
    }

    /**
     * Parses a feed (or HTML page linking to a feed) from the given URL.
     */
    // private FeedParser parseFeedOrPage(String url, boolean parsePage) throws Exception {
    private FeedParserAdapter parseFeedOrPage(String url, String query, boolean parsePage, boolean parseAsHtml) throws Exception {
        try {
            if(parseAsHtml) {
                return new HtmlToRssAdapter(url, query);
            } else {
                return new RssFeedParserAdapter(url);
            }
            // final RssReader parser = new RssReader();
            // // final FeedParser parser = new RssFeedParser(); // Default to RSS if format unknown
            // // final ParsedFeed[] result = new ParsedFeed[1];
            // new ReaderHttpClient() {
            //     @Override
            //     public Void process(InputStream is) throws Exception {
            //         // parser.parse(is);
            //         // return null;
            //         parser.readRssFeed(is);
            //         return null;
            //     }
            // }.open(new URL(url));
            // parser.getFeed().setRssUrl(url);
            // return parser;
            // // reader.getFeed().setRssUrl(url);
            // // return reader;   
        } catch (Exception eRss) {
            boolean recoverable = !(eRss instanceof UnknownHostException ||
                    eRss instanceof FileNotFoundException);
            if (parsePage && recoverable) {
                try {
                    final RssExtractor extractor = new RssExtractor(url);
                    new ReaderHttpClient() {
                        @Override
                        public Void process(InputStream is) throws Exception {
                            extractor.readPage(is);
                            return null;
                        }
                    }.open(new URL(url));
                    List<String> feedList = extractor.getFeedList();
                    if (feedList == null || feedList.isEmpty()) {
                        logParsingError(url, eRss);
                    }
                    String feedUrl = new FeedChooserStrategy().guess(feedList);
                    // boolean parseAsHtml2 = !feedUrl.endsWith(".xml") && !feedUrl.contains("rss");
                    return parseFeedOrPage(feedUrl,query, false,parseAsHtml);
                } catch (Exception ePage) {
                    logParsingError(url, ePage);
                }
            } else {
                logParsingError(url, eRss);
            }
            throw eRss;
        }
    }

    private void logParsingError(String url, Exception e) {
        if (log.isWarnEnabled()) {
            if (e instanceof UnknownHostException ||
                    e instanceof FileNotFoundException ||
                    e instanceof ConnectException) {
                log.warn(MessageFormat.format("Error parsing HTML page at URL {0} : {1}", url, e.getMessage()));
            } else {
                log.warn(MessageFormat.format("Error parsing HTML page at URL {0}", url));
            }
        }
    }

    public ArticleManager getArticleManager() {
        return articleManager;
    }
}
