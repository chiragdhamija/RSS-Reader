package com.sismics.reader.core.similarity.strategy;

import com.sismics.reader.core.dao.jpa.dto.ArticleDto;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.util.*;

import java.util.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NerUtils {

    public static double calculateSimilarityScore(ArticleDto article1, ArticleDto article2, StanfordCoreNLP pipeline) {
        Map<String, List<String>> entities1 = extractNamedEntities(article1.getDescription(), pipeline);
        Map<String, List<String>> entities2 = extractNamedEntities(article2.getDescription(), pipeline);

        if (entities1.isEmpty() && entities2.isEmpty()) {
            return calculateBasicSimilarity(article1.getDescription(), article2.getDescription());
        }

        double entityScore = calculateEntitySimilarity(entities1, entities2);
        double titleScore = calculateBasicSimilarity(article1.getTitle(), article2.getTitle());
        double contentScore = calculateBasicSimilarity(article1.getDescription(), article2.getDescription());

        double finalScore = (0.6 * entityScore) + (0.3 * titleScore) + (0.1 * contentScore);
        return Math.min(finalScore, 100.0);
    }

    public static double calculateEntitySimilarity(Map<String, List<String>> entities1, Map<String, List<String>> entities2) {
        Set<String> allEntities1 = new HashSet<>();
        Set<String> allEntities2 = new HashSet<>();

        // Java 8 replacement for Map.of(...)
        Map<String, Double> typeWeights = new HashMap<String, Double>();
        typeWeights.put("PERSON", 2.0);
        typeWeights.put("ORGANIZATION", 1.5);
        typeWeights.put("LOCATION", 1.5);
        typeWeights.put("DATE", 0.5);
        typeWeights.put("TIME", 0.5);
        typeWeights.put("MONEY", 1.0);
        typeWeights.put("PERCENT", 0.5);

        double weightedCount1 = 0, weightedCount2 = 0;
        for (Map.Entry<String, List<String>> entry : entities1.entrySet()) {
            String type = entry.getKey();
            for (String entity : entry.getValue()) {
                allEntities1.add(type + ":" + entity.toLowerCase());
                weightedCount1 += typeWeights.getOrDefault(type, 1.0);
            }
        }

        for (Map.Entry<String, List<String>> entry : entities2.entrySet()) {
            String type = entry.getKey();
            for (String entity : entry.getValue()) {
                allEntities2.add(type + ":" + entity.toLowerCase());
                weightedCount2 += typeWeights.getOrDefault(type, 1.0);
            }
        }

        Set<String> commonEntities = new HashSet<>(allEntities1);
        commonEntities.retainAll(allEntities2);

        double weightedCommonCount = 0;
        for (String entity : commonEntities) {
            String type = entity.split(":")[0];
            weightedCommonCount += typeWeights.getOrDefault(type, 1.0);
        }

        double total = weightedCount1 + weightedCount2 - weightedCommonCount;
        double score = total == 0 ? 0 : (weightedCommonCount * 100.0) / total;

        if (commonEntities.size() >= 3) score += 10;
        return Math.min(score, 100.0);
    }

    public static double calculateBasicSimilarity(String text1, String text2) {
        if (text1 == null || text2 == null || text1.isEmpty() || text2.isEmpty()) return 0;
        Set<String> tokens1 = tokenize(text1);
        Set<String> tokens2 = tokenize(text2);
        if (tokens1.isEmpty() && tokens2.isEmpty()) return 0;
        Set<String> intersection = new HashSet<>(tokens1);
        intersection.retainAll(tokens2);
        Set<String> union = new HashSet<>(tokens1);
        union.addAll(tokens2);
        return (intersection.size() * 100.0) / union.size();
    }

    public static Set<String> tokenize(String text) {
        Set<String> tokens = new HashSet<>();
        String[] words = text.toLowerCase().split("\\W+");
        // Java 8 replacement for Set.of(...)
        Set<String> stopwords = new HashSet<String>(Arrays.asList(
            "a", "an", "the", "and", "but", "if", "or", "because", "as", "what",
            "for", "with", "to", "from", "in", "out", "on", "off", "of", "by", "so", "this", "that",
            "is", "are", "was", "were", "it"
        ));
        for (String word : words) {
            if (word.length() > 2 && !stopwords.contains(word)) {
                tokens.add(word);
            }
        }
        return tokens;
    }

    public static Map<String, List<String>> extractNamedEntities(String text, StanfordCoreNLP pipeline) {
        Map<String, List<String>> entityMap = new HashMap<>();
        if (text == null || text.trim().isEmpty()) return entityMap;
        Annotation document = new Annotation(text);
        pipeline.annotate(document);
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {
            String currentType = "O";
            StringBuilder currentEntity = new StringBuilder();
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String type = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                String word = token.get(CoreAnnotations.TextAnnotation.class);
                if ("O".equals(type) || !type.equals(currentType)) {
                    if (!currentType.equals("O") && currentEntity.length() > 0) {
                        if (!entityMap.containsKey(currentType)) {
                            entityMap.put(currentType, new ArrayList<String>());
                        }
                        entityMap.get(currentType).add(currentEntity.toString().trim());
                        currentEntity.setLength(0);
                    }
                    currentType = type;
                    if (!"O".equals(type)) {
                        currentEntity.append(word);
                    }
                } else {
                    currentEntity.append(" ").append(word);
                }
            }
            if (!"O".equals(currentType) && currentEntity.length() > 0) {
                if (!entityMap.containsKey(currentType)) {
                    entityMap.put(currentType, new ArrayList<String>());
                }
                entityMap.get(currentType).add(currentEntity.toString().trim());
            }
        }
        return entityMap;
    }
}
