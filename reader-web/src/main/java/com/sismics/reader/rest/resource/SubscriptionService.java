package com.sismics.reader.rest.resource;

import com.sismics.reader.core.dao.jpa.CategoryDao;
import com.sismics.reader.core.dao.jpa.FeedSubscriptionDao;
import com.sismics.reader.core.dao.jpa.FeedSynchronizationDao;
import com.sismics.reader.core.dao.jpa.UserArticleDao;
import com.sismics.reader.core.model.jpa.*;
import com.sismics.reader.core.service.FeedService;
import com.sismics.reader.core.dao.jpa.criteria.FeedSubscriptionCriteria;
import com.sismics.reader.core.dao.jpa.criteria.UserArticleCriteria;
import com.sismics.reader.core.dao.jpa.dto.FeedSubscriptionDto;
import com.sismics.reader.core.dao.jpa.dto.UserArticleDto;
import com.sismics.reader.core.model.context.AppContext;
import com.sismics.reader.core.service.UserArticleService;
import com.sismics.reader.core.service.FeedSyncProcessor;
import com.sismics.reader.core.util.EntityManagerUtil;
import com.sismics.reader.core.util.jpa.PaginatedList;
import com.sismics.reader.core.util.jpa.PaginatedLists;
import com.sismics.rest.util.JsonUtil;
import com.sismics.rest.util.ValidationUtil;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ServerException;
import com.sismics.reader.rest.assembler.ArticleAssembler;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.persistence.NoResultException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SubscriptionService {
    // private final String userId;

    // public SubscriptionService(String userId) {
        // this.userId = userId;
    // }

    public JSONObject listSubscriptions(String userId, boolean unread) throws JSONException {
        FeedSubscriptionCriteria criteria = new FeedSubscriptionCriteria()
                .setUserId(userId)
                .setUnread(unread);

        FeedSubscriptionDao subscriptionDao = new FeedSubscriptionDao();
        List<FeedSubscriptionDto> subscriptionList = subscriptionDao.findByCriteria(criteria);

        return buildSubscriptionListResponse(userId, subscriptionList, unread);
    }

    public JSONObject getSubscriptionDetails(String userId, String subscriptionId, boolean unread, Integer limit, String afterArticle) throws JSONException {
        // Retrieve the subscription details
        FeedSubscriptionCriteria criteria = new FeedSubscriptionCriteria()
                .setId(subscriptionId)
                .setUserId(userId);
        FeedSubscriptionDao subscriptionDao = new FeedSubscriptionDao();
        List<FeedSubscriptionDto> subscriptions = subscriptionDao.findByCriteria(criteria);
        if (subscriptions.isEmpty()) {
            throw new ClientException("SubscriptionNotFound", MessageFormat.format("Subscription not found: {0}", subscriptionId));
        }
        FeedSubscriptionDto subscription = subscriptions.iterator().next();

        // Retrieve the articles (with pagination)
        UserArticleCriteria articleCriteria = new UserArticleCriteria()
                .setUnread(unread)
                .setUserId(userId)
                .setSubscribed(true)
                .setVisible(true)
                .setFeedId(subscription.getFeedId());
        if (afterArticle != null) {
            // Paginate after this user article
            UserArticleCriteria afterCriteria = new UserArticleCriteria()
                    .setUserArticleId(afterArticle)
                    .setUserId(userId);
            UserArticleDao userArticleDao = new UserArticleDao();
            List<UserArticleDto> afterArticles = userArticleDao.findByCriteria(afterCriteria);
            if (afterArticles.isEmpty()) {
                throw new ClientException("ArticleNotFound", MessageFormat.format("Can't find user article {0}", afterArticle));
            }
            UserArticleDto afterArticleDto = afterArticles.iterator().next();

            articleCriteria.setArticlePublicationDateMax(new Date(afterArticleDto.getArticlePublicationTimestamp()));
            articleCriteria.setArticleIdMax(afterArticleDto.getArticleId());
        }
        
        PaginatedList<UserArticleDto> paginatedList = PaginatedLists.create(limit, null);
        UserArticleDao userArticleDao = new UserArticleDao();
        userArticleDao.findByCriteria(paginatedList, articleCriteria, null, null);

        return buildSubscriptionDetailResponse(subscription, paginatedList.getResultList());
    }

    public JSONObject getSynchronizations(String userId, String subscriptionId) throws JSONException {
        // Retrieve the subscription
        FeedSubscriptionCriteria criteria = new FeedSubscriptionCriteria()
                .setId(subscriptionId)
                .setUserId(userId);
        FeedSubscriptionDao subscriptionDao = new FeedSubscriptionDao();
        List<FeedSubscriptionDto> subscriptions = subscriptionDao.findByCriteria(criteria);
        if (subscriptions.isEmpty()) {
            throw new ClientException("SubscriptionNotFound", MessageFormat.format("Subscription not found: {0}", subscriptionId));
        }
        FeedSubscriptionDto subscription = subscriptions.iterator().next();

        // Retrieve synchronizations for the feed
        FeedSynchronizationDao syncDao = new FeedSynchronizationDao();
        List<FeedSynchronization> syncList = syncDao.findByFeedId(subscription.getFeedId());

        List<JSONObject> syncArray = new ArrayList<JSONObject>();
        for (FeedSynchronization sync : syncList) {
            JSONObject syncJson = new JSONObject();
            syncJson.put("success", sync.isSuccess());
            syncJson.put("message", sync.getMessage());
            syncJson.put("duration", sync.getDuration());
            syncJson.put("create_date", sync.getCreateDate().getTime());
            syncArray.add(syncJson);
        }
        JSONObject response = new JSONObject();
        response.put("synchronizations", syncArray);
        return response;
    }

    public String addSubscription(String userId, String url, String title, String query, boolean parseAsHtml) throws JSONException {
        // Validate input
        ValidationUtil.validateRequired(url, "url"); 
        url = ValidationUtil.validateHttpUrl(url, "url");
        title = ValidationUtil.validateLength(title, "title", null, 100, true);

        // Check for duplicate subscription by URL
        FeedSubscriptionCriteria criteria = new FeedSubscriptionCriteria()
                .setUserId(userId)
                .setFeedUrl(url);
        FeedSubscriptionDao subscriptionDao = new FeedSubscriptionDao();
        List<FeedSubscriptionDto> existingSubscriptions = subscriptionDao.findByCriteria(criteria);
        if (!existingSubscriptions.isEmpty()) {
            throw new ClientException("AlreadySubscribed", "You are already subscribed to this URL");
        }
        // Retrieve the feed using the sync processor
        FeedSyncProcessor syncProcessor = AppContext.getInstance().getFeedService().getSyncManager().getFeedSyncProcessor();
        Feed feed;
        try {
            feed = syncProcessor.synchronize(url, query, parseAsHtml);
        } catch (Exception e) {
            throw new ServerException("FeedError", MessageFormat.format("Error retrieving feed at {0}", url), e);
        }
        // Check again using the feed's RSS URL
        criteria = new FeedSubscriptionCriteria()
                .setUserId(userId)
                .setFeedUrl(feed.getRssUrl());
        existingSubscriptions = subscriptionDao.findByCriteria(criteria);
        if (!existingSubscriptions.isEmpty()) {
            throw new ClientException("AlreadySubscribed", "You are already subscribed to this URL");
        }

        // Get the root category and display order
        CategoryDao categoryDao = new CategoryDao();
        Category rootCategory = categoryDao.getRootCategory(userId);

        Integer displayOrder = subscriptionDao.getCategoryCount(rootCategory.getId(), userId);

        // Create the subscription
        FeedSubscription feedSubscription = new FeedSubscription();
        feedSubscription.setUserId(userId);
        feedSubscription.setFeedId(feed.getId());
        feedSubscription.setCategoryId(rootCategory.getId());
        feedSubscription.setOrder(displayOrder);
        feedSubscription.setUnreadCount(0);
        feedSubscription.setTitle(title);
        String subscriptionId = subscriptionDao.create(feedSubscription);

        // Create initial article subscriptions for this user
        EntityManagerUtil.flush();
        UserArticleService userArticleService = AppContext.getInstance()
                .getFeedService().getSyncManager().getFeedSyncProcessor().getArticleManager().getUserArticleService();
        userArticleService.createInitialUserArticle(userId, feedSubscription);

        return subscriptionId;
    }

    public void updateSubscription(String userId, String subscriptionId, String title, String categoryId, Integer order) throws JSONException {
        title = ValidationUtil.validateLength(title, "name", 1, 100, true);
        FeedSubscriptionDao subscriptionDao = new FeedSubscriptionDao();
        FeedSubscription subscription = subscriptionDao.getFeedSubscription(subscriptionId, userId);
        if (subscription == null) {
            throw new ClientException("SubscriptionNotFound", MessageFormat.format("Subscription not found: {0}", subscriptionId));
        }
        if (StringUtils.isNotBlank(title)) {
            subscription.setTitle(title);
        }
        if (StringUtils.isNotBlank(categoryId)) {
            CategoryDao categoryDao = new CategoryDao();
            try {
                categoryDao.getCategory(categoryId, userId);
            } catch (NoResultException e) {
                throw new ClientException("CategoryNotFound", MessageFormat.format("Category not found: {0}", categoryId));
            }
            subscription.setCategoryId(categoryId);
        }
        subscriptionDao.update(subscription);
        if (order != null) {
            subscriptionDao.reorder(subscription, order);
        }
    }

    public void markAsRead(String userId, String subscriptionId) throws JSONException {
        FeedSubscriptionDao subscriptionDao = new FeedSubscriptionDao();
        FeedSubscription subscription = subscriptionDao.getFeedSubscription(subscriptionId, userId);
        if (subscription == null) {
            throw new ClientException("SubscriptionNotFound", MessageFormat.format("Subscription not found: {0}", subscriptionId));
        }
        UserArticleDao userArticleDao = new UserArticleDao();
        userArticleDao.markAsRead(new UserArticleCriteria()
                .setUserId(userId)
                .setSubscribed(true)
                .setFeedSubscriptionId(subscriptionId));
        subscriptionDao.updateUnreadCount(subscription.getId(), 0);
    }

    public void deleteSubscription(String userId, String subscriptionId) throws JSONException {
        FeedSubscriptionDao subscriptionDao = new FeedSubscriptionDao();
        FeedSubscription subscription = subscriptionDao.getFeedSubscription(subscriptionId, userId);
        if (subscription == null) {
            throw new ClientException("SubscriptionNotFound", MessageFormat.format("Subscription not found: {0}", subscriptionId));
        }
        subscriptionDao.delete(subscriptionId);
    }

    // --- Private helper methods for assembling JSON responses ---

    private JSONObject buildSubscriptionListResponse(String userId, List<FeedSubscriptionDto> subscriptions, boolean unread) throws JSONException {
        // Retrieve the root category and initialize its JSON representation.
        CategoryDao categoryDao = new CategoryDao();
        Category rootCategory = categoryDao.getRootCategory(userId);
        JSONObject rootCategoryJson = new JSONObject();
        rootCategoryJson.put("id", rootCategory.getId());

        // JSONArray categoriesArray = new JSONArray();
        List<JSONObject> categoriesArray = new ArrayList<JSONObject>();
        categoriesArray.add(rootCategoryJson);
        String oldCategoryId = null;
        JSONObject currentCategory = rootCategoryJson;
        int totalUnreadCount = 0;
        int categoryUnreadCount = 0;

        // Loop over subscriptions and group them by category
        for (FeedSubscriptionDto sub : subscriptions) {
            String categoryId = sub.getCategoryId();
            String categoryParentId = sub.getCategoryParentId();
            if (!categoryId.equals(oldCategoryId)) {
                if (categoryParentId != null) {
                    if(currentCategory != rootCategoryJson) {
                        currentCategory.put("unread_count", categoryUnreadCount);
                        // appendCategory(rootCategoryJson, currentCategory);
                        JsonUtil.append(rootCategoryJson, "categories", currentCategory);
                    }
                    currentCategory = new JSONObject();
                    currentCategory.put("id", categoryId);
                    currentCategory.put("name", sub.getCategoryName());
                    currentCategory.put("folded", sub.isCategoryFolded());
                    currentCategory.put("subscriptions", new JSONArray());
                    categoryUnreadCount = 0;
                }
            }
            JSONObject subscriptionJson = new JSONObject();
            subscriptionJson.put("id", sub.getId());
            subscriptionJson.put("title", sub.getFeedSubscriptionTitle());
            subscriptionJson.put("url", sub.getFeedRssUrl());
            subscriptionJson.put("unread_count", sub.getUnreadUserArticleCount());
            subscriptionJson.put("sync_fail_count", sub.getSynchronizationFailCount());
            JsonUtil.append(currentCategory, "subscriptions", subscriptionJson);
            // currentCategory.getJSONArray("subscriptions").put(subscriptionJson);

            oldCategoryId = categoryId;
            categoryUnreadCount += sub.getUnreadUserArticleCount();
            totalUnreadCount += sub.getUnreadUserArticleCount();
        }
        if (currentCategory != rootCategoryJson) {
            currentCategory.put("unread_count", categoryUnreadCount);
            // appendCategory(rootCategoryJson, currentCategory);
            JsonUtil.append(rootCategoryJson, "categories", currentCategory);
        }

        // When not filtering by unread, add all categories even if they have no subscriptions
        // if (!unread) {
        //     List<Category> allCategories = categoryDao.findSubCategory(rootCategory.getId(), userId);
        //     JSONArray categoryArrayJson = rootCategoryJson.optJSONArray("categories");
        //     // JSONArray fullCategoryList = new JSONArray();
        //     List<JSONObject> fullCategoryList = new ArrayList<JSONObject>();
        //     int i = 0;
        //     for (Category cat : allCategories) {
        //         if (categoryArrayJson != null && i < categoryArrayJson.length() && categoryArrayJson.getJSONObject(i).getString("id").equals(cat.getId())) {
        //             currentCategory = categoryArrayJson.getJSONObject(i++);
        //         } else{
        //             currentCategory = new JSONObject();
        //             currentCategory.put("id", cat.getId());
        //             currentCategory.put("name", cat.getName());
        //             currentCategory.put("folded", cat.isFolded());
        //             currentCategory.put("unread_count", 0);
        //         }
        //         fullCategoryList.add(currentCategory);
        //     }
        //     rootCategoryJson.put("categories", fullCategoryList);
        // }

        // JSONObject response = new JSONObject();
        // response.put("categories", categoriesArray);
        // response.put("unread_count", totalUnreadCount);
        // return response;

        // Add the categories without subscriptions
        List<Category> tree = categoryDao.buildCategoryTree(rootCategory.getId(), userId);
        JSONArray fullTreeJson = new JSONArray();
        for (Category cat : tree) {
            JSONObject catJson = buildCategoryWithSubscriptions(cat, subscriptions);
            fullTreeJson.put(catJson);
        }
        rootCategoryJson.put("categories", fullTreeJson);
                
        JSONObject response = new JSONObject();
        response.put("categories", categoriesArray);
        response.put("unread_count", totalUnreadCount);
        // return Response.ok().entity(response).build();
        return response;
    }
    
    private JSONObject buildCategoryWithSubscriptions(Category category, List<FeedSubscriptionDto> feedList) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", category.getId());
        json.put("name", category.getName());
        json.put("folded", category.isFolded());
        json.put("total_count", 0); // NEW FIELD
    
        JSONArray subs = new JSONArray();
        for (FeedSubscriptionDto sub : feedList) {
            if (sub.getCategoryId().equals(category.getId())) {
                JSONObject subJson = new JSONObject();
                subJson.put("id", sub.getId());
                subJson.put("title", sub.getFeedSubscriptionTitle());
                subJson.put("url", sub.getFeedRssUrl());
                subJson.put("unread_count", sub.getUnreadUserArticleCount());
                subJson.put("sync_fail_count", sub.getSynchronizationFailCount());
                subJson.put("total_count", 0); // NEW FIELD
                subs.put(subJson);
            }
        }
        json.put("subscriptions", subs);
    
        JSONArray childCats = new JSONArray();
        for (Category child : category.getChildren()) {
            childCats.put(buildCategoryWithSubscriptions(child, feedList));
        }
        json.put("categories", childCats);
        return json;
    }
    
           
       
    // }

    private void appendCategory(JSONObject root, JSONObject category) throws JSONException {
        JSONArray categories = root.optJSONArray("categories");
        if (categories == null) {
            categories = new JSONArray();
            root.put("categories", categories);
        }
        categories.put(category);
    }

    private JSONObject buildSubscriptionDetailResponse(FeedSubscriptionDto subscription, List<UserArticleDto> articles) throws JSONException {
        JSONObject response = new JSONObject();
        JSONObject subscriptionJson = new JSONObject();
        subscriptionJson.put("title", subscription.getFeedSubscriptionTitle());
        subscriptionJson.put("feed_title", subscription.getFeedTitle());
        subscriptionJson.put("url", subscription.getFeedUrl());
        subscriptionJson.put("rss_url", subscription.getFeedRssUrl());
        subscriptionJson.put("description", subscription.getFeedDescription());
        subscriptionJson.put("category_id", subscription.getCategoryId());
        subscriptionJson.put("category_name", subscription.getCategoryName());
        subscriptionJson.put("create_date", subscription.getCreateDate().getTime());
        response.put("subscription", subscriptionJson);

        List<JSONObject> articlesArray = new ArrayList<JSONObject>();
        for (UserArticleDto article : articles) {
            articlesArray.add(ArticleAssembler.asJson(article));
        }
        response.put("articles", articlesArray);
        return response;
    }
}
