package com.example.demo.controllers;

import com.example.demo.config.OAuth2LoginSuccessHandler;
import com.example.demo.config.SecurityConfig;
import com.example.demo.filter.JwtAuthFilter;
import com.example.demo.services.AnalyticsService;
import com.example.demo.services.CustomOAuth2UserService;
import com.example.demo.services.TokenBlacklistService;
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
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
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

    @MockBean
    private CustomOAuth2UserService customOAuth2UserService;

    @MockBean
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @MockBean
    private TokenBlacklistService tokenBlacklistService;

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
    @WithMockUser(authorities = "ROLE_ADMIN")
    void getTopAuthors_shouldReturnList_forAdmin() throws Exception {
        List<AnalyticsService.AuthorStats> stats = List.of(
                new AnalyticsService.AuthorStats("author1", 10L)
        );
        when(analyticsService.getTopAuthors(anyInt())).thenReturn(CompletableFuture.completedFuture(stats));

        MvcResult mvcResult = mockMvc.perform(get("/api/v1/analytics/top-authors"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].authorName").value("author1"))
                .andExpect(jsonPath("$[0].postCount").value(10));
    }

    @Test
    @WithMockUser(authorities = "ROLE_AUTHOR")
    void getTopAuthors_shouldReturnForbidden_forAuthor() throws Exception {
        // Even though the service allows AUTHOR, the filter chain now restricts /api/v1/analytics/** to ADMIN
        mockMvc.perform(get("/api/v1/analytics/top-authors"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void getTotalPosts_shouldReturnCount() throws Exception {
        when(analyticsService.getTotalPosts()).thenReturn(CompletableFuture.completedFuture(100L));

        MvcResult mvcResult = mockMvc.perform(get("/api/v1/analytics/total-posts"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().string("100"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void getTotalUsers_shouldReturnCount() throws Exception {
        when(analyticsService.getTotalUsers()).thenReturn(CompletableFuture.completedFuture(50L));

        MvcResult mvcResult = mockMvc.perform(get("/api/v1/analytics/total-users"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().string("50"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void getTopTags_shouldReturnList() throws Exception {
        List<AnalyticsService.TagStats> stats = List.of(
                new AnalyticsService.TagStats("tag1", 20L)
        );
        when(analyticsService.getTopTags(anyInt())).thenReturn(CompletableFuture.completedFuture(stats));

        MvcResult mvcResult = mockMvc.perform(get("/api/v1/analytics/top-tags"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tagName").value("tag1"))
                .andExpect(jsonPath("$[0].tagCount").value(20));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void getAverageReviews_shouldReturnDouble() throws Exception {
        when(analyticsService.getAverageReviewsPerPost()).thenReturn(CompletableFuture.completedFuture(3.5));

        MvcResult mvcResult = mockMvc.perform(get("/api/v1/analytics/average-reviews"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().string("3.5"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_READER")
    void getAnalytics_shouldReturnForbidden_forReader() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/total-posts"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAnalytics_withoutAuth_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/total-posts"))
                .andExpect(status().isUnauthorized());
    }
}
