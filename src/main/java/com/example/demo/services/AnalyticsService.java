package com.example.demo.services;

import com.example.demo.repositories.PostRepository;
import com.example.demo.repositories.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final MongoTemplate mongoTemplate;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Async("taskExecutor")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHOR')")
    @Transactional(readOnly = true)
    @Cacheable(value = "analytics-authors", key = "#limit")
    public CompletableFuture<List<AuthorStats>> getTopAuthors(int limit) {
        Aggregation aggregation = newAggregation(
                group("author").count().as("postCount"),
                sort(Sort.Direction.DESC, "postCount"),
                limit(limit),
                project("postCount").and("_id").as("authorName")
        );

        AggregationResults<AuthorStats> results = mongoTemplate.aggregate(
                aggregation, "posts", AuthorStats.class
        );

        return CompletableFuture.completedFuture(results.getMappedResults());
    }

    @Async("taskExecutor")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHOR')")
    @Transactional(readOnly = true)
    @Cacheable(value = "analytics-posts", key = "'total'")
    public CompletableFuture<Long> getTotalPosts() {
        return CompletableFuture.completedFuture(postRepository.count());
    }

    @Async("taskExecutor")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHOR')")
    @Transactional(readOnly = true)
    @Cacheable(value = "analytics-users", key = "'total'")
    public CompletableFuture<Long> getTotalUsers() {
        return CompletableFuture.completedFuture(userRepository.count());
    }

    @Async("taskExecutor")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHOR')")
    @Transactional(readOnly = true)
    @Cacheable(value = "analytics-tags", key = "#limit")
    public CompletableFuture<List<TagStats>> getTopTags(int limit) {
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

        return CompletableFuture.completedFuture(results.getMappedResults());
    }

    @Async("taskExecutor")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHOR')")
    @Transactional(readOnly = true)
    @Cacheable(value = "analytics-reviews", key = "'avg'")
    public CompletableFuture<Double> getAverageReviewsPerPost() {
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
            return CompletableFuture.completedFuture(0.0);
        }
        
        // Handle cases where avg might return a Decimal128 or Integer
        Object avg = result.get("averageReviews");
        if (avg instanceof Number) {
            return CompletableFuture.completedFuture(((Number) avg).doubleValue());
        }
        return CompletableFuture.completedFuture(0.0);
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
