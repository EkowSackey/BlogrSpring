package com.example.demo.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "jwt.secret=test-secret-key-for-testing-purposes-only"
})
public class AnalyticsServiceAsyncTest {

    @Autowired
    private AnalyticsService analyticsService;

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetTotalPostsAsync() throws ExecutionException, InterruptedException {
        // Act
        CompletableFuture<Long> future = analyticsService.getTotalPosts();

        // Assert
        assertNotNull(future);
        // Wait for the result
        Long result = future.get();
        assertNotNull(result);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetTopAuthorsAsync() throws ExecutionException, InterruptedException {
        // Act
        CompletableFuture<java.util.List<AnalyticsService.AuthorStats>> future = analyticsService.getTopAuthors(5);

        // Assert
        assertNotNull(future);
        java.util.List<AnalyticsService.AuthorStats> result = future.get();
        assertNotNull(result);
    }
}
