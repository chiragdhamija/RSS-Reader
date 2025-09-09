package com.sismics.reader.core.model.jpa;

import com.google.common.base.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Subscription from a user to a custom feed.
 * 
 * @author jtremeaux
 */
@Entity
@Table(name = "T_CUSTOM_FEED_SUBSCRIPTION")
public class CustomFeedSubscription {
    /**
     * Subscription ID.
     */
    @Id
    @Column(name = "CFS_ID_C", length = 36)
    private String id;
    
    /**
     * User ID.
     */
    @Column(name = "CFS_IDUSER_C", nullable = false, length = 36)
    private String userId;
    
    /**
     * Feed ID.
     */
    @Column(name = "CFS_IDFEED_C", nullable = false, length = 36)
    private String feedId;
    
    /**
     * Subscription title (overrides feed title).
     */
    @Column(name = "CFS_TITLE_C", length = 100)
    private String title;
    
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

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("userId", userId)
                .add("feedId", feedId)
                .add("title", title)
                .toString();
    }
}