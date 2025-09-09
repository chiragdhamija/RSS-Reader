package com.sismics.reader.core.dao.jpa.criteria;

/**
 * Custom feed subscription criteria.
 *
 * @author jtremeaux 
 */
public class CustomFeedSubscriptionCriteria {
    /**
     * Custom feed subscription id.
     */
    private String id;
    
    /**
     * User ID.
     */
    private String userId;
    
    /**
     * Feed ID.
     */
    private String feedId;
    
    /**
     * Feed URL.
     */
    private String feedUrl;

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
    public CustomFeedSubscriptionCriteria setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Getter of userId.
     *
     * @return userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Setter of userId.
     *
     * @param userId userId
     */
    public CustomFeedSubscriptionCriteria setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    /**
     * Getter of feedId.
     *
     * @return feedId
     */
    public String getFeedId() {
        return feedId;
    }

    /**
     * Setter of feedId.
     *
     * @param feedId feedId
     */
    public CustomFeedSubscriptionCriteria setFeedId(String feedId) {
        this.feedId = feedId;
        return this;
    }

    /**
     * Getter of feedUrl.
     *
     * @return feedUrl
     */
    public String getFeedUrl() {
        return feedUrl;
    }

    /**
     * Setter of feedUrl.
     *
     * @param feedUrl feedUrl
     */
    public CustomFeedSubscriptionCriteria setFeedUrl(String feedUrl) {
        this.feedUrl = feedUrl;
        return this;
    }
}