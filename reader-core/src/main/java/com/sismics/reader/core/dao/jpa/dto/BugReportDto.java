package com.sismics.reader.core.dao.jpa.dto;

import java.util.Date;

/**
 * Bug report DTO.
 *
 * @author adityamishra
 */
public class BugReportDto {
    /**
     * Bug report ID.
     */
    private String id;
    
    /**
     * Bug description.
     */
    private String description;
    
    /**
     * Creation date.
     */
    private Date creationDate;
    
    /**
     * User who reported the bug.
     */
    private String username;

    /**
     * User ID.
     */
    private String userid;
    
    /**
     * Resolved.
     */
    private boolean resolved;
    /**
     * Getter of id.
     *
     * @return id
     */
    public String getId() {
        return id;
    }
    
    /**
     * Setter of id.
     *
     * @param id id
     */
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * Getter of description.
     *
     * @return description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Setter of description.
     *
     * @param description description
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Getter of creationDate.
     *
     * @return creationDate
     */
    public Date getCreationDate() {
        return creationDate;
    }
    
    /**
     * Setter of creationDate.
     *
     * @param creationDate creationDate
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
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
     */
    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserid() {
        return userid;
    }
    public void setUserid(String userid) {
        this.userid = userid;
    }
    /**
     * Getter of resolved.
     *
     * @return resolved
     */
    public boolean isResolved() {
        return resolved;
    }
    /**
     * Setter of resolved.
     *
     * @param resolved resolved
     */
    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }
}