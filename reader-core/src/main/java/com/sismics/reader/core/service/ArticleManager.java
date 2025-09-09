package com.sismics.reader.core.service;

import com.google.common.collect.Lists;
import com.google.common.base.Strings;
import com.sismics.reader.core.dao.jpa.ArticleDao;
import com.sismics.reader.core.dao.jpa.FeedSubscriptionDao;
import com.sismics.reader.core.dao.jpa.UserArticleDao;
import com.sismics.reader.core.dao.jpa.criteria.ArticleCriteria;
import com.sismics.reader.core.dao.jpa.criteria.FeedSubscriptionCriteria;
import com.sismics.reader.core.dao.jpa.criteria.UserArticleCriteria;
import com.sismics.reader.core.dao.jpa.dto.ArticleDto;
import com.sismics.reader.core.dao.jpa.dto.FeedSubscriptionDto;
import com.sismics.reader.core.dao.jpa.dto.UserArticleDto;
import com.sismics.reader.core.event.ArticleCreatedAsyncEvent;
import com.sismics.reader.core.event.ArticleDeletedAsyncEvent;
import com.sismics.reader.core.event.ArticleUpdatedAsyncEvent;
import com.sismics.reader.core.model.context.AppContext;
import com.sismics.reader.core.model.jpa.Article;
import com.sismics.reader.core.model.jpa.UserArticle;
import com.sismics.reader.core.util.sanitizer.ArticleSanitizer;
import com.sismics.reader.core.util.sanitizer.TextSanitizer;
import com.sismics.util.UrlUtil;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DurationFieldType;
// import org.joda.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sismics.reader.core.model.jpa.Feed;

import java.util.*;

public class ArticleManager {
    private static final Logger log = LoggerFactory.getLogger(ArticleManager.class);
    private UserArticleService userArticleService = new UserArticleService();
    /**
     * Ensures that every article has a valid publication date.
     */
    public void completeArticleList(List<Article> articleList) {
        Date now = new Date();
        for (Article article : articleList) {
            if (article.getPublicationDate() == null || article.getPublicationDate().after(now)) {
                article.setPublicationDate(now);
            }
        }
    }

    /**
     * Determines which articles (present in the DB but missing from the current feed)
     * should be removed.
     */
    public List<Article> getArticlesToRemove(List<Article> articleList) {
        List<Article> removedArticles = new ArrayList<Article>();
        Article oldestArticle = getOldestArticle(articleList);
        if (oldestArticle == null) {
            return removedArticles;
        }
        ArticleDao articleDao = new ArticleDao();
        ArticleDto localArticle = articleDao.findFirstByCriteria(new ArticleCriteria()
                .setGuidIn(Collections.singletonList(oldestArticle.getGuid())));
        if (localArticle == null) {
            return removedArticles;
        }
        List<Article> newerArticles = getNewerArticles(articleList, oldestArticle);
        Set<String> newerGuids = new HashSet<>();
        for (Article art : newerArticles) {
            newerGuids.add(art.getGuid());
        }
        List<ArticleDto> localArticles = articleDao.findByCriteria(new ArticleCriteria()
                .setFeedId(localArticle.getFeedId())
                .setPublicationDateMin(oldestArticle.getPublicationDate()));
        Date dateMin = new DateTime().withFieldAdded(DurationFieldType.days(), -1).toDate();
        for (ArticleDto localArt : localArticles) {
            if (!newerGuids.contains(localArt.getGuid()) && localArt.getCreateDate().after(dateMin)) {
                removedArticles.add(new Article(localArt.getId()));
            }
        }
        return removedArticles;
    }

    private List<Article> getNewerArticles(List<Article> articleList, Article oldestArticle) {
        List<Article> result = new ArrayList<>();
        for (Article article : articleList) {
            if (article.getPublicationDate().after(oldestArticle.getPublicationDate())) {
                result.add(article);
            }
        }
        return result;
    }

    private Article getOldestArticle(List<Article> articleList) {
        Article oldest = null;
        for (Article article : articleList) {
            if (oldest == null || article.getPublicationDate().before(oldest.getPublicationDate())) {
                oldest = article;
            }
        }
        return oldest;
    }

    /**
     * Processes article deletions: updates unread counts, deletes from the DB,
     * and publishes a deletion event.
     */
    public void handleArticleRemovals(List<Article> articlesToRemove) {
        // UserArticleDao userArticleDao = new UserArticleDao();
        for (Article article : articlesToRemove) {
            List<UserArticleDto> userArticles = new UserArticleDao()
            .findByCriteria(new UserArticleCriteria()
                    .setArticleId(article.getId())
                    .setFetchAllFeedSubscription(true)
                    .setUnread(true));
            // FeedSubscriptionDao feedSubscriptionDao = new FeedSubscriptionDao();
            for (UserArticleDto userArticle : userArticles) {
                FeedSubscriptionDto fsDto = new FeedSubscriptionDao().findFirstByCriteria(
                        new FeedSubscriptionCriteria().setId(userArticle.getFeedSubscriptionId()));
                if (fsDto != null) {
                    new FeedSubscriptionDao().updateUnreadCount(fsDto.getId(), fsDto.getUnreadUserArticleCount() - 1);
                }
            }
        }
        // ArticleDao articleDao = new ArticleDao();
        for (Article article : articlesToRemove) {
            new ArticleDao().delete(article.getId());
        }
        ArticleDeletedAsyncEvent deletionEvent = new ArticleDeletedAsyncEvent();
        deletionEvent.setArticleList(articlesToRemove);
        AppContext.getInstance().getAsyncEventBus().post(deletionEvent);
    }

    /**
     * Processes article updates and creations, then publishes the corresponding events.
     */
    public void processArticles(Feed feed, List<Article> articleList) {

        Map<String, Article> articleMap = new HashMap<String, Article>();
        for (Article article : articleList) {
            articleMap.put(article.getGuid(), article);
        }
        // List<String> guidList = new ArrayList<>(articleMap.keySet());
        List<String> guidList = new ArrayList<String>();
        for(Article article : articleList) {
            guidList.add(article.getGuid());
        }

        ArticleSanitizer sanitizer = new ArticleSanitizer();
        ArticleDao articleDao = new ArticleDao();
        // Update existing articles.
        if (!guidList.isEmpty()) {
            ArticleCriteria criteria = new ArticleCriteria().setFeedId(feed.getId()).setGuidIn(guidList);
            List<ArticleDto> currentArticles = articleDao.findByCriteria(criteria);
            List<Article> updatedArticles = new ArrayList<Article>();
            for (ArticleDto currentArticle : currentArticles) {
                Article newArticle = articleMap.remove(currentArticle.getGuid());

                Article articleToUpdate = new Article();
                articleToUpdate.setPublicationDate(currentArticle.getPublicationDate());
                articleToUpdate.setId(currentArticle.getId());
                articleToUpdate.setFeedId(feed.getId());
                articleToUpdate.setUrl(newArticle.getUrl());
                articleToUpdate.setTitle(StringUtils.abbreviate(TextSanitizer.sanitize(newArticle.getTitle()), 4000));
                articleToUpdate.setCreator(StringUtils.abbreviate(newArticle.getCreator(), 200));
                String baseUri = UrlUtil.getBaseUri(feed, newArticle);
                articleToUpdate.setDescription(sanitizer.sanitize(baseUri, newArticle.getDescription()));
                articleToUpdate.setCommentUrl(newArticle.getCommentUrl());
                articleToUpdate.setCommentCount(newArticle.getCommentCount());
                articleToUpdate.setEnclosureUrl(newArticle.getEnclosureUrl());
                articleToUpdate.setEnclosureLength(newArticle.getEnclosureLength());
                articleToUpdate.setEnclosureType(newArticle.getEnclosureType());

                if (!Strings.nullToEmpty(currentArticle.getTitle()).equals(Strings.nullToEmpty(articleToUpdate.getTitle())) ||
                        !Strings.nullToEmpty(currentArticle.getDescription()).equals(Strings.nullToEmpty(articleToUpdate.getDescription()))) {
                    articleDao.update(articleToUpdate);
                    updatedArticles.add(articleToUpdate);
                }
            }
            if (!updatedArticles.isEmpty()) {
                ArticleUpdatedAsyncEvent updateEvent = new ArticleUpdatedAsyncEvent();
                updateEvent.setArticleList(updatedArticles);
                AppContext.getInstance().getAsyncEventBus().post(updateEvent);
            }
        }

        // Create new articles.
        if (!articleMap.isEmpty()) {
            FeedSubscriptionCriteria feedSubscriptionCriteria = new FeedSubscriptionCriteria()
                    .setFeedId(feed.getId());

            FeedSubscriptionDao feedSubscriptionDao = new FeedSubscriptionDao();
            List<FeedSubscriptionDto> subscriptions = feedSubscriptionDao.findByCriteria(feedSubscriptionCriteria);

            UserArticleDao userArticleDao = new UserArticleDao();
            for (Article newArticle : articleMap.values()) {
                newArticle.setFeedId(feed.getId());
                newArticle.setTitle(StringUtils.abbreviate(TextSanitizer.sanitize(newArticle.getTitle()), 4000));
                newArticle.setCreator(StringUtils.abbreviate(newArticle.getCreator(), 200));
                String baseUri = UrlUtil.getBaseUri(feed, newArticle);
                newArticle.setDescription(sanitizer.sanitize(baseUri, newArticle.getDescription()));
                articleDao.create(newArticle);

                for (FeedSubscriptionDto subscription : subscriptions) {
                    // Assuming subObj is of type FeedSubscriptionDto
                    // com.sismics.reader.core.dao.jpa.dto.FeedSubscriptionDto sub = (com.sismics.reader.core.dao.jpa.dto.FeedSubscriptionDto) subObj;
                    UserArticle userArticle = new UserArticle();
                    userArticle.setArticleId(newArticle.getId());
                    userArticle.setUserId(subscription.getUserId());
                    userArticleDao.create(userArticle);

                    subscription.setUnreadUserArticleCount(subscription.getUnreadUserArticleCount() + 1);
                    feedSubscriptionDao.updateUnreadCount(subscription.getId(), subscription.getUnreadUserArticleCount());
                }
            }
            ArticleCreatedAsyncEvent createEvent = new ArticleCreatedAsyncEvent();
            createEvent.setArticleList(Lists.newArrayList(articleMap.values()));
            AppContext.getInstance().getAsyncEventBus().post(createEvent);
        }
    }

    public UserArticleService getUserArticleService() {
        return userArticleService;
    }
}
