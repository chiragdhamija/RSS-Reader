package com.sismics.reader.core.dao.jpa.dto;

import java.util.Date;

/**
 * Custom article DTO.
 *
 * @author jtremeaux
 */
public class CustomArticleDto {
    private String origId;
    /**
     * Custom article ID.
     */
    private String id;

    /**
     * Custom article URL.
     */
    private String url;
    
    /**
     * Custom article GUID.
     */
    private String guid;
    
    /**
     * Custom article title.
     */
    private String title;
    
    /**
     * Custom article creator.
     */
    private String creator;
    
    /**
     * Custom article description.
     */
    private String description;
    
    /**
     * Comment URL.
     */
    private String commentUrl;
    
    /**
     * Comment count.
     */
    private Integer commentCount;
    
    /**
     * Enclosure URL.
     */
    private String enclosureUrl;
    
    /**
     * Enclosure length in bytes.
     */
    private Integer enclosureCount;
    
    /**
     * Enclosure MIME type.
     */
    private String enclosureType;
    
    /**
     * Publication date.
     */
    private Date publicationDate;
    
    /**
     * Creation date.
     */
    private Date createDate;
    
    /**
     * Custom feed ID.
     */
    private String customFeedId;

    public String getOrigId() {
        return origId;
    }
    public void setOrigId(String origId) {
        this.origId = origId;
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
    public void setId(String id) {
        this.id = id;
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
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Getter of guid.
     *
     * @return guid
     */
    public String getGuid() {
        return guid;
    }

    /**
     * Setter of guid.
     *
     * @param guid guid
     */
    public void setGuid(String guid) {
        this.guid = guid;
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
     * Getter of creator.
     *
     * @return creator
     */
    public String getCreator() {
        return creator;
    }

    /**
     * Setter of creator.
     *
     * @param creator creator
     */
    public void setCreator(String creator) {
        this.creator = creator;
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
     * Getter of commentUrl.
     *
     * @return commentUrl
     */
    public String getCommentUrl() {
        return commentUrl;
    }

    /**
     * Setter of commentUrl.
     *
     * @param commentUrl commentUrl
     */
    public void setCommentUrl(String commentUrl) {
        this.commentUrl = commentUrl;
    }

    /**
     * Getter of commentCount.
     *
     * @return commentCount
     */
    public Integer getCommentCount() {
        return commentCount;
    }

    /**
     * Setter of commentCount.
     *
     * @param commentCount commentCount
     */
    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    /**
     * Getter of enclosureUrl.
     *
     * @return enclosureUrl
     */
    public String getEnclosureUrl() {
        return enclosureUrl;
    }

    /**
     * Setter of enclosureUrl.
     *
     * @param enclosureUrl enclosureUrl
     */
    public void setEnclosureUrl(String enclosureUrl) {
        this.enclosureUrl = enclosureUrl;
    }

    /**
     * Getter of enclosureCount.
     *
     * @return enclosureCount
     */
    public Integer getEnclosureCount() {
        return enclosureCount;
    }

    /**
     * Setter of enclosureCount.
     *
     * @param enclosureCount enclosureCount
     */
    public void setEnclosureCount(Integer enclosureCount) {
        this.enclosureCount = enclosureCount;
    }

    /**
     * Getter of enclosureType.
     *
     * @return enclosureType
     */
    public String getEnclosureType() {
        return enclosureType;
    }

    /**
     * Setter of enclosureType.
     *
     * @param enclosureType enclosureType
     */
    public void setEnclosureType(String enclosureType) {
        this.enclosureType = enclosureType;
    }

    /**
     * Getter of publicationDate.
     *
     * @return publicationDate
     */
    public Date getPublicationDate() {
        return publicationDate;
    }

    /**
     * Setter of publicationDate.
     *
     * @param publicationDate publicationDate
     */
    public void setPublicationDate(Date publicationDate) {
        this.publicationDate = publicationDate;
    }

    /**
     * Getter of createDate.
     *
     * @return createDate
     */
    public Date getCreateDate() {
        return createDate;
    }

    /**
     * Setter of createDate.
     *
     * @param createDate createDate
     */
    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
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
    public void setCustomFeedId(String customFeedId) {
        this.customFeedId = customFeedId;
    }
}