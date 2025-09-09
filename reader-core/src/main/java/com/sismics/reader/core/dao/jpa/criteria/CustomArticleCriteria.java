package com.sismics.reader.core.dao.jpa.criteria;

import java.util.Date;
import java.util.List;

/**
 * Custom article criteria.
 *
 * @author jtremeaux
 */
public class CustomArticleCriteria {
    private String origId;
    /**
     * CustomArticle ID.
     */
    private String id;
    
    /**
     * CustomArticle GUID list (inclusive).
     */
    private List<String> guidIn;
    
    /**
     * CustomArticle title.
     */
    private String title;
    
    /**
     * CustomArticle url.
     */
    private String url;
    
    /**
     * Max publication date.
     */
    private Date publicationDateMin;
    
    /**
     * CustomFeed ID.
     */
    private String customFeedId;
    
    public String getOrigId() {
        return origId;
    }
    public CustomArticleCriteria setOrigId(String origId) {
        this.origId = origId;
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
     */
    public CustomArticleCriteria setId(String id) {
        this.id = id;
        return this;
    }
    
    /**
     * Getter of guidIn.
     *
     * @return guidIn
     */
    public List<String> getGuidIn() {
        return guidIn;
    }
    
    /**
     * Setter of guidIn.
     *
     * @param guidIn guidIn
     */
    public CustomArticleCriteria setGuidIn(List<String> guidIn) {
        this.guidIn = guidIn;
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
     */
    public CustomArticleCriteria setTitle(String title) {
        this.title = title;
        return this;
    }
    
    /**
     * Getter of url.
     *
     * @return url
     */
    public String getUrl() {
        return url;
    }
    
    /**
     * Setter of url.
     *
     * @param url url
     */
    public CustomArticleCriteria setUrl(String url) {
        this.url = url;
        return this;
    }
    
    /**
     * Getter of publicationDateMin.
     *
     * @return publicationDateMin
     */
    public Date getPublicationDateMin() {
        return publicationDateMin;
    }
    
    /**
     * Setter of publicationDateMin.
     *
     * @param publicationDateMin publicationDateMin
     */
    public CustomArticleCriteria setPublicationDateMin(Date publicationDateMin) {
        this.publicationDateMin = publicationDateMin;
        return this;
    }
    
    /**
     * Getter of customFeedId.
     *
     * @return customFeedId
     */
    public String getCustomFeedId() {
        return customFeedId;
    }
    
    /**
     * Setter of customFeedId.
     *
     * @param customFeedId customFeedId
     */
    public CustomArticleCriteria setCustomFeedId(String customFeedId) {
        this.customFeedId = customFeedId;
        return this;
    }
}