package com.sismics.reader.core.service;

import com.sismics.reader.core.dao.jpa.FeedSubscriptionDao;
import com.sismics.reader.core.dao.jpa.UserArticleDao;
import com.sismics.reader.core.dao.jpa.criteria.UserArticleCriteria;
import com.sismics.reader.core.dao.jpa.dto.UserArticleDto;
import com.sismics.reader.core.model.jpa.FeedSubscription;
import com.sismics.reader.core.model.jpa.UserArticle;
import com.sismics.reader.core.util.jpa.PaginatedList;
import com.sismics.reader.core.util.jpa.PaginatedLists;

public class UserArticleService {
    /**
     * Creates the initial batch of user articles when subscribing to a feed.
     */
    public void createInitialUserArticle(String userId, FeedSubscription feedSubscription) {
        UserArticleCriteria criteria = new UserArticleCriteria()
                .setUserId(userId)
                .setSubscribed(true)
                .setFeedId(feedSubscription.getFeedId());
        UserArticleDao userArticleDao = new UserArticleDao();
        PaginatedList<UserArticleDto> paginatedList = PaginatedLists.create();
        userArticleDao.findByCriteria(paginatedList, criteria, null, null);
        for (UserArticleDto dto : paginatedList.getResultList()) {
            if (dto.getId() == null) {
                UserArticle userArticle = new UserArticle();
                userArticle.setArticleId(dto.getArticleId());
                userArticle.setUserId(userId);
                userArticleDao.create(userArticle);
                feedSubscription.setUnreadCount(feedSubscription.getUnreadCount() + 1);
            } else if (dto.getReadTimestamp() == null) {
                feedSubscription.setUnreadCount(feedSubscription.getUnreadCount() + 1);
            }
        }
        FeedSubscriptionDao feedSubscriptionDao = new FeedSubscriptionDao();
        feedSubscriptionDao.updateUnreadCount(feedSubscription.getId(), feedSubscription.getUnreadCount());
    }
}
