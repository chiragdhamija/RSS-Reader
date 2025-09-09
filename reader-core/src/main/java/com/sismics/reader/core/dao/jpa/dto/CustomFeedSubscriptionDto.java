package com.sismics.reader.core.dao.jpa.dto;

/**
 * Custom feed subscription DTO.
 *
 * @author jtremeaux 
 */
public class CustomFeedSubscriptionDto {
    /**
     * Custom feed subscription ID.
     */
    private String id;

    /**
     * Custom feed subscription title.
     */
    private String customFeedSubscriptionTitle;
    
    /**
     * Custom feed title.
     */
    private String customFeedTitle;

    /**
     * User ID.
     */
    private String userId;

    /**
     * Custom feed ID.
     */
    private String feedId;

    /**
     * Custom feed URL.
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
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Getter of customFeedSubscriptionTitle.
     *
     * @return customFeedSubscriptionTitle
     */
    public String getCustomFeedSubscriptionTitle() {
        return customFeedSubscriptionTitle;
    }

    /**
     * Setter of customFeedSubscriptionTitle.
     *
     * @param customFeedSubscriptionTitle customFeedSubscriptionTitle
     */
    public void setCustomFeedSubscriptionTitle(String customFeedSubscriptionTitle) {
        this.customFeedSubscriptionTitle = customFeedSubscriptionTitle;
    }

    /**
     * Getter of customFeedTitle.
     *
     * @return customFeedTitle
     */
    public String getCustomFeedTitle() {
        return customFeedTitle;
    }

    /**
     * Setter of customFeedTitle.
     *
     * @param customFeedTitle customFeedTitle
     */
    public void setCustomFeedTitle(String customFeedTitle) {
        this.customFeedTitle = customFeedTitle;
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
    public void setUserId(String userId) {
        this.userId = userId;
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
    public void setFeedId(String feedId) {
        this.feedId = feedId;
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
    public void setFeedUrl(String feedUrl) {
        this.feedUrl = feedUrl;
    }
}