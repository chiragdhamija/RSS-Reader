package com.sismics.reader.core.dao.jpa.dto;

/**
 * CustomFeed DTO.
 *
 * @author jtremeaux
 */
public class CustomFeedDto {
    /**
     * Custom Feed ID.
     */
    private String id;
    
    /**
     * Custom user ID.
     */
    private String customUserId;
    
    /**
     * Custom feed title.
     */
    private String title;
    
    /**
     * Custom feed description.
     */
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
}