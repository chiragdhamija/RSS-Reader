package com.sismics.reader.core.dao.jpa.criteria;

/**
 * Bug report criteria.
 *
 * @author adityamishra
 */
public class BugReportCriteria {
    /**
     * User ID.
     */
    private String userid;

    /**
     * Username.
     */
    private String username;
    
    /**
     * Limit.
     */
    private Integer limit;
    
    /**
     * Offset.
     */
    private Integer offset;

    /**
     * Resolved.
     */
    private Boolean resolved;

    /**
     * Getter of userId.
     *
     * @return userId
     */
    public String getUserid() {
        return userid;
    }

    /**
     * Setter of userid.
     *
     * @param userid userid
     * @return This criteria
     */
    public BugReportCriteria setUserid(String userid) {
        this.userid = userid;
        return this;
    }

    /**
     * Getter of username.
     *
     * @return username
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * Setter of username.
     *
     * @param username username
     * @return This criteria
     */
    public BugReportCriteria setUsername(String username) {
        this.username = username;
        return this;
    }
    
    /**
     * Getter of limit.
     *
     * @return limit
     */
    public Integer getLimit() {
        return limit;
    }
    
    /**
     * Setter of limit.
     *
     * @param limit limit
     * @return This criteria
     */
    public BugReportCriteria setLimit(Integer limit) {
        this.limit = limit;
        return this;
    }
    
    /**
     * Getter of offset.
     *
     * @return offset
     */
    public Integer getOffset() {
        return offset;
    }
    
    /**
     * Setter of offset.
     *
     * @param offset offset
     * @return This criteria
     */
    public BugReportCriteria setOffset(Integer offset) {
        this.offset = offset;
        return this;
    }
    /**
     * Getter of resolved.
     * @return resolved
     */
    public Boolean isResolved() {
        return resolved;
    }

    /**
     * Setter of resolved.
     * @param resolved resolved
     * @return This criteria
     */
    public BugReportCriteria setResolved(Boolean resolved) {
        this.resolved = resolved;
        return this;
    }
}