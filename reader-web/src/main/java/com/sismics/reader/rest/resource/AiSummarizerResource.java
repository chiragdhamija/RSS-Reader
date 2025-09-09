package com.sismics.reader.rest.resource;

import com.sismics.reader.core.dao.jpa.CategoryDao;
import com.sismics.reader.core.dao.jpa.FeedSubscriptionDao;
import com.sismics.reader.core.dao.jpa.UserArticleDao;
import com.sismics.reader.core.dao.jpa.criteria.FeedSubscriptionCriteria;
import com.sismics.reader.core.dao.jpa.criteria.UserArticleCriteria;
import com.sismics.reader.core.dao.jpa.dto.FeedSubscriptionDto;
import com.sismics.reader.core.dao.jpa.dto.UserArticleDto;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.util.JsonUtil;
import com.sismics.reader.core.model.jpa.*;
import com.sismics.reader.core.util.jpa.PaginatedList;
import com.sismics.reader.core.util.jpa.PaginatedLists;
import com.sismics.reader.rest.assembler.ArticleAssembler;
import com.sismics.reader.core.ai.adapter.AiSummarizerAdapter;
import com.sismics.reader.core.ai.adapter.GeminiAiSummarizerAdapter;


import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.json.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.*;

/**
 * AI Summarizer REST resources.
 *
 * @author adityamishra
 */
@Path("/aisummary")
public class AiSummarizerResource extends BaseResource {
    // here it can be any AI , we just named our variables according to Gemini
    private static final String GEMINI_API_URL = "...";
    private static final String GEMINI_API_KEY = "...";
    // private static final int DEFAULT_ARTICLE_LIMIT = 5;
    // private static final int MAX_CATEGORY_DEPTH = 5;

    /**
     * Generates an AI summary.
     *
     * @return Response containing the summary
     * @throws JSONException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response generateSummary() throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        String userid = principal.getId();
        System.out.println("l32 AiSummarizerResource userid: " + userid);
        FeedSubscriptionCriteria feedSubscriptionCriteria = new FeedSubscriptionCriteria()
                .setUserId(principal.getId())
                .setUnread(false);

        FeedSubscriptionDao feedSubscriptionDao = new FeedSubscriptionDao();
        List<FeedSubscriptionDto> feedSubscriptionList = feedSubscriptionDao.findByCriteria(feedSubscriptionCriteria);
        System.out.println("l56 AiSummarizerResource feedSubscriptionList: " + feedSubscriptionList);
        CategoryDao categoryDao = new CategoryDao();
        Category rootCategory = categoryDao.getRootCategory(principal.getId());
        JSONObject rootCategoryJson = new JSONObject();
        rootCategoryJson.put("id", rootCategory.getId());

        // Construct the response
        List<JSONObject> rootCategories = new ArrayList<JSONObject>();
        rootCategories.add(rootCategoryJson);
        String oldCategoryId = null;
        JSONObject categoryJson = rootCategoryJson;
        int totalUnreadCount = 0;
        int categoryUnreadCount = 0;
        for (FeedSubscriptionDto feedSubscription : feedSubscriptionList) {
            String categoryId = feedSubscription.getCategoryId();
            String categoryParentId = feedSubscription.getCategoryParentId();

            if (!categoryId.equals(oldCategoryId)) {
                if (categoryParentId != null) {
                    if (categoryJson != rootCategoryJson) {
                        categoryJson.put("unread_count", categoryUnreadCount);
                        JsonUtil.append(rootCategoryJson, "categories", categoryJson);
                    }
                    categoryJson = new JSONObject();
                    categoryJson.put("id", categoryId);
                    categoryJson.put("name", feedSubscription.getCategoryName());
                    categoryJson.put("folded", feedSubscription.isCategoryFolded());
                    categoryJson.put("subscriptions", new JSONArray());
                    categoryUnreadCount = 0;
                }
            }
            JSONObject subscription = new JSONObject();
            subscription.put("id", feedSubscription.getId());
            subscription.put("title", feedSubscription.getFeedSubscriptionTitle());
            subscription.put("url", feedSubscription.getFeedRssUrl());
            subscription.put("unread_count", feedSubscription.getUnreadUserArticleCount());
            subscription.put("sync_fail_count", feedSubscription.getSynchronizationFailCount());
            JsonUtil.append(categoryJson, "subscriptions", subscription);

            oldCategoryId = categoryId;
            categoryUnreadCount += feedSubscription.getUnreadUserArticleCount();
            totalUnreadCount += feedSubscription.getUnreadUserArticleCount();
        }
        if (categoryJson != rootCategoryJson) {
            categoryJson.put("unread_count", categoryUnreadCount);
            JsonUtil.append(rootCategoryJson, "categories", categoryJson);
        }

        // Add the categories without subscriptions
        List<Category> tree = categoryDao.buildCategoryTree(rootCategory.getId(), principal.getId());
        JSONArray fullTreeJson = new JSONArray();
        for (Category cat : tree) {
            JSONObject catJson = buildCategoryWithSubscriptions(cat, feedSubscriptionList);
            fullTreeJson.put(catJson);
        }
        rootCategoryJson.put("categories", fullTreeJson);

        List<List<Map<String, Object>>> levels = levelOrderTraversal(rootCategoryJson);
        System.out.println("l171 AiSummarizerResource levels: " + levels);
        String prompt = "Generate a Summary of below feed. Prioritise the articles that are a part of a lower level. These should be given more importance while summarising.";
        int levelCount = 1;
        for (List<Map<String, Object>> level : levels) {
            prompt += "Level " + levelCount + ": ";
            for (Map<String, Object> node : level) {
                Object SubscriptionId = node.get("id");
                FeedSubscriptionCriteria feedSubscriptionCriteriatraverse = new FeedSubscriptionCriteria()
                        .setId(SubscriptionId.toString())
                        .setUserId(principal.getId());
                FeedSubscriptionDao feedSubscriptionDaotraverse = new FeedSubscriptionDao();
                List<FeedSubscriptionDto> feedSubscriptionListtraverse = feedSubscriptionDaotraverse.findByCriteria(feedSubscriptionCriteriatraverse);
                if (feedSubscriptionList.isEmpty()) {
                    throw new ClientException("SubscriptionNotFound",
                            MessageFormat.format("Subscription not found: {0}", SubscriptionId));
                }
                FeedSubscriptionDto feedSubscriptiontraverse = feedSubscriptionListtraverse.iterator().next();

                // Get the articles
                UserArticleDao userArticleDaotraverse = new UserArticleDao();
                UserArticleCriteria userArticleCriteriatraverse = new UserArticleCriteria()
                        .setUnread(false)
                        .setUserId(principal.getId())
                        .setSubscribed(true)
                        .setVisible(true)
                        .setFeedId(feedSubscriptiontraverse.getFeedId());
                

                PaginatedList<UserArticleDto> paginatedList = PaginatedLists.create(3, null);
                userArticleDaotraverse.findByCriteria(paginatedList, userArticleCriteriatraverse, null, null);
                System.out.println("l219 AiSummarizerResource paginatedList: " + paginatedList);
                
                for (UserArticleDto userArticle : paginatedList.getResultList()) {
                    prompt += userArticle.getArticleTitle() + ": " + "\n"+ userArticle.getArticleDescription() + "\n";
                }
                
            }
            prompt += "\n";
        }
        prompt +="Ensure that the summary is formatted well for readiblity. Divide the segments into relevant subdivisions through bullet points .Output the summary only. Do not add any additional text. Output should contain proper spaco=ing and newline etc.";
        // output text
        System.out.println("l238 AiSummarizerResource prompt: " + prompt);
        String summaryText;
        try {
            AiSummarizerAdapter summarizerAdapter = new GeminiAiSummarizerAdapter();
            summaryText = summarizerAdapter.generateSummary(prompt);
        } catch (Exception e) {
            System.err.println("Error calling AI summarizer adapter: " + e.getMessage());
            e.printStackTrace();
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Error generating AI summary: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse).build();
        }


        // Create response object
        JSONObject response2 = new JSONObject();
        response2.put("status", "ok");
        response2.put("summary", summaryText);

        return Response.ok().entity(response2).build();
    }

    private JSONObject buildCategoryWithSubscriptions(Category category, List<FeedSubscriptionDto> feedList)
            throws JSONException {
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

    public List<List<Map<String, Object>>> levelOrderTraversal(JSONObject rootJson) throws JSONException {
        // Initialize result - a list of lists, where each inner list contains
        // subscriptions from one level
        List<List<Map<String, Object>>> result = new ArrayList<>();

        // Use a queue for level order traversal
        Queue<JSONObject> queue = new LinkedList<>();
        queue.offer(rootJson);

        while (!queue.isEmpty()) {
            int levelSize = queue.size();
            List<Map<String, Object>> currentLevelSubscriptions = new ArrayList<>();

            // Process all nodes at the current level
            for (int i = 0; i < levelSize; i++) {
                JSONObject current = queue.poll();

                // Add subscriptions from this node to the current level list
                if (current.has("subscriptions") && current.getJSONArray("subscriptions").length() > 0) {
                    JSONArray subs = current.getJSONArray("subscriptions");
                    for (int j = 0; j < subs.length(); j++) {
                        // Convert each subscription JSONObject to a Map
                        JSONObject subJson = subs.getJSONObject(j);
                        Map<String, Object> subMap = jsonObjectToMap(subJson);
                        currentLevelSubscriptions.add(subMap);
                    }
                }

                // Add child categories to the queue for the next level
                if (current.has("categories")) {
                    JSONArray categories = current.getJSONArray("categories");
                    for (int j = 0; j < categories.length(); j++) {
                        queue.offer(categories.getJSONObject(j));
                    }
                }
            }

            // Add the current level's subscriptions to the result
            if (!currentLevelSubscriptions.isEmpty()) {
                result.add(currentLevelSubscriptions);
            }
        }

        return result;
    }

    // Helper method to convert JSONObject to Map
    private Map<String, Object> jsonObjectToMap(JSONObject json) throws JSONException {
        Map<String, Object> map = new HashMap<>();
        Iterator<String> keys = json.keys();

        while (keys.hasNext()) {
            String key = keys.next();
            Object value = json.get(key);
            map.put(key, value);
        }

        return map;
    }


}