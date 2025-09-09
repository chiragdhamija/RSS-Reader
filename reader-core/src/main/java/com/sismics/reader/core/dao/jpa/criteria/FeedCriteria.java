package com.sismics.reader.core.dao.jpa.criteria;

/**
 * Feed criteria.
 *
 * @author jtremeaux 
 */
public class FeedCriteria {
    /**
     * Feed URL.
     */
    private String feedUrl;
    
    /**
     * Returns only feed having user subscriptions.
     */
    private boolean withUserSubscription;

    // /**
    //  * Returns only custom feeds.
    //  */
    // private boolean custom;

    // /**
    //  * Returns only feeds created by the user with the given ID.
    //  */
    // private String customUserid;

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
    public FeedCriteria setFeedUrl(String feedUrl) {
        this.feedUrl = feedUrl;
        return this;
    }

    /**
     * Getter of withUserSubscription.
     *
     * @return withUserSubscription
     */
    public boolean isWithUserSubscription() {
        return withUserSubscription;
    }

    /**
     * Setter of withUserSubscription.
     *
     * @param withUserSubscription withUserSubscription
     */
    public FeedCriteria setWithUserSubscription(boolean withUserSubscription) {
        this.withUserSubscription = withUserSubscription;
        return this;
    }

    // /**
    //  * Getter of custom.
    //  * 
    //  * @param custom
    //  */
    // public boolean isCustom() {
    //     return custom;
    // }

    // /**
    //  * Setter of custom.
    //  * 
    //  * @param custom custom
    //  */
    // public FeedCriteria setCustom(boolean custom) {
    //     this.custom = custom;
    //     return this;
    // }

    // /**
    //  * Getter of customUserid.
    //  * 
    //  * @param customUserid
    //  */
    // public String getCustomUserid() {
    //     return customUserid;
    // }

    // /**
    //  * Setter of customUserid.
    //  * 
    //  * @param customUserid customUserid
    //  */
    // public FeedCriteria setCustomUserid(String customUserid) {
    //     this.customUserid = customUserid;
    //     return this;
    // }
}
