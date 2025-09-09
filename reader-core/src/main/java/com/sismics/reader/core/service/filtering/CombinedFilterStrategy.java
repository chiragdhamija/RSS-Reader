package com.sismics.reader.core.service.filtering;

import com.sismics.reader.core.dao.jpa.criteria.UserArticleCriteria;

import java.util.ArrayList;
import java.util.List;

public class CombinedFilterStrategy implements ArticleFilterStrategy {

    private final ArticleFilterStrategy categoryStrategy = new CategoryBasedFilterStrategy();
    private final ArticleFilterStrategy feedStrategy = new FeedBasedFilterStrategy();

    @Override
    public void applySelection(UserArticleCriteria criteria, List<String> selectedIds) {
        List<String> categoryIds = extractCategoryIds(selectedIds);
        List<String> feedIds = extractFeedIds(selectedIds);

        applyCategoryFilter(criteria, categoryIds);
        applyFeedFilter(criteria, feedIds);
    }

    /**
     * Extracts category IDs from the list of selected IDs.
     */
    private List<String> extractCategoryIds(List<String> selectedIds) {
        List<String> categoryIds = new ArrayList<>();
        for (String id : selectedIds) {
            String[] parts = splitId(id);
            if (parts != null && "categoryId".equals(parts[0])) {
                categoryIds.add(parts[1]);
            }
        }
        return categoryIds;
    }

    /**
     * Extracts feed IDs from the list of selected IDs.
     */
    private List<String> extractFeedIds(List<String> selectedIds) {
        List<String> feedIds = new ArrayList<>();
        for (String id : selectedIds) {
            String[] parts = splitId(id);
            if (parts != null && "subscriptionId".equals(parts[0])) {
                feedIds.add(parts[1]);
            }
        }
        return feedIds;
    }

    /**
     * Helper to split the raw ID into prefix and actual ID.
     */
    private String[] splitId(String raw) {
        if (raw == null || !raw.contains(":")) return null;
        String[] parts = raw.split(":", 2);
        if (parts.length < 2) return null;
        return new String[] { parts[0].trim(), parts[1].trim() };
    }

    /**
     * Applies category-based filtering if applicable.
     */
    private void applyCategoryFilter(UserArticleCriteria criteria, List<String> categoryIds) {
        if (!categoryIds.isEmpty()) {
            categoryStrategy.applySelection(criteria, categoryIds);
        }
    }

    /**
     * Applies feed-based filtering if applicable.
     */
    private void applyFeedFilter(UserArticleCriteria criteria, List<String> feedIds) {
        if (!feedIds.isEmpty()) {
            feedStrategy.applySelection(criteria, feedIds);
        }
    }
}
