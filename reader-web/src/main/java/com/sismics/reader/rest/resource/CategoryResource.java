package com.sismics.reader.rest.resource;

import com.sismics.reader.core.dao.jpa.CategoryDao;
import com.sismics.reader.core.dao.jpa.FeedSubscriptionDao;
import com.sismics.reader.core.dao.jpa.UserArticleDao;
import com.sismics.reader.core.dao.jpa.criteria.FeedSubscriptionCriteria;
import com.sismics.reader.core.dao.jpa.criteria.UserArticleCriteria;
import com.sismics.reader.core.dao.jpa.dto.FeedSubscriptionDto;
import com.sismics.reader.core.dao.jpa.dto.UserArticleDto;
import com.sismics.reader.core.model.jpa.Category;
import com.sismics.reader.core.model.jpa.FeedSubscription;
import com.sismics.reader.core.util.jpa.PaginatedList;
import com.sismics.reader.core.util.jpa.PaginatedLists;
import com.sismics.reader.rest.assembler.ArticleAssembler;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.util.ValidationUtil;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.persistence.NoResultException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.codehaus.jettison.json.JSONArray;


/**
 * Category REST resources.
 * 
 * @author jtremeaux
 */
@Path("/category")
public class CategoryResource extends BaseResource {
    /**
     * Returns all categories.
     * 
     * @return Response
     */
    // CategoryResource.java
@GET
@Produces(MediaType.APPLICATION_JSON)
public Response list() throws JSONException {
    if (!authenticate()) {
        throw new ForbiddenClientException();
    }

    CategoryDao categoryDao = new CategoryDao();
    Category rootCategory = categoryDao.getRootCategory(principal.getId());
    List<Category> children = categoryDao.buildCategoryTree(rootCategory.getId(), principal.getId());
    rootCategory.setChildren(children); // <- 💡 Important!

    JSONObject response = new JSONObject();
    JSONArray categoriesArray = new JSONArray();
    categoriesArray.put(buildCategoryJson(rootCategory)); // <- Recursive
    response.put("categories", categoriesArray);
    return Response.ok().entity(response).build();
}

    // Recursive
    private JSONObject buildCategoryJson(Category category) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", category.getId());
        json.put("name", category.getName());
        json.put("folded", category.isFolded());

        JSONArray childrenJson = new JSONArray();
        for (Category child : category.getChildren()) {
            childrenJson.put(buildCategoryJson(child));
        }
        json.put("categories", childrenJson);
        return json;
    }

    /**
     * Returns all articles in a category.
     * 
     * @param id Category ID
     * @param unread Returns only unread articles
     * @param limit Page limit
     * @param afterArticle Start the list after this article
     * @return Response
     */
    @GET
    @Path("{id: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(
            @PathParam("id") String id,
            @QueryParam("unread") boolean unread,
            @QueryParam("limit") Integer limit,
            @QueryParam("after_article") String afterArticle) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Get the category
        CategoryDao categoryDao = new CategoryDao();
        Category category;
        try {
            category = categoryDao.getCategory(id, principal.getId());
        } catch (NoResultException e) {
            throw new ClientException("CategoryNotFound", MessageFormat.format("Category not found: {0}", id));
        }

        // Get the articles
        UserArticleDao userArticleDao = new UserArticleDao();
        UserArticleCriteria userArticleCriteria = new UserArticleCriteria()
                .setUnread(unread)
                .setUserId(principal.getId())
                .setSubscribed(true)
                .setVisible(true);
        if (category.getParentId() != null) {
            userArticleCriteria.setCategoryId(id);
        }
        if (afterArticle != null) {
            // Paginate after this user article
            UserArticleCriteria afterArticleCriteria = new UserArticleCriteria()
                    .setUserArticleId(afterArticle)
                    .setUserId(principal.getId());
            List<UserArticleDto> userArticleDtoList = userArticleDao.findByCriteria(afterArticleCriteria);
            if (userArticleDtoList.isEmpty()) {
                throw new ClientException("ArticleNotFound", MessageFormat.format("Can't find user article {0}", afterArticle));
            }
            UserArticleDto userArticleDto = userArticleDtoList.iterator().next();

            userArticleCriteria.setArticlePublicationDateMax(new Date(userArticleDto.getArticlePublicationTimestamp()));
            userArticleCriteria.setArticleIdMax(userArticleDto.getArticleId());
        }

        PaginatedList<UserArticleDto> paginatedList = PaginatedLists.create(limit, null);
        userArticleDao.findByCriteria(paginatedList, userArticleCriteria, null, null);
        
        // Build the response
        JSONObject response = new JSONObject();

        List<JSONObject> articles = new ArrayList<JSONObject>();
        for (UserArticleDto userArticle : paginatedList.getResultList()) {
            articles.add(ArticleAssembler.asJson(userArticle));
        }
        response.put("articles", articles);

        return Response.ok().entity(response).build();
    }
    
    /**
     * Creates a new category.
     * 
     * @param name Category name
     * @return Response
     */
    @PUT
@Produces(MediaType.APPLICATION_JSON)
public Response add(
        @FormParam("name") String name,
        @FormParam("parent_id") String parentId) throws JSONException {
    if (!authenticate()) {
        throw new ForbiddenClientException();
    }

    // Validate input data
    name = ValidationUtil.validateLength(name, "name", 1, 100, false);

    CategoryDao categoryDao = new CategoryDao();

    // Determine parent category
    Category parentCategory;
    if (parentId == null || parentId.isEmpty()) {
        parentCategory = categoryDao.getRootCategory(principal.getId());
    } else {
        parentCategory = categoryDao.getCategory(parentId, principal.getId());
    }

    // Validate depth limit
    if (categoryDao.computeDepth(parentCategory, principal.getId()) > 5)  {
        throw new ClientException("MaxDepthExceeded", "Cannot exceed 5 levels of nesting");
    }
    

    // Get the display order
    int displayOrder = categoryDao.getCategoryCount(parentCategory.getId(), principal.getId());

    // Create the category
    Category category = new Category();
    category.setUserId(principal.getId());
    category.setParentId(parentCategory.getId());
    category.setName(name);
    category.setOrder(displayOrder);
    String categoryId = categoryDao.create(category);

    JSONObject response = new JSONObject();
    response.put("id", categoryId);
    return Response.ok().entity(response).build();
}


    /**
     * Deletes a category.
     * 
     * @param id Category ID
     * @return Response
     */
    @DELETE
@Path("{id: [a-z0-9\\-]+}")
@Produces(MediaType.APPLICATION_JSON)
public Response delete(@PathParam("id") String id) throws JSONException {
    if (!authenticate()) {
        throw new ForbiddenClientException();
    }

    // Get the category
    CategoryDao categoryDao = new CategoryDao();
    Category category;
    try {
        category = categoryDao.getCategory(id, principal.getId());
    } catch (NoResultException e) {
        throw new ClientException("CategoryNotFound", MessageFormat.format("Category not found: {0}", id));
    }

    // Move subscriptions to root category
    FeedSubscriptionDao feedSubscriptionDao = new FeedSubscriptionDao();
    List<FeedSubscription> feedSubscriptionList = feedSubscriptionDao.findByCategory(id);
    Category rootCategory = categoryDao.getRootCategory(principal.getId());
    for (FeedSubscription feedSubscription : feedSubscriptionList) {
        feedSubscription.setCategoryId(rootCategory.getId());
        feedSubscriptionDao.update(feedSubscription);
        feedSubscriptionDao.reorder(feedSubscription, 0);
    }

    // Move child categories to root
    List<Category> childCategories = categoryDao.findSubCategory(id, principal.getId());
    for (Category child : childCategories) {
        child.setParentId(rootCategory.getId());
        categoryDao.update(child);
    }

    // Delete the category
    categoryDao.delete(id);

    JSONObject response = new JSONObject();
    response.put("status", "ok");
    return Response.ok().entity(response).build();
}


    /**
     * Marks all articles in this category as read.
     * 
     * @param id Category ID
     * @return Response
     */
    @POST
    @Path("{id: [a-z0-9\\-]+}/read")
    @Produces(MediaType.APPLICATION_JSON)
    public Response read(
            @PathParam("id") String id) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Get the category
        Category category;
        try {
            category = new CategoryDao().getCategory(id, principal.getId());
        } catch (NoResultException e) {
            throw new ClientException("CategoryNotFound", MessageFormat.format("Category not found: {0}", id));
        }

        // Marks all articles as read in this category
        UserArticleDao userArticleDao = new UserArticleDao();
        userArticleDao.markAsRead(new UserArticleCriteria()
                .setUserId(principal.getId())
                .setSubscribed(true)
                .setCategoryId(id));

        FeedSubscriptionDao feedSubscriptionDao = new FeedSubscriptionDao();
        for (FeedSubscriptionDto feedSubscrition : feedSubscriptionDao.findByCriteria(new FeedSubscriptionCriteria()
                .setCategoryId(category.getId())
                .setUserId(principal.getId()))) {
            feedSubscriptionDao.updateUnreadCount(feedSubscrition.getId(), 0);
        }
        
        // Always return ok
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }

    /**
     * Updates the category.
     * 
     * @param id Category ID
     * @param name Category name
     * @param order Display order of this category
     * @param folded True if this category is folded in the subscriptions tree.
     * @return Response
     */
    @POST
@Path("{id: [a-z0-9\\-]+}")
@Produces(MediaType.APPLICATION_JSON)
public Response update(
        @PathParam("id") String id,
        @FormParam("name") String name,
        @FormParam("order") Integer order,
        @FormParam("folded") Boolean folded,
        @FormParam("parent_id") String parentId) throws JSONException {
    if (!authenticate()) {
        throw new ForbiddenClientException();
    }

    // Validate input data
    name = ValidationUtil.validateLength(name, "name", 1, 100, true);

    // Get the category
    CategoryDao categoryDao = new CategoryDao();
    Category category;
    try {
        category = categoryDao.getCategory(id, principal.getId());
    } catch (NoResultException e) {
        throw new ClientException("CategoryNotFound", MessageFormat.format("Category not found: {0}", id));
    }

    // Change parent category if provided
    if (parentId != null && !parentId.equals(category.getParentId())) {
        Category newParent = categoryDao.getCategory(parentId, principal.getId());

        // Ensure new parent is not a child of the category (prevent cyclic dependency)
        if (categoryDao.computeDepth(newParent, principal.getId()) > 5) {
            throw new ClientException("MaxDepthExceeded", "Cannot exceed 5 levels of nesting");
        }
        

        category.setParentId(newParent.getId());
    }

    // Update the category
    if (name != null) {
        category.setName(name);
    }
    if (folded != null) {
        category.setFolded(folded);
    }
    categoryDao.update(category);

    // Reorder categories
    if (order != null) {
        categoryDao.reorder(category, order);
    }

    JSONObject response = new JSONObject();
    response.put("status", "ok");
    return Response.ok().entity(response).build();
}

}
