package com.sismics.reader.core.dao.jpa;

import com.sismics.reader.core.dao.jpa.criteria.CustomArticleCriteria;
import com.sismics.reader.core.dao.jpa.dto.CustomArticleDto;
import com.sismics.reader.core.dao.jpa.mapper.CustomArticleMapper;
import com.sismics.reader.core.model.jpa.CustomArticle;
import com.sismics.reader.core.util.jpa.SortCriteria;
import com.sismics.util.context.ThreadLocalContext;
import com.sismics.util.jpa.BaseDao;
import com.sismics.util.jpa.DialectUtil;
import com.sismics.util.jpa.QueryParam;
import com.sismics.util.jpa.filter.FilterCriteria;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * CustomArticle DAO.
 * 
 * @author jtremeaux
 */
public class CustomArticleDao extends BaseDao<CustomArticleDto, CustomArticleCriteria> {

    @Override
    protected QueryParam getQueryParam(CustomArticleCriteria criteria, FilterCriteria filterCriteria) {
        List<String> criteriaList = new ArrayList<String>();
        Map<String, Object> parameterMap = new HashMap<String, Object>();

        StringBuilder sb = new StringBuilder("select a.CFA_ORIG_ID, a.CFA_ID, a.CFA_URL_C, a.CFA_GUID_C, a.CFA_TITLE_C, a.CFA_CREATOR_C, a.CFA_DESCRIPTION_C, a.CFA_COMMENTURL_C, a.CFA_COMMENTCOUNT_N, a.CFA_ENCLOSUREURL_C, a.CFA_ENCLOSURELENGTH_N, a.CFA_ENCLOSURETYPE_C, a.CFA_PUBLICATIONDATE_D, a.CFA_CREATEDATE_D, a.CFA_IDCUSTOMFEED_C ")
                .append("  from T_CUSTOMARTICLE a ");

        // Adds search criteria
        if (criteria.getOrigId() != null) {
            criteriaList.add("a.CFA_ORIG_ID = :origId");
            parameterMap.put("origId", criteria.getOrigId());
        }
        if (criteria.getId() != null) {
            criteriaList.add("a.CFA_ID = :id");
            parameterMap.put("id", criteria.getId());
        }
        if (criteria.getGuidIn() != null) {
            criteriaList.add("a.CFA_GUID_C in :guidIn");
            parameterMap.put("guidIn", criteria.getGuidIn());
        }
        if (criteria.getTitle() != null) {
            criteriaList.add("a.CFA_TITLE_C = :title");
            parameterMap.put("title", criteria.getTitle());
        }
        if (criteria.getUrl() != null) {
            criteriaList.add("a.CFA_URL_C = :url");
            parameterMap.put("url", criteria.getUrl());
        }
        if (criteria.getPublicationDateMin() != null) {
            criteriaList.add("a.CFA_PUBLICATIONDATE_D > :publicationDateMax");
            parameterMap.put("publicationDateMax", criteria.getPublicationDateMin());
        }
        if (criteria.getCustomFeedId() != null) {
            criteriaList.add("a.CFA_IDCUSTOMFEED_C = :customFeedId");
            parameterMap.put("customFeedId", criteria.getCustomFeedId());
        }

        SortCriteria sortCriteria = new SortCriteria("  order by a.CFA_CREATEDATE_D asc");

        return new QueryParam(sb.toString(), criteriaList, parameterMap, sortCriteria, filterCriteria, new CustomArticleMapper());
    }

    /**
     * Creates a new custom article.
     * 
     * @param customArticle CustomArticle to create
     * @return New ID
     */
    public String create(CustomArticle customArticle) {
        customArticle.setOrigId(UUID.randomUUID().toString());
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.persist(customArticle);
        return customArticle.getOrigId();
    }

    /**
     * Deletes a custom feed.
     *
     * @param id Custom Feed ID
     */
    public void delete(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.createNativeQuery("DELETE FROM T_CUSTOMARTICLE WHERE CFA_ORIG_ID = :id")
            .setParameter("id", id)
            .executeUpdate();
    }
    /**
     * Get a custom articles for a given Feed.
     * @param customFeedId
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<CustomArticle> listByCustomFeedId(String customFeedId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        // Query q = em.createQuery("select c from CustomArticle c where c.customFeedId = :customFeedId")
        //             .setParameter("customFeedId", customFeedId);
        Query q = em.createQuery("select c from CustomArticle c where c.customFeedId = :customFeedId")
                .setParameter("customFeedId", customFeedId);
        return q.getResultList();
    }

    /**
     * Finds custom articles by criteria.
     * 
     * @param criteria Search criteria
     * @return List of custom articles
     */
    @SuppressWarnings("unchecked")
    public List<CustomArticleDto> findByCriteria(CustomArticleCriteria criteria) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        StringBuilder sb = new StringBuilder(
            "select a.CFA_ORIG_ID, a.CFA_ID, a.CFA_URL_C, a.CFA_GUID_C, a.CFA_TITLE_C, a.CFA_CREATOR_C, a.CFA_DESCRIPTION_C, " +
            "a.CFA_COMMENTURL_C, a.CFA_COMMENTCOUNT_N, a.CFA_ENCLOSUREURL_C, a.CFA_ENCLOSURELENGTH_N, a.CFA_ENCLOSURETYPE_C, " +
            "a.CFA_PUBLICATIONDATE_D, a.CFA_CREATEDATE_D, a.CFA_IDCUSTOMFEED_C ");
        sb.append(" from T_CUSTOMARTICLE a ");

        // Criteria
        List<String> criteriaList = new ArrayList<String>();
        HashMap<String, Object> parameterMap = new HashMap<String, Object>();

        // if id criterion is mentioned
        if (criteria.getId() != null) {
            criteriaList.add("a.CFA_ID = :id");
            parameterMap.put("id", criteria.getId());
        }

        // if orig id criterion is mentioned
        if (criteria.getOrigId() != null) {
            criteriaList.add("a.CFA_ORIG_ID = :origId");
            parameterMap.put("origId", criteria.getOrigId());
        }

        // if custom feed id criterion is mentioned
        if (criteria.getCustomFeedId() != null) {
            criteriaList.add("a.CFA_IDCUSTOMFEED_C = :customFeedId");
            parameterMap.put("customFeedId", criteria.getCustomFeedId());
        }

        if (!criteriaList.isEmpty()) {
            sb.append(" where ");
            sb.append(String.join(" and ", criteriaList));
        }

        // Order
        sb.append(" order by a.CFA_CREATEDATE_D desc");

        // Create the query
        Query q = em.createNativeQuery(sb.toString());

        // Add parameters
        for (Map.Entry<String, Object> entry : parameterMap.entrySet()) {
            q.setParameter(entry.getKey(), entry.getValue());
        }

        // Transform to DTO
        List<Object[]> resultList = q.getResultList();
        List<CustomArticleDto> customArticleDtoList = new ArrayList<CustomArticleDto>();
        for (Object[] result : resultList) {
            int i = 0;
            CustomArticleDto customArticleDto = new CustomArticleDto();
            customArticleDto.setOrigId((String) result[i++]);
            customArticleDto.setId((String) result[i++]);
            customArticleDto.setUrl((String) result[i++]);
            customArticleDto.setGuid((String) result[i++]);
            customArticleDto.setTitle((String) result[i++]);
            customArticleDto.setCreator((String) result[i++]);
            customArticleDto.setDescription((String) result[i++]);
            customArticleDto.setCommentUrl((String) result[i++]);
            customArticleDto.setCommentCount((Integer) result[i++]);
            customArticleDto.setEnclosureUrl((String) result[i++]);
            customArticleDto.setEnclosureCount((Integer) result[i++]);
            customArticleDto.setEnclosureType((String) result[i++]);
            customArticleDto.setPublicationDate((Date) result[i++]);
            customArticleDto.setCreateDate((Date) result[i++]);
            customArticleDto.setCustomFeedId((String) result[i]);
            customArticleDtoList.add(customArticleDto);
        }

        return customArticleDtoList;
    }
}