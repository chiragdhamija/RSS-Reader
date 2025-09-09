package com.sismics.reader.core.dao.jpa.mapper;

import com.sismics.reader.core.dao.jpa.dto.CustomArticleDto;
import com.sismics.util.jpa.ResultMapper;

/**
 * Custom article mapper.
 * 
 * @author jtremeaux
 */
public class CustomArticleMapper extends ResultMapper<CustomArticleDto> {
    @Override
    public CustomArticleDto map(Object[] o) {
        int i = 0;
        CustomArticleDto dto = new CustomArticleDto();
        dto.setOrigId(stringValue(o[i++]));
        dto.setId(stringValue(o[i++]));
        dto.setUrl(stringValue(o[i++]));
        dto.setGuid(stringValue(o[i++]));
        dto.setTitle(stringValue(o[i++]));
        dto.setCreator(stringValue(o[i++]));
        dto.setDescription(stringValue(o[i++]));
        dto.setCommentUrl(stringValue(o[i++]));
        dto.setCommentCount(intValue(o[i++]));
        dto.setEnclosureUrl(stringValue(o[i++]));
        dto.setEnclosureCount(intValue(o[i++]));
        dto.setEnclosureType(stringValue(o[i++]));
        dto.setPublicationDate(dateValue(o[i++]));
        dto.setCreateDate(dateValue(o[i++]));
        dto.setCustomFeedId(stringValue(o[i]));
        return dto;
    }
}