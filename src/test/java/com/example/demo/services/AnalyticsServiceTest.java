package com.example.demo.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private AnalyticsService analyticsService;

    @Test
    void getTopAuthors_shouldReturnAuthorStats() {
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
        List<AnalyticsService.AuthorStats> result = analyticsService.getTopAuthors(limit);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("author1", result.get(0).getAuthorName());
        assertEquals(10L, result.get(0).getPostCount());

        verify(mongoTemplate).aggregate(any(Aggregation.class), eq("posts"), eq(AnalyticsService.AuthorStats.class));
    }
}
