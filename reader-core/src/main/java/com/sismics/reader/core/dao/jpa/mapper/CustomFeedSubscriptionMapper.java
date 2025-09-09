package com.sismics.reader.core.dao.jpa.mapper;

import com.sismics.reader.core.dao.jpa.dto.CustomFeedSubscriptionDto;
import com.sismics.util.jpa.ResultMapper;

/**
 * @author jtremeaux
 */
public class CustomFeedSubscriptionMapper extends ResultMapper<CustomFeedSubscriptionDto> {
    @Override
    public CustomFeedSubscriptionDto map(Object[] o) {
        int i = 0;
        CustomFeedSubscriptionDto dto = new CustomFeedSubscriptionDto();
        dto.setId(stringValue(o[i++]));
        String customFeedSubscriptionTitle = stringValue(o[i++]);
        dto.setUserId(stringValue(o[i++]));
        dto.setFeedId(stringValue(o[i++]));
        String feedTitle = stringValue(o[i++]);
        dto.setCustomFeedSubscriptionTitle(customFeedSubscriptionTitle != null ? customFeedSubscriptionTitle : feedTitle);
        dto.setCustomFeedTitle(feedTitle);

        return dto;
    }
} 

