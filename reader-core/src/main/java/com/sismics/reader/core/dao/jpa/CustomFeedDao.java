package com.sismics.reader.core.dao.jpa;

import com.sismics.reader.core.dao.jpa.criteria.CustomFeedCriteria;
import com.sismics.reader.core.dao.jpa.dto.CustomFeedDto;
import com.sismics.reader.core.dao.jpa.mapper.CustomFeedMapper;
import com.sismics.reader.core.model.jpa.CustomFeed;
import com.sismics.reader.core.util.jpa.SortCriteria;
import com.sismics.util.context.ThreadLocalContext;
import com.sismics.util.jpa.BaseDao;
import com.sismics.util.jpa.QueryParam;
import com.sismics.util.jpa.filter.FilterCriteria;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import java.util.*;

/**
 * CustomFeed DAO.
 * 
 * @author adityamishra
 */
public class CustomFeedDao extends BaseDao<CustomFeedDto, CustomFeedCriteria> {

    @Override
    protected QueryParam getQueryParam(CustomFeedCriteria criteria, FilterCriteria filterCriteria) {
        List<String> criteriaList = new ArrayList<String>();
        Map<String, Object> parameterMap = new HashMap<String, Object>();

        StringBuilder sb = new StringBuilder("select cf.CFD_ID_C as id, cf.CFD_CUSTOMUSERID_C as customUserId, ");
        sb.append("cf.CFD_TITLE_C as title, cf.CFD_DESCRIPTION_C as description ");
        sb.append(" from T_CUSTOMFEED cf ");

        // Adds search criteria
        if (criteria.getCustomUserId() != null) {
            criteriaList.add("cf.CFD_CUSTOMUSERID_C = :customUserId");
            parameterMap.put("customUserId", criteria.getCustomUserId());
        }
        
        if (criteria.getId() != null) {
            criteriaList.add("cf.CFD_ID_C = :id");
            parameterMap.put("id", criteria.getId());
        }
        
        if (criteria.getTitle() != null) {
            criteriaList.add("cf.CFD_TITLE_C like :title");
            parameterMap.put("title", "%" + criteria.getTitle() + "%");
        }

        SortCriteria sortCriteria = new SortCriteria("  order by cf.CFD_TITLE_C asc");

        return new QueryParam(sb.toString(), criteriaList, parameterMap, sortCriteria, filterCriteria, new CustomFeedMapper());
    }

    /**
     * Creates a new custom feed.
     * 
     * @param customFeed CustomFeed to create
     * @return ID
     */
    public String create(CustomFeed customFeed) {
        customFeed.setId(UUID.randomUUID().toString());
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.persist(customFeed);
        return customFeed.getId();
    }

    /**
     * Deletes a custom feed.
     * 
     * @param id Custom Feed ID
     */
    public void delete(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createNativeQuery("select * from T_CUSTOMFEED where CFD_ID_C = :id", CustomFeed.class)
                .setParameter("id", id);
        CustomFeed customFeedFromDb = (CustomFeed) q.getSingleResult();
        em.remove(customFeedFromDb);

    }
    
    /**
     * Get a custom feed by its ID.
     * 
     * @param id Custom Feed ID
     * @return CustomFeed
     */
    public CustomFeed getById(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        // Get the custom feed
        Query q = em.createNativeQuery("select * from T_CUSTOMFEED where CFD_ID_C = :id", CustomFeed.class)
                .setParameter("id", id);
        try {
            return (CustomFeed) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Get all custom feeds by user ID.
     * 
     * @param customUserId Custom User ID
     * @return List of CustomFeed
     */
    public List<CustomFeed> getByUserId(String customUserId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        // Get the custom feeds
        Query q = em.createNativeQuery("select * from T_CUSTOMFEED where CFD_CUSTOMUSERID_C = :customUserId order by CFD_TITLE_C asc", CustomFeed.class)
                .setParameter("customUserId", customUserId);
        try {
            @SuppressWarnings("unchecked")
            List<CustomFeed> customFeeds = q.getResultList();
            return customFeeds;
        } catch (NoResultException e) {
            return new ArrayList<>();
        }
    }

    /**
     * Updates a custom feed.
     * 
     * @param customFeed CustomFeed to update
     * @return Updated CustomFeed
     */
    public CustomFeed update(CustomFeed customFeed) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        // Get the custom feed
        Query q = em.createNativeQuery("select * from T_CUSTOMFEED where CFD_ID_C = :id", CustomFeed.class)
                .setParameter("id", customFeed.getId());
        try {
            CustomFeed customFeedFromDb = (CustomFeed) q.getSingleResult();

            // Update the custom feed
            customFeedFromDb.setCustomUserId(customFeed.getCustomUserId());
            customFeedFromDb.setTitle(customFeed.getTitle());
            customFeedFromDb.setDescription(customFeed.getDescription());
            
            return customFeedFromDb;
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Finds custom feeds by criteria.
     * 
     * @param criteria Search criteria
     * @return List of CustomFeedDto
     */
    @SuppressWarnings("unchecked")
    public List<CustomFeedDto> findByCriteria(CustomFeedCriteria criteria) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        StringBuilder sb = new StringBuilder(
                "select cfd.CFD_ID_C, cfd.CFD_CUSTOMUSERID_C, cfd.CFD_TITLE_C, cfd.CFD_DESCRIPTION_C ");
        sb.append(" from T_CUSTOMFEED cfd ");
        
        // Criteria
        List<String> criteriaList = new ArrayList<String>();
        HashMap<String, Object> parameterMap = new HashMap<String, Object>();
        
        // if custom user id criterion is mentioned
        if (criteria.getCustomUserId() != null) {
            criteriaList.add("cfd.CFD_CUSTOMUSERID_C = :customUserId");
            parameterMap.put("customUserId", criteria.getCustomUserId());
        }
        
        // if id criterion is mentioned
        if (criteria.getId() != null) {
            criteriaList.add("cfd.CFD_ID_C = :id");
            parameterMap.put("id", criteria.getId());
        }
        
        // if title criterion is mentioned
        if (criteria.getTitle() != null) {
            criteriaList.add("cfd.CFD_TITLE_C like :title");
            parameterMap.put("title", "%" + criteria.getTitle() + "%");
        }
        
        if (!criteriaList.isEmpty()) {
            sb.append(" where ");
            sb.append(String.join(" and ", criteriaList));
        }
        
        // Order
        sb.append(" order by cfd.CFD_TITLE_C asc");
        
        // Create the query
        Query q = em.createNativeQuery(sb.toString());
        
        // Add parameters
        for (Map.Entry<String, Object> entry : parameterMap.entrySet()) {
            q.setParameter(entry.getKey(), entry.getValue());
        }
        
        // Transform to DTO
        List<Object[]> resultList = q.getResultList();
        List<CustomFeedDto> customFeedDtoList = new ArrayList<CustomFeedDto>();
        for (Object[] result : resultList) {
            int i = 0;
            CustomFeedDto customFeedDto = new CustomFeedDto();
            customFeedDto.setId((String) result[i++]);
            customFeedDto.setCustomUserId((String) result[i++]);
            customFeedDto.setTitle((String) result[i++]);
            customFeedDto.setDescription((String) result[i]);
            customFeedDtoList.add(customFeedDto);
        }
        
        return customFeedDtoList;
    }
}