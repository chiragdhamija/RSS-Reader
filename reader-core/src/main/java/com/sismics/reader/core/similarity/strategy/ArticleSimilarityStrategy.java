package com.sismics.reader.core.similarity.strategy;

import com.sismics.reader.core.dao.jpa.dto.ArticleDto;

public interface ArticleSimilarityStrategy {
    double calculateSimilarity(ArticleDto article1, ArticleDto article2);
}
