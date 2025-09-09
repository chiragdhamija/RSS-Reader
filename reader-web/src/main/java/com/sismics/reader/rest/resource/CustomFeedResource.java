package com.sismics.reader.rest.resource;

import com.sismics.reader.core.dao.jpa.ArticleDao;
import com.sismics.reader.core.dao.jpa.CustomArticleDao;
import com.sismics.reader.core.dao.jpa.CustomFeedDao;
import com.sismics.reader.core.dao.jpa.criteria.ArticleCriteria;
import com.sismics.reader.core.dao.jpa.criteria.CustomArticleCriteria;
import com.sismics.reader.core.dao.jpa.criteria.CustomFeedCriteria;
import com.sismics.reader.core.dao.jpa.dto.ArticleDto;
import com.sismics.reader.core.dao.jpa.dto.CustomFeedDto;
import com.sismics.reader.core.model.jpa.Article;
import com.sismics.reader.core.model.jpa.CustomArticle;
import com.sismics.reader.core.dao.jpa.dto.CustomArticleDto;
import com.sismics.reader.core.model.jpa.CustomFeed;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Custom feeds REST resource.
 * Returns feeds created by the current user.
 * 
 * @author adityamishra
 */
@Path("/customfeed")
public class CustomFeedResource extends BaseResource {

    /**
     * Creates a new custom feed.
     * 
     * @param title
     * @param description
     * @return
     */
    @Path("/create")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(
            @FormParam("title") String title,
            @FormParam("description") String description) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        if (title == null || title.trim().isEmpty()) {
            throw new ClientException("ValidationError", "Title must not be empty");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new ClientException("ValidationError", "Description must not be empty");
        }
        
        CustomFeedDao customFeedDao = new CustomFeedDao();
        CustomFeed customFeed = new CustomFeed();
        customFeed.setTitle(title);
        customFeed.setDescription(description);
        customFeed.setCustomUserId(principal.getId());

        customFeedDao.create(customFeed);
        System.out.println("l57 creating custom feed");
        
        // Build the response
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }

    /**
     * Deletes an existing custom feed.
     * 
     * @param feedId
     * @return
     */
    @Path("/{id: [a-z0-9\\-]+}/delete")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("id") String id) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        CustomFeedDao customFeedDao = new CustomFeedDao();
        customFeedDao.delete(id);
        System.out.println("l74 deleting custom feed");

        // Build the response
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }


    /**
     * Returns all custom feeds created by the current user.
     * 
     * @return Response
     */
    @Path("/my_custom_feeds")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll() throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        System.out.println("l98 getting all custom feeds");
        CustomFeedDao customFeedDao = new CustomFeedDao();
        CustomFeedCriteria criteria = new CustomFeedCriteria()
                    .setCustomUserId(principal.getId());

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
     * Add article to custom feed
     * 
     * @param feedId
     * @param articleId
     * @return Response
     */
    @Path("/add_article")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response addArticle(
            @FormParam("feed_id") String feedId, 
            @FormParam("article_id") String articleId) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        System.out.println("l136 adding article " + articleId + " to custom feed " + feedId);


        // Validate inputs
        if (feedId == null || feedId.trim().isEmpty()) {
            throw new ClientException("ValidationError", "Feed ID must not be empty");
        }
        if (articleId == null || articleId.trim().isEmpty()) {
            throw new ClientException("ValidationError", "Article ID must not be empty");
        }
        // Check if article already exists in the custom feed
        CustomArticleDao customArticleDao = new CustomArticleDao();
        CustomArticleCriteria customArticleCriteria = new CustomArticleCriteria()
                .setId(articleId)
                .setCustomFeedId(feedId);
        if (customArticleDao.findByCriteria(customArticleCriteria).size() > 0) {
            throw new ClientException("AlreadyExists", "Article already exists in the custom feed");
        }

        System.out.println("l166");
        // Retrieve the original article
        ArticleDao articleDao = new ArticleDao();
        ArticleCriteria articleCriteria = new ArticleCriteria()
                .setId(articleId);
        List<ArticleDto> articleDtos = articleDao.findByCriteria(articleCriteria);
        System.out.println("l172");
        
        if (articleDtos.isEmpty()) {
            System.out.println("l175");
            throw new ClientException("NotFound", "Original article not found");
        }
        ArticleDto articleDto = articleDtos.get(0);
        System.out.println("l178");

        CustomArticleDao cad = new CustomArticleDao();
        CustomArticle ca = new CustomArticle();
        ca.setId(articleId);
        ca.setCustomFeedId(feedId);
        ca.setTitle(articleDto.getTitle());
        ca.setUrl(articleDto.getUrl()); 
        ca.setDescription(articleDto.getDescription());
        ca.setCreateDate(articleDto.getCreateDate());
        cad.create(ca);

        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }

    /**
     * Remove article from custom feed
     * 
     * @param feedId
     * @param articleId
     * @return Response
     */
    @Path("/remove_article")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeArticle(
            @FormParam("feed_id") String feedId,
            @FormParam("article_id") String articleId) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Validate inputs
        if (feedId == null || feedId.trim().isEmpty()) {
            throw new ClientException("ValidationError", "Feed ID must not be empty");
        }
        if (articleId == null || articleId.trim().isEmpty()) {
            throw new ClientException("ValidationError", "Article ID must not be empty");
        }
        System.out.println("l160 removing article" + articleId + " from custom feed " + feedId);

        // Delete the custom article
        CustomArticleDao customArticleDao = new CustomArticleDao();
        CustomArticleCriteria customArticleCriteria = new CustomArticleCriteria()
                .setId(articleId)
                .setCustomFeedId(feedId);
        System.out.println("l244");
        List<CustomArticleDto> customArticleDtos = customArticleDao.findByCriteria(customArticleCriteria);

        System.out.println("Custom Articles found:");
        for (CustomArticleDto dto : customArticleDtos) {
            System.out.println("Orig ID: " + dto.getOrigId() + 
                             ", Feed ID: " + dto.getCustomFeedId() + 
                             ", (Article) ID: " + dto.getId());
        }

        System.out.println("l247");
        if (customArticleDtos.isEmpty()) {
            System.out.println("l250");
            throw new ClientException("NotFound", "Article not found in the custom feed.");
        }
        CustomArticleDto customArticleDto = customArticleDtos.get(0);
        // origId is the primary key
        // id is the article id, which can be present in multiple rows in the table
        customArticleDao.delete(customArticleDto.getOrigId());
        
        // Build the response
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }         
}