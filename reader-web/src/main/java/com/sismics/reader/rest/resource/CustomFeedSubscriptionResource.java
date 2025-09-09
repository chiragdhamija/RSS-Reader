package com.sismics.reader.rest.resource;

import com.sismics.reader.core.dao.jpa.CustomArticleDao;
import com.sismics.reader.core.dao.jpa.CustomFeedDao;
import com.sismics.reader.core.dao.jpa.CustomFeedSubscriptionDao;
import com.sismics.reader.core.dao.jpa.FeedSubscriptionDao;
import com.sismics.reader.core.dao.jpa.UserArticleDao;
import com.sismics.reader.core.dao.jpa.criteria.CustomArticleCriteria;
import com.sismics.reader.core.dao.jpa.criteria.CustomFeedCriteria;
import com.sismics.reader.core.dao.jpa.criteria.CustomFeedSubscriptionCriteria;
import com.sismics.reader.core.dao.jpa.criteria.FeedSubscriptionCriteria;
import com.sismics.reader.core.dao.jpa.criteria.UserArticleCriteria;
import com.sismics.reader.core.dao.jpa.dto.CustomArticleDto;
import com.sismics.reader.core.dao.jpa.dto.CustomFeedDto;
import com.sismics.reader.core.model.jpa.CustomArticle;
import com.sismics.reader.core.dao.jpa.dto.CustomFeedSubscriptionDto;
import com.sismics.reader.core.dao.jpa.dto.FeedSubscriptionDto;
import com.sismics.reader.core.dao.jpa.dto.UserArticleDto;
import com.sismics.reader.core.model.jpa.FeedSubscription;
import com.sismics.reader.core.model.jpa.CustomFeedSubscription;
import com.sismics.reader.core.util.EntityManagerUtil;
import com.sismics.reader.core.util.jpa.PaginatedList;
import com.sismics.reader.core.util.jpa.PaginatedLists;
import com.sismics.reader.rest.assembler.ArticleAssembler;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.security.Principal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Custom feed subscription REST resource.
 * Handles operations related to custom feed subscriptions.
 * 
 * @author adityamishra
 */
@Path("/customfeedsubscription")
public class CustomFeedSubscriptionResource extends BaseResource {

    /**
     * Returns all custom feeds that exist.
     * 
     * @param userId ID of the user
     * @return Response
     */
    @GET
    @Path("/all_custom_feeds")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listCustomFeeds() throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        System.out.println("l59 in all custom feeds");
        CustomFeedDao customFeedDao = new CustomFeedDao();
        CustomFeedCriteria criteria = new CustomFeedCriteria();
        List<CustomFeedDto> customFeedDtoList = customFeedDao.findByCriteria(criteria);
        // Build the response
        JSONObject response = new JSONObject();
        response.put("status", "ok");

        JSONArray items = new JSONArray();
        for (CustomFeedDto customFeedDto : customFeedDtoList) {
            JSONObject item = new JSONObject();
            item.put("id", customFeedDto.getId());
            item.put("title", customFeedDto.getTitle());
            item.put("description", customFeedDto.getDescription());
            items.put(item);
        }
        response.put("custom_feeds", items);
        return Response.ok().entity(response).build();
    }
    
    /**
     * Returns all articles in a custom feed when the show button is clicked.
     * 
     * @param customFeedId ID of the custom feed
     * @return Response
     */
    @POST
    @Path("/show")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listArticles(
            @FormParam("feed_id") String customFeedId) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        System.out.println("l114 in show all articles in custom feed");
        // Shailender Stacked -> Fuck this whole thing. Just make a direct call to table thru entitiy manager for all articles having given feedID
        // Validate inputs
        if (customFeedId == null || customFeedId.trim().isEmpty()) {
            throw new ClientException("ValidationError", "Custom Feed ID must not be empty");
        }
        CustomArticleDao cad = new CustomArticleDao();
        List<CustomArticle> customArticleList = cad.listByCustomFeedId(customFeedId);
 
        // Build the response
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        JSONArray articles = new JSONArray();
        for (CustomArticle userArticle : customArticleList) {
            JSONObject article = new JSONObject();
            article.put("id", userArticle.getId());
            article.put("title", userArticle.getTitle());
            article.put("description", userArticle.getDescription());
            article.put("url", userArticle.getUrl());
            articles.put(article);
        }
        response.put("articles", articles);
        return Response.ok().entity(response).build();
    }

    /**
     * Subscribe to a custom feed
     * 
     * @param feedId
     * @return Response
     */
    @POST
    @Path("/subscribe")
    @Produces(MediaType.APPLICATION_JSON)
    public Response subscribe(
            @FormParam("custom_feed_id") String feedId) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        System.out.println("l138 feedId = " + feedId);
        // Check if feedId is empty
        if (feedId == null || feedId.trim().isEmpty()) {
            throw new ClientException("ValidationError", "Custom Feed ID must not be empty");
        }
        System.out.println("l143");

        // Check if already subscribed
        CustomFeedSubscriptionDao customFeedSubscriptionDao = new CustomFeedSubscriptionDao();
        CustomFeedSubscriptionCriteria customFeedSubscriptionCriteria = new CustomFeedSubscriptionCriteria()
                .setUserId(principal.getId())
                .setFeedId(feedId);
        List<CustomFeedSubscriptionDto> customFeedSubscriptionList = customFeedSubscriptionDao.findByCriteria(customFeedSubscriptionCriteria);
        if (customFeedSubscriptionList.size() > 0) {
            System.out.println("l152");
            throw new ClientException("AlreadyExists", "Already subscribed to this feed");
        }
        System.out.println("l155");

        CustomFeedSubscriptionDao feedSubscriptionDao = new CustomFeedSubscriptionDao();
        CustomFeedSubscription feedSubscription = new CustomFeedSubscription();
        feedSubscription.setUserId(principal.getId());
        feedSubscription.setFeedId(feedId);
        System.out.println("l161");
        feedSubscriptionDao.create(feedSubscription);
        System.out.println("l165");

        // Shailender stacked
        EntityManagerUtil.flush();
        System.out.println("l167");

        // Build the response
        JSONObject response = new JSONObject();
        response.put("status", "ok"); 
        return Response.ok().entity(response).build();
    }

    /**
     * Unsubscribe from a custom feed
     * 
     * @param feedId
     * @return Response
     */
    @Path("/unsubscribe")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response unsubscribe(
            @FormParam("custom_feed_id") String feedId) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        System.out.println("l218 in unsubscribe from custom feed");
        // Check if feedId is empty
        if (feedId == null || feedId.trim().isEmpty()) {
            throw new ClientException("ValidationError", "Custom Feed ID must not be empty");
        }
        // Check if already not subscribed
        CustomFeedSubscriptionDao customFeedSubscriptionDao = new CustomFeedSubscriptionDao();
        CustomFeedSubscriptionCriteria customFeedSubscriptionCriteria = new CustomFeedSubscriptionCriteria()
                .setUserId(principal.getId())
                .setFeedId(feedId);
        List<CustomFeedSubscriptionDto> customFeedSubscriptionList = customFeedSubscriptionDao.findByCriteria(customFeedSubscriptionCriteria);
        if (customFeedSubscriptionList.size() == 0) {
            throw new ClientException("AlreadyExists", "Already not subscribed to this feed");
        }

        CustomFeedSubscriptionDao feedSubscriptionDao = new CustomFeedSubscriptionDao();
        feedSubscriptionDao.delete(feedId);
        // Build the response
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }

    @Path("/subscribed_feeds")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSubscribedFeeds() throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        System.out.println("l221");
        // Get all feed subscriptions for the current user
        FeedSubscriptionDao feedSubscriptionDao = new FeedSubscriptionDao();
        FeedSubscriptionCriteria criteria = new FeedSubscriptionCriteria()
                .setUserId(principal.getId());
        
        List<FeedSubscriptionDto> feedSubscriptionDtoList = feedSubscriptionDao.findByCriteria(criteria);
        System.out.println("l228");
        
        // Build the response
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        
        JSONArray items = new JSONArray();
        for (FeedSubscriptionDto feedSubscriptionDto : feedSubscriptionDtoList) {
            JSONObject item = new JSONObject();
            item.put("id", feedSubscriptionDto.getId());
            item.put("feed_id", feedSubscriptionDto.getFeedId());
            item.put("feed_title", feedSubscriptionDto.getFeedTitle());
            item.put("feed_url", feedSubscriptionDto.getFeedUrl());
            item.put("feed_description", feedSubscriptionDto.getFeedDescription());
            item.put("category_id", feedSubscriptionDto.getCategoryId());
            item.put("category_name", feedSubscriptionDto.getCategoryName());
            items.put(item);
        }
        
        response.put("subscribed_feeds", items);
        return Response.ok().entity(response).build();
    }

    /**
     * Returns all articles from feeds the current user has subscribed to.
     * 
     * @param limit Maximum number of articles to return
     * @param offset Offset for pagination
     * @return Response with all user's articles
     */
    @Path("/user_articles")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserArticles() throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Get all feed IDs the user is subscribed to
        CustomFeedSubscriptionDao feedSubscriptionDao = new CustomFeedSubscriptionDao();
        CustomFeedSubscriptionCriteria subscriptionCriteria = new CustomFeedSubscriptionCriteria()
                .setUserId(principal.getId());
        
        List<CustomFeedSubscriptionDto> subscriptions = feedSubscriptionDao.findByCriteria(subscriptionCriteria);
        
        System.out.println("l273 fetching all articles for all custom feeds");
        if (subscriptions.isEmpty()) {
            // User has no subscriptions, return empty list
            System.out.println("l276");
            JSONObject response = new JSONObject();
            response.put("status", "ok");
            response.put("articles", new JSONArray());
            response.put("total", 0);
            return Response.ok().entity(response).build();
        }
        
        System.out.println("l284");
        for (CustomFeedSubscriptionDto subscription : subscriptions) {
            System.out.println("l286 subscription = " + subscription);
        }
    
        // Get all articles for each feed
        CustomArticleDao articleDao = new CustomArticleDao();
        List<CustomArticleDto> allArticles = new ArrayList<>();
        
        System.out.println("l288");
        // For each subscription, get the articles
        for (CustomFeedSubscriptionDto subscription : subscriptions) {
            System.out.println("l296");
            CustomArticleCriteria articleCriteria = new CustomArticleCriteria()
                    .setCustomFeedId(subscription.getFeedId());
            
            List<CustomArticleDto> feedArticles = articleDao.findByCriteria(articleCriteria);
            allArticles.addAll(feedArticles);
            System.out.println("l302 feedArticles.size() = " + feedArticles.size());
        }
        System.out.println("l297");
        // Build the response
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        
        JSONArray items = new JSONArray();
        for (CustomArticleDto articleDto : allArticles) {
            JSONObject item = new JSONObject();
            item.put("id", articleDto.getId());
            item.put("title", articleDto.getTitle());
            item.put("description", articleDto.getDescription());
            item.put("url", articleDto.getUrl());
            item.put("guid", articleDto.getGuid());
            item.put("feed_id", articleDto.getCustomFeedId());
            items.put(item);
        }


        response.put("articles", items);

        for (CustomArticleDto articleDto : allArticles) {
            System.out.println("l318 articleDto = " + articleDto);
        }

        // response.put("total", allArticles.size());
        return Response.ok().entity(response).build();
    
    }
}