package com.example.demo.controllers;

import com.example.demo.config.SecurityConfig;
import com.example.demo.filter.JwtAuthFilter;
import com.example.demo.services.AnalyticsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AnalyticsController.class)
@Import(SecurityConfig.class)
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalyticsService analyticsService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @MockBean
    private AuthenticationProvider authenticationProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(invocation -> {
            HttpServletRequest request = invocation.getArgument(0);
            HttpServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(jwtAuthFilter).doFilter(any(), any(), any());
    }

    @Test
    @WithMockUser
    void getTopAuthors_shouldReturnList() throws Exception {
        List<AnalyticsService.AuthorStats> stats = List.of(
                new AnalyticsService.AuthorStats("author1", 10L)
        );
        when(analyticsService.getTopAuthors(anyInt())).thenReturn(stats);

        mockMvc.perform(get("/api/v1/analytics/top-authors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].authorName").value("author1"))
                .andExpect(jsonPath("$[0].postCount").value(10));
    }

    @Test
    @WithMockUser
    void getTotalPosts_shouldReturnCount() throws Exception {
        when(analyticsService.getTotalPosts()).thenReturn(100L);

        mockMvc.perform(get("/api/v1/analytics/total-posts"))
                .andExpect(status().isOk())
                .andExpect(content().string("100"));
    }

    @Test
    @WithMockUser
    void getTotalUsers_shouldReturnCount() throws Exception {
        when(analyticsService.getTotalUsers()).thenReturn(50L);

        mockMvc.perform(get("/api/v1/analytics/total-users"))
                .andExpect(status().isOk())
                .andExpect(content().string("50"));
    }

    @Test
    @WithMockUser
    void getTopTags_shouldReturnList() throws Exception {
        List<AnalyticsService.TagStats> stats = List.of(
                new AnalyticsService.TagStats("tag1", 20L)
        );
        when(analyticsService.getTopTags(anyInt())).thenReturn(stats);

        mockMvc.perform(get("/api/v1/analytics/top-tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tagName").value("tag1"))
                .andExpect(jsonPath("$[0].tagCount").value(20));
    }

    @Test
    @WithMockUser
    void getAverageReviews_shouldReturnDouble() throws Exception {
        when(analyticsService.getAverageReviewsPerPost()).thenReturn(3.5);

        mockMvc.perform(get("/api/v1/analytics/average-reviews"))
                .andExpect(status().isOk())
                .andExpect(content().string("3.5"));
    }

    @Test
    void getAnalytics_withoutAuth_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/total-posts"))
                .andExpect(status().isForbidden());
    }
}
