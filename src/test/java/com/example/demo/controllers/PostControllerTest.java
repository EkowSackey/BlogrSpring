package com.example.demo.controllers;

import com.example.demo.config.OAuth2LoginSuccessHandler;
import com.example.demo.config.SecurityConfig;
import com.example.demo.domain.Post;
import com.example.demo.dto.CreatePostRequest;
import com.example.demo.dto.UpdatePostRequest;
import com.example.demo.dto.ReviewRequest;
import com.example.demo.filter.JwtAuthFilter;
import com.example.demo.services.CustomOAuth2UserService;
import com.example.demo.services.PostService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostController.class)
@Import(SecurityConfig.class)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;

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

    private Post samplePost;

    @BeforeEach
    void setUp() throws Exception {
        // Mock the filter to just pass through
        doAnswer(invocation -> {
            HttpServletRequest request = invocation.getArgument(0);
            HttpServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(jwtAuthFilter).doFilter(any(), any(), any());

        samplePost = new Post("Test Title", "Test Content", List.of("tag1"));
        samplePost.setPostId("123");
        samplePost.setAuthor("testUser");
        samplePost.setDateCreated(Instant.now());
        samplePost.setLastUpdate(Instant.now());
        samplePost.setComments(new ArrayList<>());
        samplePost.setReviews(new ArrayList<>());
    }

    @Test
    @WithMockUser(authorities = "ROLE_AUTHOR")
    void createPost_shouldReturnCreated() throws Exception {
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("Test Title");
        request.setContent("Test Content");
        request.setTags(List.of("tag1"));

        when(postService.createPost(any(CreatePostRequest.class))).thenReturn(samplePost);

        mockMvc.perform(post("/api/v1/posts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("123"))
                .andExpect(jsonPath("$.title").value("Test Title"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_READER")
    void createPost_shouldReturnForbidden_forReader() throws Exception {
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("Test Title");
        request.setContent("Test Content");

        mockMvc.perform(post("/api/v1/posts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ROLE_READER")
    void getPost_shouldReturnPost() throws Exception {
        when(postService.getPostById("123")).thenReturn(samplePost);

        mockMvc.perform(get("/api/v1/posts/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("123"))
                .andExpect(jsonPath("$.title").value("Test Title"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_READER")
    void getAllPosts_shouldReturnPage() throws Exception {
        Page<Post> page = new PageImpl<>(Collections.singletonList(samplePost));
        when(postService.getAllPosts(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("123"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_READER")
    void getAllPosts_withAuthorAndTag_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/posts")
                        .param("author", "user1")
                        .param("tag", "tag1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "ROLE_AUTHOR")
    void updatePost_shouldReturnUpdatedPost() throws Exception {
        UpdatePostRequest request = new UpdatePostRequest();
        request.setTitle("Updated Title");
        request.setContent("Updated Content");
        request.setTags(List.of("tag1", "tag2"));

        Post updatedPost = new Post("Updated Title", "Updated Content", List.of("tag1", "tag2"));
        updatedPost.setPostId("123");
        updatedPost.setAuthor("testUser");

        when(postService.updatePost(eq("123"), any(UpdatePostRequest.class))).thenReturn(updatedPost);

        mockMvc.perform(put("/api/v1/posts/123")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_READER")
    void updatePost_shouldReturnForbidden_forReader() throws Exception {
        UpdatePostRequest request = new UpdatePostRequest();
        request.setTitle("Updated Title");

        mockMvc.perform(put("/api/v1/posts/123")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ROLE_READER")
    void reviewPost_shouldReturnPost() throws Exception {
        ReviewRequest request = new ReviewRequest();
        request.setStars(5);

        when(postService.addReview(eq("123"), any(ReviewRequest.class))).thenReturn(samplePost);

        mockMvc.perform(patch("/api/v1/posts/123")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "ROLE_AUTHOR")
    void deletePost_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/posts/123")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = "ROLE_READER")
    void deletePost_shouldReturnForbidden_forReader() throws Exception {
        mockMvc.perform(delete("/api/v1/posts/123")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}
