package com.example.demo.services;

import com.example.demo.repositories.PostRepository;
import com.example.demo.repositories.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final MongoTemplate mongoTemplate;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public List<AuthorStats> getTopAuthors(int limit) {
        Aggregation aggregation = newAggregation(
                group("author").count().as("postCount"),
                sort(Sort.Direction.DESC, "postCount"),
                limit(limit),
                project("postCount").and("_id").as("authorName")
        );

        AggregationResults<AuthorStats> results = mongoTemplate.aggregate(
                aggregation, "posts", AuthorStats.class
        );

        return results.getMappedResults();
    }

    public long getTotalPosts() {
        return postRepository.count();
    }

    public long getTotalUsers() {
        return userRepository.count();
    }

    public List<TagStats> getTopTags(int limit) {
        Aggregation aggregation = newAggregation(
                unwind("tagSlugs"),
                group("tagSlugs").count().as("tagCount"),
                sort(Sort.Direction.DESC, "tagCount"),
                limit(limit),
                project("tagCount").and("_id").as("tagName")
        );

        AggregationResults<TagStats> results = mongoTemplate.aggregate(
                aggregation, "posts", TagStats.class
        );

        return results.getMappedResults();
    }

    public double getAverageReviewsPerPost() {
        Aggregation aggregation = newAggregation(
                project()
                        .and(ConditionalOperators.ifNull("reviews").then(Collections.emptyList()))
                        .as("safeReviews"),
                project()
                        .and("safeReviews").size().as("reviewCount"),
                group()
                        .avg("reviewCount").as("averageReviews")
        );

        AggregationResults<org.bson.Document> results = mongoTemplate.aggregate(
                aggregation, "posts", org.bson.Document.class
        );

        org.bson.Document result = results.getUniqueMappedResult();
        
        if (result == null || !result.containsKey("averageReviews")) {
            return 0.0;
        }
        
        // Handle cases where avg might return a Decimal128 or Integer
        Object avg = result.get("averageReviews");
        if (avg instanceof Number) {
            return ((Number) avg).doubleValue();
        }
        return 0.0;
    }

    @Data
    @AllArgsConstructor
    public static class AuthorStats {
        private String authorName;
        private long postCount;
    }

    @Data
    @AllArgsConstructor
    public static class TagStats {
        private String tagName;
        private long tagCount;
    }
}
