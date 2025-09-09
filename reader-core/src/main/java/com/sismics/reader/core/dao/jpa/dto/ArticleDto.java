package com.sismics.reader.core.dao.jpa.dto;

import java.util.Date;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * Article DTO.
 *
 * @author jtremeaux 
 */
public class ArticleDto {
    /**
     * Article ID.
     */
    private String id;

    /**
     * Article URL.
     */
    private String url;

    /**
     * Article GUID.
     */
    private String guid;

    /**
     * Article title.
     */
    private String title;

    /**
     * Article creator.
     */
    private String creator;

    /**
     * Article description.
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
     * Enclosure size in bytes.
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
     * Feed ID.
     */
    private String feedId;
    
    /*8
     * Starred count.
     */
    private int starredCount;

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

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
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
     * Getter of feedId.
     *
     * @return feedId
     */
    public String getFeedId() {
        return feedId;
    }

    public int getStarredCount() {
        return starredCount;
    }

    public void setStarredCount(int starredCount) {
        this.starredCount = starredCount;
    }

    /**
     * Setter of feedId.
     *
     * @param feedId feedId
     */
    public void setFeedId(String feedId) {
        this.feedId = feedId;
    }

    public JSONObject asJson() throws JSONException {
        JSONObject ArticleJson = new JSONObject();
        ArticleJson.put("id", id);
        JSONObject subscription = new JSONObject();
        subscription.put("id",feedId);
        subscription.put("title", title);
        ArticleJson.put("subscription", subscription);
        ArticleJson.put("article_id", id);
        ArticleJson.put("title", title);
        ArticleJson.put("url", url);
        ArticleJson.put("date",publicationDate);
        ArticleJson.put("creator", creator);
        ArticleJson.put("description", description);
        ArticleJson.put("comment_url", commentUrl);
        ArticleJson.put("comment_count", commentCount);
        if (enclosureUrl != null) {
            JSONObject enclosure = new JSONObject();
            enclosure.put("url", enclosureUrl);
            enclosure.put("length", enclosureCount);
            enclosure.put("type", enclosureType);
            ArticleJson.put("enclosure", enclosure);
        }
        ArticleJson.put("is_read", false);
        ArticleJson.put("is_starred", true);
        ArticleJson.put("starred_count", starredCount);
        return ArticleJson;
    }
}
