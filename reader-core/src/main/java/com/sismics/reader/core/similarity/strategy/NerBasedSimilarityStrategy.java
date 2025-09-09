package com.sismics.reader.core.similarity.strategy;

import com.sismics.reader.core.dao.jpa.dto.ArticleDto;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class NerBasedSimilarityStrategy implements ArticleSimilarityStrategy {
    private final StanfordCoreNLP pipeline;

    public NerBasedSimilarityStrategy(StanfordCoreNLP pipeline) {
        this.pipeline = pipeline;
    }

    @Override
    public double calculateSimilarity(ArticleDto article1, ArticleDto article2) {
        return NerUtils.calculateSimilarityScore(article1, article2, pipeline);
    }
}
