package com.sismics.reader.core.dao.jpa;

import com.google.common.collect.Lists;
import com.sismics.reader.core.dao.jpa.criteria.CustomFeedSubscriptionCriteria;
import com.sismics.reader.core.dao.jpa.dto.CustomFeedSubscriptionDto;
import com.sismics.reader.core.dao.jpa.mapper.CustomFeedSubscriptionMapper;
import com.sismics.reader.core.model.jpa.CustomFeedSubscription;
import com.sismics.util.context.ThreadLocalContext;
import com.sismics.util.jpa.BaseDao;
import com.sismics.util.jpa.QueryParam;
import com.sismics.util.jpa.filter.FilterCriteria;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.*;

/**
 * Custom feed subscription DAO.
 * 
 * @author jtremeaux
 */
public class CustomFeedSubscriptionDao extends BaseDao<CustomFeedSubscriptionDto, CustomFeedSubscriptionCriteria> {

    @Override
    protected QueryParam getQueryParam(CustomFeedSubscriptionCriteria criteria, FilterCriteria filterCriteria) {
        List<String> criteriaList = Lists.newArrayList();
        Map<String, Object> parameterMap = new HashMap<String, Object>();

        StringBuilder sb = new StringBuilder("select cfs.CFS_ID_C, cfs.CFS_TITLE_C, cfs.CFS_IDUSER_C, cfs.CFS_IDFEED_C, cf.CFD_TITLE_C ")
                .append("  from T_CUSTOM_FEED_SUBSCRIPTION cfs ")
                .append("  join T_CUSTOMFEED cf on(cf.CFD_ID_C = cfs.CFS_IDFEED_C) ");

        // Adds search criteria
        if (criteria.getId() != null) {
            criteriaList.add("cfs.CFS_ID_C = :id");
            parameterMap.put("id", criteria.getId());
        }
        if (criteria.getUserId() != null) {
            criteriaList.add("cfs.CFS_IDUSER_C = :userId");
            parameterMap.put("userId", criteria.getUserId());
        }
        if (criteria.getFeedId() != null) {
            criteriaList.add("cfs.CFS_IDFEED_C = :feedId");
            parameterMap.put("feedId", criteria.getFeedId());
        }

        return new QueryParam(sb.toString(), criteriaList, parameterMap, null, filterCriteria, new CustomFeedSubscriptionMapper());
    }

    /**
     * Creates a new custom feed subscription.
     * 
     * @param customFeedSubscription Custom feed subscription to create
     * @return New ID
     */
    public String create(CustomFeedSubscription customFeedSubscription) {
        // Create the UUID
        customFeedSubscription.setId(UUID.randomUUID().toString());
        
        // Create the custom feed subscription
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.persist(customFeedSubscription);
        
        return customFeedSubscription.getId();
    }
    
    /**
     * Updates a customFeedSubscription.
     * 
     * @param customFeedSubscription CustomFeedSubscription
     * @return Updated customFeedSubscription
     */
    public CustomFeedSubscription update(CustomFeedSubscription customFeedSubscription) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        // Get the subscription
        Query q = em.createQuery("select cfs from CustomFeedSubscription cfs where cfs.id = :id")
                .setParameter("id", customFeedSubscription.getId());
        CustomFeedSubscription customFeedSubscriptionFromDb = (CustomFeedSubscription) q.getSingleResult();

        // Update the subscription
        customFeedSubscriptionFromDb.setTitle(customFeedSubscription.getTitle());

        return customFeedSubscription;
    }
    
    /**
     * Deletes a subscription.
     * 
     * @param id Subscription ID
     */
    public void delete(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        // Delete the subscription directly
        em.createNativeQuery("delete from T_CUSTOM_FEED_SUBSCRIPTION where CFS_ID_C = :id")
                .setParameter("id", id)
                .executeUpdate();
    }
    
    /**
     * Returns a subscription.
     * 
     * @param id Subscription ID
     * @param userId User ID
     * @return Custom feed subscription
     */
    public CustomFeedSubscription getCustomFeedSubscription(String id, String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select cfs from CustomFeedSubscription cfs where cfs.id = :id and cfs.userId = :userId")
                .setParameter("id", id)
                .setParameter("userId", userId);
        try {
            return (CustomFeedSubscription) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Returns all subscriptions for a user.
     * 
     * @param userId User ID
     * @return List of custom feed subscriptions
     */
    @SuppressWarnings("unchecked")
    public List<CustomFeedSubscription> findByUser(String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select cfs from CustomFeedSubscription cfs where cfs.userId = :userId")
                .setParameter("userId", userId);
        return q.getResultList();
    }

    /**
     * Finds custom feed subscriptions by criteria.
     * 
     * @param criteria Search criteria
     * @return List of custom feed subscriptions
     */
    @SuppressWarnings("unchecked")
    public List<CustomFeedSubscriptionDto> findByCriteria(CustomFeedSubscriptionCriteria criteria) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        StringBuilder sb = new StringBuilder(
            "select cfs.CFS_ID_C, cfs.CFS_TITLE_C, cfs.CFS_IDUSER_C, cfs.CFS_IDFEED_C, cf.CFD_TITLE_C ");
        sb.append(" from T_CUSTOM_FEED_SUBSCRIPTION cfs ");
        sb.append(" join T_CUSTOMFEED cf on(cf.CFD_ID_C = cfs.CFS_IDFEED_C) ");

        // Criteria
        List<String> criteriaList = new ArrayList<String>();
        HashMap<String, Object> parameterMap = new HashMap<String, Object>();

        // if id criterion is mentioned
        if (criteria.getId() != null) {
            criteriaList.add("cfs.CFS_ID_C = :id");
            parameterMap.put("id", criteria.getId());
        }

        // if user id criterion is mentioned
        if (criteria.getUserId() != null) {
            criteriaList.add("cfs.CFS_IDUSER_C = :userId");
            parameterMap.put("userId", criteria.getUserId());
        }

        // if feed id criterion is mentioned
        if (criteria.getFeedId() != null) {
            criteriaList.add("cfs.CFS_IDFEED_C = :feedId");
            parameterMap.put("feedId", criteria.getFeedId());
        }

        if (!criteriaList.isEmpty()) {
            sb.append(" where ");
            sb.append(String.join(" and ", criteriaList));
        }

        // Order by title
        sb.append(" order by cfs.CFS_TITLE_C asc");

        // Create the query
        Query q = em.createNativeQuery(sb.toString());

        // Add parameters
        for (Map.Entry<String, Object> entry : parameterMap.entrySet()) {
            q.setParameter(entry.getKey(), entry.getValue());
        }

        // Transform to DTO
        List<Object[]> resultList = q.getResultList();
        List<CustomFeedSubscriptionDto> customFeedSubscriptionDtoList = new ArrayList<CustomFeedSubscriptionDto>();
        for (Object[] result : resultList) {
            int i = 0;
            CustomFeedSubscriptionDto customFeedSubscriptionDto = new CustomFeedSubscriptionDto();
            customFeedSubscriptionDto.setId((String) result[i++]);
            // customFeedSubscriptionDto.setTitle((String) result[i++]);
            customFeedSubscriptionDto.setUserId((String) result[i++]);
            customFeedSubscriptionDto.setFeedId((String) result[i++]);
            // customFeedSubscriptionDto.setFeedTitle((String) result[i]);
            customFeedSubscriptionDtoList.add(customFeedSubscriptionDto);
        }

        return customFeedSubscriptionDtoList;
    }
    
}