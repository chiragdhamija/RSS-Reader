package com.sismics.reader.core.service.filtering;
 import com.sismics.reader.core.dao.jpa.criteria.UserArticleCriteria;
 import java.util.List;
 
 public class FeedBasedFilterStrategy implements ArticleFilterStrategy {
     @Override
     public void applySelection(UserArticleCriteria criteria, List<String> selectedIds) {
         // Set the list of feed IDs in the criteria.
         // print the ids
         
         criteria.setFeedIds(selectedIds);
     }
 }