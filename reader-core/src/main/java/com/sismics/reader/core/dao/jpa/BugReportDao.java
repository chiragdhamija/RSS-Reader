package com.sismics.reader.core.dao.jpa;

import com.sismics.reader.core.dao.jpa.criteria.BugReportCriteria;
import com.sismics.reader.core.dao.jpa.dto.BugReportDto;
import com.sismics.reader.core.model.jpa.BugReport;
import com.sismics.util.context.ThreadLocalContext;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.*;

/**
 * Bug report DAO.
 * 
 * @author adityamishra
 */
public class BugReportDao {
    /**
     * Creates a new bug report.
     * 
     * @param bugReport Bug report to create
     * @return Created bug report ID
     */
    public String create(BugReport bugReport) {
        // Create the UUID
        bugReport.setId(UUID.randomUUID().toString());
        // Create the bug report
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.persist(bugReport);
        return bugReport.getId();
    }

    /**
     * Deletes a bug report.
     * 
     * @param id Bug report ID
     */
    public void delete(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        BugReport bugReport = getById(id);
        if (bugReport != null) {
            em.remove(bugReport);
        }
    }

    /**
     * Resolves a bug report.
     * 
     * @param id Bug report ID
     */
    public void resolve(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        BugReport bugReport = getById(id);
        if (bugReport != null) {
            bugReport.setResolved(true);
            em.merge(bugReport);
        }
    }

    /**
     * Returns a bug report by ID.
     * 
     * @param id Bug report ID
     * @return Bug report
     */
    public BugReport getById(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            return em.find(BugReport.class, id);
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Finds all bug reports.
     * 
     * @return List of bug reports
     */
    public List<BugReportDto> findAll() {
        BugReportCriteria criteria = new BugReportCriteria();
        return findByCriteria(criteria);
    }

    /**
     * Finds bug reports by criteria.
     * 
     * @param criteria Search criteria
     * @return List of bug reports
     */
    @SuppressWarnings("unchecked")
    public List<BugReportDto> findByCriteria(BugReportCriteria criteria) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        StringBuilder sb = new StringBuilder(
            "select br.BUR_ID_C, br.BUR_DESCRIPTION_C, br.BUR_CREATEDATE_D, u.USE_USERNAME_C, br.BUR_RESOLVED_B ");
            sb.append(" from T_BUG_REPORT br ");
        sb.append(" join T_USER u on u.USE_ID_C = br.BUR_IDUSER_C ");

        // Criteria
        List<String> criteriaList = new ArrayList<String>();
        HashMap<String, Object> parameterMap = new HashMap<String, Object>();

        // if user id criterion is mentioned
        if (criteria.getUserid() != null) {
            criteriaList.add("br.BUR_IDUSER_C = :userid");
            parameterMap.put("userid", criteria.getUserid());
        }

        // if resolved criterion is mentioned
        if (criteria.isResolved() != null) {
            criteriaList.add("br.BUR_RESOLVED_B = :resolved");
            parameterMap.put("resolved", criteria.isResolved());
        }

        // if username criterion is mentoined
        if (criteria.getUsername() != null) {
            criteriaList.add("u.USE_USERNAME_C = :username");
            parameterMap.put("username", criteria.getUsername());
        }

        if (!criteriaList.isEmpty()) {
            sb.append(" where ");
            sb.append(String.join(" and ", criteriaList));
        }

        // Order
        sb.append(" order by br.BUR_CREATEDATE_D desc");

        // Create the query
        Query q = em.createNativeQuery(sb.toString());

        // Add parameters
        for (Map.Entry<String, Object> entry : parameterMap.entrySet()) {
            q.setParameter(entry.getKey(), entry.getValue());
        }

        // Pagination
        if (criteria.getLimit() != null) {
            q.setMaxResults(criteria.getLimit());
        }
        if (criteria.getOffset() != null) {
            q.setFirstResult(criteria.getOffset());
        }

        // Transform to DTO
        List<Object[]> resultList = q.getResultList();
        List<BugReportDto> bugReportDtoList = new ArrayList<BugReportDto>();
        for (Object[] result : resultList) {
            int i = 0;
            BugReportDto bugReportDto = new BugReportDto();
            bugReportDto.setId((String) result[i++]);
            bugReportDto.setDescription((String) result[i++]);
            bugReportDto.setCreationDate((Date) result[i++]);
            bugReportDto.setUsername((String) result[i++]);
            bugReportDto.setResolved((Boolean) result[i]);
            bugReportDtoList.add(bugReportDto);
        }

        return bugReportDtoList;
    }

    /**
     * Counts bug reports by criteria.
     * 
     * @param criteria Search criteria
     * @return Number of bug reports
     */
    public int countByCriteria(BugReportCriteria criteria) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        StringBuilder sb = new StringBuilder("select count(br.BUR_ID_C) ");
        sb.append(" from T_BUG_REPORT br ");

        // Add join if we're filtering by username
        if (criteria.getUsername() != null) {
            sb.append(" join T_USER u on u.USE_ID_C = br.BUR_IDUSER_C ");
        }

        // Criteria
        List<String> criteriaList = new ArrayList<String>();
        HashMap<String, Object> parameterMap = new HashMap<String, Object>();

        // Add criteria if any
        if (criteria.getUsername() != null) {
            criteriaList.add("u.USE_USERNAME_C = :username");
            parameterMap.put("username", criteria.getUsername());
        }

        // Add search criteria
        if (!criteriaList.isEmpty()) {
            sb.append(" where ");
            sb.append(String.join(" and ", criteriaList));
        }

        // Create the query
        Query q = em.createNativeQuery(sb.toString());

        // Add parameters
        for (Map.Entry<String, Object> entry : parameterMap.entrySet()) {
            q.setParameter(entry.getKey(), entry.getValue());
        }

        return ((Number) q.getSingleResult()).intValue();
    }
}