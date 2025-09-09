package com.sismics.reader.core.service.filtering;
 
 import com.sismics.reader.core.dao.jpa.criteria.UserArticleCriteria;
 import java.util.List;
 
 public interface ArticleFilterStrategy {
     void applySelection(UserArticleCriteria criteria, List<String> selectedIds);
 
 }