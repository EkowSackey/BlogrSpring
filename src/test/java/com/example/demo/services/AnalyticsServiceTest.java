package com.example.demo.services;

import com.example.demo.repositories.PostRepository;
import com.example.demo.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    @Test
    void getTopAuthors_shouldReturnAuthorStats() throws Exception {
        // Arrange
        int limit = 5;
        AnalyticsService.AuthorStats stats = new AnalyticsService.AuthorStats("author1", 10L);
        AggregationResults<AnalyticsService.AuthorStats> results = new AggregationResults<>(
                Collections.singletonList(stats),
                new org.bson.Document()
        );

        when(mongoTemplate.aggregate(any(Aggregation.class), eq("posts"), eq(AnalyticsService.AuthorStats.class)))
                .thenReturn(results);

        // Act
        CompletableFuture<List<AnalyticsService.AuthorStats>> future = analyticsService.getTopAuthors(limit);
        List<AnalyticsService.AuthorStats> result = future.get();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("author1", result.get(0).getAuthorName());
        assertEquals(10L, result.get(0).getPostCount());

        verify(mongoTemplate).aggregate(any(Aggregation.class), eq("posts"), eq(AnalyticsService.AuthorStats.class));
    }

    @Test
    void getTotalPosts_shouldReturnCount() throws Exception {
        // Arrange
        when(postRepository.count()).thenReturn(100L);

        // Act
        CompletableFuture<Long> future = analyticsService.getTotalPosts();
        long result = future.get();

        // Assert
        assertEquals(100L, result);
        verify(postRepository).count();
    }

    @Test
    void getTotalUsers_shouldReturnCount() throws Exception {
        // Arrange
        when(userRepository.count()).thenReturn(50L);

        // Act
        CompletableFuture<Long> future = analyticsService.getTotalUsers();
        long result = future.get();

        // Assert
        assertEquals(50L, result);
        verify(userRepository).count();
    }

    @Test
    void getTopTags_shouldReturnTagStats() throws Exception {
        // Arrange
        int limit = 5;
        AnalyticsService.TagStats stats = new AnalyticsService.TagStats("tag1", 20L);
        AggregationResults<AnalyticsService.TagStats> results = new AggregationResults<>(
                Collections.singletonList(stats),
                new org.bson.Document()
        );

        when(mongoTemplate.aggregate(any(Aggregation.class), eq("posts"), eq(AnalyticsService.TagStats.class)))
                .thenReturn(results);

        // Act
        CompletableFuture<List<AnalyticsService.TagStats>> future = analyticsService.getTopTags(limit);
        List<AnalyticsService.TagStats> result = future.get();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("tag1", result.get(0).getTagName());
        assertEquals(20L, result.get(0).getTagCount());

        verify(mongoTemplate).aggregate(any(Aggregation.class), eq("posts"), eq(AnalyticsService.TagStats.class));
    }

    @Test
    void getAverageReviewsPerPost_shouldReturnAverage() throws Exception {
        // Arrange
        org.bson.Document doc = new org.bson.Document("averageReviews", 3.5);
        AggregationResults<org.bson.Document> results = new AggregationResults<>(
                Collections.singletonList(doc),
                new org.bson.Document()
        );

        when(mongoTemplate.aggregate(any(Aggregation.class), eq("posts"), eq(org.bson.Document.class)))
                .thenReturn(results);

        // Act
        CompletableFuture<Double> future = analyticsService.getAverageReviewsPerPost();
        double result = future.get();

        // Assert
        assertEquals(3.5, result);
        verify(mongoTemplate).aggregate(any(Aggregation.class), eq("posts"), eq(org.bson.Document.class));
    }

    @Test
    void getAverageReviewsPerPost_shouldReturnZeroIfNoResults() throws Exception {
        // Arrange
        AggregationResults<org.bson.Document> results = new AggregationResults<>(
                Collections.emptyList(),
                new org.bson.Document()
        );

        when(mongoTemplate.aggregate(any(Aggregation.class), eq("posts"), eq(org.bson.Document.class)))
                .thenReturn(results);

        // Act
        CompletableFuture<Double> future = analyticsService.getAverageReviewsPerPost();
        double result = future.get();

        // Assert
        assertEquals(0.0, result);
        verify(mongoTemplate).aggregate(any(Aggregation.class), eq("posts"), eq(org.bson.Document.class));
    }
}
