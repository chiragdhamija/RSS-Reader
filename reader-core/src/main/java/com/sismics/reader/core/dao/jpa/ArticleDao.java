package com.sismics.reader.core.dao.jpa;

import com.sismics.reader.core.dao.jpa.criteria.ArticleCriteria;
import com.sismics.reader.core.dao.jpa.dto.ArticleDto;
import com.sismics.reader.core.dao.jpa.mapper.ArticleMapper;
import com.sismics.reader.core.model.jpa.Article;
import com.sismics.reader.core.util.jpa.SortCriteria;
import com.sismics.util.context.ThreadLocalContext;
import com.sismics.util.jpa.BaseDao;
import com.sismics.util.jpa.DialectUtil;
import com.sismics.util.jpa.QueryParam;
import com.sismics.util.jpa.filter.FilterCriteria;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.*;

/**
 * Article DAO.
 * 
 * @author jtremeaux
 */
public class ArticleDao extends BaseDao<ArticleDto, ArticleCriteria> {

    @Override
    protected QueryParam getQueryParam(ArticleCriteria criteria, FilterCriteria filterCriteria) {
        List<String> criteriaList = new ArrayList<String>();
        Map<String, Object> parameterMap = new HashMap<String, Object>();

        StringBuilder sb = new StringBuilder("select a.ART_ID_C, a.ART_URL_C, a.ART_GUID_C, a.ART_TITLE_C, a.ART_CREATOR_C, a.ART_DESCRIPTION_C, a.ART_COMMENTURL_C, a.ART_COMMENTCOUNT_N, a.ART_ENCLOSUREURL_C, a.ART_ENCLOSURELENGTH_N, a.ART_ENCLOSURETYPE_C, a.ART_PUBLICATIONDATE_D, a.ART_CREATEDATE_D, a.ART_IDFEED_C ")
                .append("  from T_ARTICLE a ");

        // Adds search criteria
        criteriaList.add("a.ART_DELETEDATE_D is null");
        if (criteria.getId() != null) {
            criteriaList.add("a.ART_ID_C = :id");
            parameterMap.put("id", criteria.getId());
        }
        if (criteria.getGuidIn() != null) {
            criteriaList.add("a.ART_GUID_C in :guidIn");
            parameterMap.put("guidIn", criteria.getGuidIn());
        }
        if (criteria.getTitle() != null) {
            criteriaList.add("a.ART_TITLE_C = :title");
            parameterMap.put("title", criteria.getTitle());
        }
        if (criteria.getUrl() != null) {
            criteriaList.add("a.ART_URL_C = :url");
            parameterMap.put("url", criteria.getUrl());
        }
        if (criteria.getPublicationDateMin() != null) {
            criteriaList.add("a.ART_PUBLICATIONDATE_D > :publicationDateMax");
            parameterMap.put("publicationDateMax", criteria.getPublicationDateMin());
        }
        if (criteria.getFeedId() != null) {
            criteriaList.add("a.ART_IDFEED_C = :feedId");
            parameterMap.put("feedId", criteria.getFeedId());
        }

        SortCriteria sortCriteria = new SortCriteria("  order by a.ART_CREATEDATE_D asc");

        return new QueryParam(sb.toString(), criteriaList, parameterMap, sortCriteria, filterCriteria, new ArticleMapper());
    }

    /**
     * Creates a new article.
     * 
     * @param article Article to create
     * @return New ID
     */
    public String create(Article article) {
        // Create the UUID
        article.setId(UUID.randomUUID().toString());
        article.setCreateDate(new Date());

        // Create the article
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createNativeQuery("insert into T_ARTICLE(ART_ID_C, ART_IDFEED_C, ART_URL_C, ART_BASEURI_C, ART_GUID_C, ART_TITLE_C, ART_CREATOR_C, ART_DESCRIPTION_C, ART_COMMENTURL_C, ART_COMMENTCOUNT_N, ART_ENCLOSUREURL_C, ART_ENCLOSURELENGTH_N, ART_ENCLOSURETYPE_C, ART_PUBLICATIONDATE_D, ART_CREATEDATE_D, ART_STARREDCOUNT_N)" +
                "  values (:id, :feedId, :url, :baseUri, :guid, :title, :creator, :description, :commentUrl, " + DialectUtil.getNullParameter(":commentCount", article.getCommentCount())+ ", :enclosureUrl, " + DialectUtil.getNullParameter(":enclosureLength", article.getEnclosureLength())+ ", :enclosureType, :publicationDate, :createDate, :starredCount)")
                .setParameter("id", article.getId())
                .setParameter("feedId", article.getFeedId())
                .setParameter("url", article.getUrl())
                .setParameter("baseUri", article.getBaseUri())
                .setParameter("guid", article.getGuid())
                .setParameter("title", article.getTitle())
                .setParameter("creator", article.getCreator())
                .setParameter("description", article.getDescription())
                .setParameter("commentUrl", article.getCommentUrl())
                .setParameter("enclosureUrl", article.getEnclosureUrl())
                .setParameter("enclosureType", article.getEnclosureType())
                .setParameter("publicationDate", article.getPublicationDate())
                .setParameter("createDate", article.getCreateDate())
                .setParameter("starredCount", article.getStarredCount());
        if (article.getCommentCount() != null) {
            q.setParameter("commentCount", article.getCommentCount());
        }
        if (article.getEnclosureLength() != null) {
            q.setParameter("enclosureLength", article.getEnclosureLength());
        }
        q.executeUpdate();

        return article.getId();
    }

    /**
     * Updates a article.
     *
     * @param article Article to update
     * @return Updated article
     */
    public Article update(Article article) {
        // Get the article
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createNativeQuery("update T_ARTICLE set" +
                "  ART_URL_C = :url," +
                "  ART_TITLE_C = :title," +
                "  ART_CREATOR_C = :creator," +
                "  ART_DESCRIPTION_C = :description," +
                "  ART_COMMENTURL_C = :commentUrl," +
                "  ART_COMMENTCOUNT_N = " + DialectUtil.getNullParameter(":commentCount", article.getCommentCount())+ "," +
                "  ART_ENCLOSUREURL_C = :enclosureUrl," +
                "  ART_ENCLOSURELENGTH_N = " + DialectUtil.getNullParameter(":enclosureLength", article.getEnclosureLength())+ "," +
                "  ART_ENCLOSURETYPE_C = :enclosureType" +
                "  ART_STARREDCOUNT_N = :starredCount" +
                "  where ART_ID_C = :id and ART_DELETEDATE_D is null")
                .setParameter("url", article.getUrl())
                .setParameter("title", article.getTitle())
                .setParameter("creator", article.getCreator())
                .setParameter("description", article.getDescription())
                .setParameter("commentUrl", article.getCommentUrl())
                .setParameter("enclosureUrl", article.getEnclosureUrl())
                .setParameter("enclosureType", article.getEnclosureType())
                .setParameter("starredCount", article.getStarredCount())
                .setParameter("id", article.getId());
        if (article.getCommentCount() != null) {
            q.setParameter("commentCount", article.getCommentCount());
        }
        if (article.getEnclosureLength() != null) {
            q.setParameter("enclosureLength", article.getEnclosureLength());
        }
        q.executeUpdate();

        return article;
    }

    /**
     * Returns the list of all articles.
     * 
     * @return List of articles
     */
    @SuppressWarnings("unchecked")
    public List<Article> findAll() {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select a from Article a where a.deleteDate is null order by a.id");
        return q.getResultList();
    }
    
    /**
     * Deletes a article.
     * 
     * @param id Article ID
     */
    public void delete(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Date deleteDate = new Date();
        em.createNativeQuery("update T_ARTICLE set ART_DELETEDATE_D = :deleteDate where ART_ID_C = :id and ART_DELETEDATE_D is null")
                .setParameter("deleteDate", deleteDate)
                .setParameter("id", id)
                .executeUpdate();
        em.createNativeQuery("update T_USER_ARTICLE set USA_DELETEDATE_D = :deleteDate where USA_IDARTICLE_C = :articleId and USA_DELETEDATE_D is null")
                .setParameter("deleteDate", deleteDate)
                .setParameter("articleId", id)
                .executeUpdate();
    }

    // /**
    //  * Increments the starred count for an article by its ID.
    //  * 
    //  * @param id The ID of the article.
    //  */
    public void incrementStarredCount(String id){
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.createNativeQuery("update T_ARTICLE set ART_STARREDCOUNT_N = ART_STARREDCOUNT_N + 1 where ART_ID_C = :id")
                .setParameter("id", id)
                .executeUpdate();
    }

    public void decrementStarredCount(String id){
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.createNativeQuery("update T_ARTICLE set ART_STARREDCOUNT_N = ART_STARREDCOUNT_N - 1 where ART_ID_C = :id")
                .setParameter("id", id)
                .executeUpdate();
    }

    /**
     * Find articles by specified criteria, filtering only by article ID and feed ID.
     * 
     * @param criteria Search criteria
     * @return List of articles
     */
    @SuppressWarnings("unchecked")
    public List<ArticleDto> findByCriteria(ArticleCriteria criteria) {
        // printAllArticles();
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        StringBuilder sb = new StringBuilder("select a.ART_ID_C, a.ART_URL_C, a.ART_GUID_C, a.ART_TITLE_C, a.ART_CREATOR_C, a.ART_DESCRIPTION_C, a.ART_COMMENTURL_C, a.ART_COMMENTCOUNT_N, a.ART_ENCLOSUREURL_C, a.ART_ENCLOSURELENGTH_N, a.ART_ENCLOSURETYPE_C, a.ART_PUBLICATIONDATE_D, a.ART_CREATEDATE_D, a.ART_IDFEED_C ")
                .append("  from T_ARTICLE a ")
                .append("  where a.ART_DELETEDATE_D is null ");
        
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        
        // Add ID criteria if specified
        if (criteria.getId() != null) {
            sb.append("  and a.ART_ID_C = :id ");
            parameterMap.put("id", criteria.getId());
        }
        
        // Add feed ID criteria if specified
        if (criteria.getFeedId() != null) {
            sb.append("  and a.ART_IDFEED_C = :feedId ");
            parameterMap.put("feedId", criteria.getFeedId());
        }
        
        // Order by creation date
        sb.append("  order by a.ART_CREATEDATE_D asc");
        
        // Create the query
        Query q = em.createNativeQuery(sb.toString());
        
        // Bind parameters
        for (Map.Entry<String, Object> entry : parameterMap.entrySet()) {
            q.setParameter(entry.getKey(), entry.getValue());
        }
        
        // Execute the query and map results
        List<Object[]> resultList = q.getResultList();
        List<ArticleDto> articleDtoList = new ArrayList<ArticleDto>();
        
        for (Object[] result : resultList) {
            int i = 0;
            ArticleDto articleDto = new ArticleDto();
            articleDto.setId((String) result[i++]);
            articleDto.setUrl((String) result[i++]);
            articleDto.setGuid((String) result[i++]);
            articleDto.setTitle((String) result[i++]);
            articleDto.setCreator((String) result[i++]);
            articleDto.setDescription((String) result[i++]);
            articleDto.setCommentUrl((String) result[i++]);
            articleDto.setCommentCount((Integer) result[i++]);
            articleDto.setEnclosureUrl((String) result[i++]);
            articleDto.setEnclosureType((String) result[i++]);
            articleDto.setEnclosureCount((Integer) result[i++]);
            articleDto.setPublicationDate((Date) result[i++]);
            articleDto.setCreateDate((Date) result[i++]);
            articleDto.setFeedId((String) result[i++]);
            articleDtoList.add(articleDto);
        }
        
        return articleDtoList;
    }
    /**
     * Prints all articles in the database to the console.
     */
    public void printAllArticles() {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        // Query to get all articles (including deleted ones)
        String sql = "select a.ART_ID_C, a.ART_IDFEED_C, a.ART_URL_C, a.ART_GUID_C, a.ART_TITLE_C, " +
                    "a.ART_CREATOR_C, a.ART_DESCRIPTION_C, a.ART_COMMENTURL_C, a.ART_COMMENTCOUNT_N, " +
                    "a.ART_ENCLOSUREURL_C, a.ART_ENCLOSURELENGTH_N, a.ART_ENCLOSURETYPE_C, " +
                    "a.ART_PUBLICATIONDATE_D, a.ART_CREATEDATE_D, a.ART_DELETEDATE_D, a.ART_STARREDCOUNT_N " +
                    "from T_ARTICLE a";
                    
        Query q = em.createNativeQuery(sql);
        
        @SuppressWarnings("unchecked")
        List<Object[]> resultList = q.getResultList();
        
        System.out.println("===== ARTICLE TABLE CONTENTS =====");
        System.out.println("Total records: " + resultList.size());
        
        for (Object[] result : resultList) {
            System.out.println("----------------------------------");
            System.out.println("ID: " + result[0]);
            System.out.println("Feed ID: " + result[1]);
            System.out.println("URL: " + result[2]);
            System.out.println("GUID: " + result[3]);
            System.out.println("Title: " + result[4]);
            System.out.println("Creator: " + result[5]);
            System.out.println("Description: " + (result[6] != null ? result[6].toString().substring(0, Math.min(50, result[6].toString().length())) + "..." : "null"));
            System.out.println("Comment URL: " + result[7]);
            System.out.println("Comment Count: " + result[8]);
            System.out.println("Enclosure URL: " + result[9]);
            System.out.println("Enclosure Length: " + result[10]);
            System.out.println("Enclosure Type: " + result[11]);
            System.out.println("Publication Date: " + result[12]);
            System.out.println("Create Date: " + result[13]);
            System.out.println("Delete Date: " + result[14]);
            System.out.println("Starred Count" + result[15]);
        }
        System.out.println("=================================");
    }

    @SuppressWarnings("unchecked")
    public List<ArticleDto> getTrending(){
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        String sql="select a.ART_ID_C, a.ART_IDFEED_C, a.ART_URL_C, a.ART_GUID_C, a.ART_TITLE_C, "+
                        "a.ART_CREATOR_C, a.ART_DESCRIPTION_C, a.ART_COMMENTURL_C, a.ART_COMMENTCOUNT_N, " + 
                        "a.ART_ENCLOSUREURL_C, a.ART_ENCLOSURELENGTH_N, a.ART_ENCLOSURETYPE_C, " + 
                        "a.ART_PUBLICATIONDATE_D, a.ART_CREATEDATE_D, a.ART_STARREDCOUNT_N  FROM T_ARTICLE a WHERE a.ART_STARREDCOUNT_N > 0 ORDER BY a.ART_STARREDCOUNT_N DESC";
        // String sql = "select a.ART_ID_C, a.ART_IDFEED_C, a.ART_URL_C, a.ART_GUID_C, a.ART_TITLE_C, " +
        // "a.ART_CREATOR_C, a.ART_DESCRIPTION_C, a.ART_COMMENTURL_C, a.ART_COMMENTCOUNT_N, " +
        // "a.ART_ENCLOSUREURL_C, a.ART_ENCLOSURELENGTH_N, a.ART_ENCLOSURETYPE_C, " +
        // "a.ART_PUBLICATIONDATE_D, a.ART_CREATEDATE_D, a.ART_DELETEDATE_D " +
        // "from T_ARTICLE a";

        Query q = em.createNativeQuery(sql);
        q.setMaxResults(5);

        // return q.getResultList();
        List<Object[]> resultList = q.getResultList();
        List<ArticleDto> articleList = new ArrayList<ArticleDto>();
        
        for (Object[] result : resultList) {
            int i = 0;
            ArticleDto article = new ArticleDto();
            article.setId((String) result[i++]);
            article.setFeedId((String) result[i++]);
            article.setUrl((String) result[i++]);
            article.setGuid((String) result[i++]);
            article.setTitle((String) result[i++]);
            article.setCreator((String) result[i++]);
            article.setDescription((String) result[i++]);
            article.setCommentUrl((String) result[i++]);
            article.setCommentCount((Integer) result[i++]);
            article.setEnclosureUrl((String) result[i++]);
            article.setEnclosureCount((Integer) result[i++]);
            article.setEnclosureType((String) result[i++]);
            article.setPublicationDate((Date) result[i++]);
            article.setCreateDate((Date) result[i++]);
            article.setStarredCount((Integer) result[i++]);
            articleList.add(article);
        }
        
        return articleList;
    }
}
