package com.sismics.reader.core.service.filtering;
 
 import com.sismics.reader.core.dao.jpa.criteria.UserArticleCriteria;
 import java.util.List;
 
 public class CategoryBasedFilterStrategy implements ArticleFilterStrategy {
     @Override
     public void applySelection(UserArticleCriteria criteria, List<String> selectedIds) {
         // Assuming the selection is a list of category IDs.
         criteria.setCategoryIds(selectedIds);
     }
 }