package com.example.demo.controllers;

import com.example.demo.services.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
@Tag(name = "Analytics", description = "API for blog analytics and statistics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Operation(summary = "Get top authors", description = "Retrieves a list of authors with the highest post counts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved top authors",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = AnalyticsService.AuthorStats.class)))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHOR')")
    @GetMapping("/top-authors")
    public ResponseEntity<List<AnalyticsService.AuthorStats>> getTopAuthors(
            @Parameter(description = "Maximum number of authors to return")
            @RequestParam(defaultValue = "5") int limit
    ) {
        return ResponseEntity.ok(analyticsService.getTopAuthors(limit));
    }

    @Operation(summary = "Get total posts", description = "Retrieves the total number of posts in the blog")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved total posts count"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHOR')")
    @GetMapping("/total-posts")
    public ResponseEntity<Long> getTotalPosts() {
        return ResponseEntity.ok(analyticsService.getTotalPosts());
    }

    @Operation(summary = "Get total users", description = "Retrieves the total number of registered users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved total users count"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHOR')")
    @GetMapping("/total-users")
    public ResponseEntity<Long> getTotalUsers() {
        return ResponseEntity.ok(analyticsService.getTotalUsers());
    }

    @Operation(summary = "Get top tags", description = "Retrieves a list of the most popular tags and their counts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved top tags",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = AnalyticsService.TagStats.class)))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHOR')")
    @GetMapping("/top-tags")
    public ResponseEntity<List<AnalyticsService.TagStats>> getTopTags(
            @Parameter(description = "Maximum number of tags to return")
            @RequestParam(defaultValue = "5") int limit
    ) {
        return ResponseEntity.ok(analyticsService.getTopTags(limit));
    }

    @Operation(summary = "Get average reviews per post", description = "Calculates the average number of reviews per post")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved average reviews count"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHOR')")
    @GetMapping("/average-reviews")
    public ResponseEntity<Double> getAverageReviews() {
        return ResponseEntity.ok(analyticsService.getAverageReviewsPerPost());
    }
}
