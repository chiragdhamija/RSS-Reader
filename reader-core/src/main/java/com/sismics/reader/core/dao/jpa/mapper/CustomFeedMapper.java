package com.sismics.reader.core.dao.jpa.mapper;

import com.sismics.reader.core.dao.jpa.dto.CustomFeedDto;
import com.sismics.util.jpa.ResultMapper;

/**
 * Custom feed result mapper.
 *
 * @author jtremeaux
 */
public class CustomFeedMapper extends ResultMapper<CustomFeedDto> {
    @Override
    public CustomFeedDto map(Object[] o) {
        int i = 0;
        CustomFeedDto dto = new CustomFeedDto();
        dto.setId(stringValue(o[i++]));
        dto.setCustomUserId(stringValue(o[i++]));
        dto.setTitle(stringValue(o[i++]));
        dto.setDescription(stringValue(o[i]));
        return dto;
    }
}