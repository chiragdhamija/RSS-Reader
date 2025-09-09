package com.sismics.reader.core.model.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Bug report entity.
 *
 * @author adityamishra
 */
@Entity
@Table(name = "T_BUG_REPORT")
public class BugReport {
    /**
     * Bug report ID.
     */
    @Id
    @Column(name = "BUR_ID_C", length = 36)
    private String id;
    
    /**
     * Username.
     */
    @Column(name = "BUR_NAMEUSER_C", nullable = false, length = 50)
    private String username;
    
    @Column(name = "BUR_IDUSER_C", nullable = false, length = 36)
    private String userid;
     
    /**
     * Bug report description.
     */
    @Column(name = "BUR_DESCRIPTION_C", nullable = false, length = 4000)
    private String description;
    
    /**
     * Creation date.
     */
    @Column(name = "BUR_CREATEDATE_D", nullable = false)
    private Date creationDate;

    /**
     * Resolved.
     */
    @Column(name = "BUR_RESOLVED_B", nullable = false)
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