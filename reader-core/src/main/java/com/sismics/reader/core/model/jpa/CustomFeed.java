package com.sismics.reader.core.model.jpa;

import com.google.common.base.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Custom Feed entity.
 * 
 * @author jtremeaux
 */
@Entity
@Table(name = "T_CUSTOMFEED")
public class CustomFeed {
    /**
     * Custom Feed ID.
     */
    @Id
    @Column(name = "CFD_ID_C", length = 50)
    private String id;
    
    /**
     * Custom user ID.
     */
    @Column(name = "CFD_CUSTOMUSERID_C", nullable = false, length = 36)
    private String customUserId;
    
    /**
     * Custom feed title.
     */
    @Column(name = "CFD_TITLE_C", nullable = false, length = 100)
    private String title;
    
    /**
     * Custom feed description.
     */
    @Column(name = "CFD_DESCRIPTION_C", nullable = false, length = 4000)
    private String description;

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
     * Getter of customUserId.
     *
     * @return customUserId
     */
    public String getCustomUserId() {
        return customUserId;
    }

    /**
     * Setter of customUserId.
     *
     * @param customUserId customUserId
     */
    public void setCustomUserId(String customUserId) {
        this.customUserId = customUserId;
    }
    
    /**
     * Getter of title.
     *
     * @return title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Setter of title.
     *
     * @param title title
     */
    public void setTitle(String title) {
        this.title = title;
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

    public CustomFeed() {
    }

    public CustomFeed(String id) {
        this.id = id;
    }
}