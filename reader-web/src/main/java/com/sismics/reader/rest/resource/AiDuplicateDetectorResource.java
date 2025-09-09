package com.sismics.reader.rest.resource;

import com.sismics.reader.core.dao.jpa.ArticleDao;
import com.sismics.reader.core.dao.jpa.UserArticleDao;
import com.sismics.reader.core.dao.jpa.criteria.ArticleCriteria;
import com.sismics.reader.core.dao.jpa.dto.ArticleDto;
import com.sismics.reader.core.model.jpa.UserArticle;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;


import java.text.MessageFormat;
import java.util.*;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

// Stanford CoreNLP imports
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import com.sismics.reader.core.similarity.strategy.ArticleSimilarityStrategy;
import com.sismics.reader.core.similarity.strategy.NerBasedSimilarityStrategy;

/**
 * AI Duplicate Detector REST resources.
 *
 * @author adityamishra
 */
@Path("/ai_duplicate_detector")
public class AiDuplicateDetectorResource extends BaseResource {

    // Initialize Stanford CoreNLP pipeline once for efficiency
    private static final StanfordCoreNLP pipeline;
    private final ArticleSimilarityStrategy similarityStrategy = new NerBasedSimilarityStrategy(pipeline);

    
    static {
        // Set up properties for the pipeline
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");
        pipeline = new StanfordCoreNLP(props);
    }
    
    /**
     * Compares two articles for duplicity.
     *
     * @param article1Id ID of the first article
     * @param article2Id ID of the second article
     * @return Response containing the comparison result
     * @throws JSONException
     */
    @GET
    @Path("/compare_articles")
    @Produces(MediaType.APPLICATION_JSON)
    public Response compareArticles(
        @QueryParam("article1Id") String article1Id,
        @QueryParam("article2Id") String article2Id) throws JSONException {
        
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        UserArticleDao userArticleDao = new UserArticleDao();
        UserArticle userArticle = userArticleDao.getUserArticle(article1Id, principal.getId());
        if (userArticle == null) {
            throw new ClientException("ArticleNotFound", MessageFormat.format("Article not found: {0}", article1Id));
        }
        
        ArticleDto article1 = new ArticleDao().findFirstByCriteria(
            new ArticleCriteria().setId(userArticle.getArticleId()));
        System.out.println(article1.getTitle());
        System.out.println(article1.getDescription());
        
        userArticle = userArticleDao.getUserArticle(article2Id, principal.getId());
        if (userArticle == null) {
            throw new ClientException("ArticleNotFound", MessageFormat.format("Article not found: {0}", article2Id));
        }
        
        ArticleDto article2 = new ArticleDao().findFirstByCriteria(
            new ArticleCriteria().setId(userArticle.getArticleId()));
        System.out.println(article2.getTitle());
        System.out.println(article2.getDescription());
        
        System.out.println(userArticle);
        String userId = principal.getId();
        System.out.println("AiDuplicateDetectorResource userId: " + userId);
        System.out.println("Comparing articles with article ids: " + article1Id + " and " + article2Id);
        
        // Generate a similarity score between 0 and 100
        double similarityScore = similarityStrategy.calculateSimilarity(article1, article2);
        boolean isDuplicate = similarityScore > 50;
        
        // Create response object
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        response.put("isDuplicate", isDuplicate);
        response.put("similarityScore", Math.round(similarityScore * 10) / 10.0); // Round to 1 decimal place
        
        return Response.ok().entity(response).build();
    }
    

}