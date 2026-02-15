package com.example.demo.services;

import com.example.demo.domain.Post;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final MongoTemplate mongoTemplate;

    public List<AuthorStats> getTopAuthors(int limit) {
        Aggregation aggregation = newAggregation(
                group("author").count().as("postCount"),
                sort(Sort.Direction.DESC, "postCount"),
                limit(limit),
                project("postCount").and("_id").as("authorName")
        );

        AggregationResults<AuthorStats> results = mongoTemplate.aggregate(
                aggregation, "posts", AuthorStats.class // Querying "posts" collection
        );

        return results.getMappedResults();
    }

    @Data
    @AllArgsConstructor
    public static class AuthorStats {
        private String authorName;
        private long postCount;
    }
}
