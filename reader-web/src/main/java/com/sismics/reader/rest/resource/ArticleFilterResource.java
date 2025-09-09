package com.sismics.reader.rest.resource;

import com.sismics.reader.core.dao.jpa.UserArticleDao;
import com.sismics.reader.core.dao.jpa.criteria.UserArticleCriteria;
import com.sismics.reader.core.dao.jpa.dto.UserArticleDto;
import com.sismics.reader.core.service.filtering.ArticleFilterStrategy;
import com.sismics.reader.core.service.filtering.CombinedFilterStrategy;
import com.sismics.reader.core.util.jpa.PaginatedList;
import com.sismics.reader.core.util.jpa.PaginatedLists;
import com.sismics.reader.rest.assembler.ArticleAssembler;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Path("/mixed/selection")
public class ArticleFilterResource extends BaseResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBySelection(
            @QueryParam("ids") String ids,
            @QueryParam("starred") boolean starred,
            @QueryParam("unread") boolean unread,
            @QueryParam("limit") Integer limit,
            @QueryParam("after_article") String afterArticle) throws JSONException {
        
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Parse IDs from the query parameter
        List<String> selectedIds = parseSelectedIds(ids);
        
        // Create and configure the criteria for article selection
        UserArticleCriteria criteria = createBaseCriteria(starred, unread);
        
        // Apply the strategy to handle prefixed IDs
        applySelectionStrategy(criteria, selectedIds);
        
        // Handle pagination if specified
        handlePagination(criteria, afterArticle);
        
        // Fetch articles based on criteria
        PaginatedList<UserArticleDto> paginatedList = fetchArticles(criteria, limit);
        
        // Build and return the response
        return buildResponse(paginatedList);
    }
    
    /**
     * Parse comma-separated IDs into a list
     * 
     * @param ids Comma-separated list of IDs
     * @return List of selected IDs
     */
    private List<String> parseSelectedIds(String ids) {
        return Arrays.asList(ids.split(","));
    }
    
    /**
     * Create base criteria with user settings
     * 
     * @param starred Flag for starred articles
     * @param unread Flag for unread articles
     * @return Configured UserArticleCriteria
     */
    private UserArticleCriteria createBaseCriteria(boolean starred, boolean unread) {
        UserArticleCriteria criteria = new UserArticleCriteria()
                .setUserId(principal.getId())
                .setSubscribed(true)
                .setVisible(true);
        
        // Configure read/starred flags based on parameters
        if (starred) {
            criteria.setStarred(true);
            criteria.setUnread(false);
        } else if (unread) {
            criteria.setUnread(true);
            criteria.setStarred(false);
        } else {
            criteria.setUnread(false);
            criteria.setStarred(false);
        }
        
        return criteria;
    }
    
    /**
     * Apply the composite selection strategy to the criteria
     * 
     * @param criteria The criteria to modify
     * @param selectedIds List of selected IDs with prefixes
     */
    private void applySelectionStrategy(UserArticleCriteria criteria, List<String> selectedIds) {
        ArticleFilterStrategy strategy = new CombinedFilterStrategy();
        strategy.applySelection(criteria, selectedIds);
    }
    
    /**
     * Handle pagination based on the "after_article" parameter
     * 
     * @param criteria The criteria to modify
     * @param afterArticle ID of the article to paginate after
     */
    private void handlePagination(UserArticleCriteria criteria, String afterArticle) throws JSONException {
        if (afterArticle == null) {
            return;
        }
        
        UserArticleCriteria afterArticleCriteria = new UserArticleCriteria()
                .setUserArticleId(afterArticle)
                .setUserId(principal.getId());
        
        List<UserArticleDto> userArticleDtoList = new UserArticleDao().findByCriteria(afterArticleCriteria);
        if (userArticleDtoList.isEmpty()) {
            throw new ClientException("ArticleNotFound", 
                    MessageFormat.format("Can't find user article {0}", afterArticle));
        }
        
        UserArticleDto userArticleDto = userArticleDtoList.iterator().next();
        criteria.setArticlePublicationDateMax(new Date(userArticleDto.getArticlePublicationTimestamp()));
        criteria.setArticleIdMax(userArticleDto.getArticleId());
    }
    
    /**
     * Fetch articles based on the criteria
     * 
     * @param criteria The search criteria
     * @param limit Maximum number of results
     * @return Paginated list of articles
     */
    private PaginatedList<UserArticleDto> fetchArticles(UserArticleCriteria criteria, Integer limit) {
        PaginatedList<UserArticleDto> paginatedList = PaginatedLists.create(limit, null);
        new UserArticleDao().findByCriteria(paginatedList, criteria, null, null);
        return paginatedList;
    }
    
    /**
     * Build the JSON response from article results
     * 
     * @param paginatedList List of articles
     * @return HTTP response with JSON payload
     * @throws JSONException If JSON creation fails
     */
    private Response buildResponse(PaginatedList<UserArticleDto> paginatedList) throws JSONException {
        JSONObject response = new JSONObject();
        List<JSONObject> articles = new ArrayList<>();
        
        for (UserArticleDto article : paginatedList.getResultList()) {
            articles.add(ArticleAssembler.asJson(article));
        }
        
        response.put("articles", articles);
        return Response.ok().entity(response).build();
    }
}