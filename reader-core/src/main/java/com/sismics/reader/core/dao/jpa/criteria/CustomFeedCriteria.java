package com.sismics.reader.core.dao.jpa.criteria;

/**
 * CustomFeed criteria.
 *
 * @author jtremeaux
 */
public class CustomFeedCriteria {
    /**
     * Custom user ID.
     */
    private String customUserId;
    
    /**
     * Feed ID.
     */
    private String id;
    
    /**
     * Feed title.
     */
    private String title;
    
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
     * @return this
     */
    public CustomFeedCriteria setCustomUserId(String customUserId) {
        this.customUserId = customUserId;
        return this;
    }
    
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
     * @return this
     */
    public CustomFeedCriteria setId(String id) {
        this.id = id;
        return this;
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
     * @return this
     */
    public CustomFeedCriteria setTitle(String title) {
        this.title = title;
        return this;
    }
}