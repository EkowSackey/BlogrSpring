package com.example.demo.controllers;

import com.example.demo.domain.Post;
import com.example.demo.dto.CreatePostRequest;
import com.example.demo.dto.UpdatePostRequest;
import com.example.demo.services.JwtService;
import com.example.demo.services.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostController.class)
@AutoConfigureMockMvc(addFilters = false)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService; // Required by JwtAuthFilter during context load

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void createPost_ShouldReturnCreated() throws Exception {
        CreatePostRequest request = new CreatePostRequest("Valid Title", "Valid Content", List.of("tag1"));
        Post post = new Post("Valid Title", "Valid Content", List.of("tag1"));
        post.setPostId("123");

        when(postService.createPost(any(CreatePostRequest.class))).thenReturn(post);

        mockMvc.perform(post("/api/v1/posts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("123"))
                .andExpect(jsonPath("$.title").value("Valid Title"));
    }

    @Test
    void getPost_ShouldReturnPost() throws Exception {
        Post post = new Post("Title", "Content", new ArrayList<>());
        post.setPostId("123");

        when(postService.getPostById("123")).thenReturn(post);

        mockMvc.perform(get("/api/v1/posts/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("123"));
    }

    @Test
    void getAllPosts_ShouldReturnPage() throws Exception {
        Post post = new Post("Title", "Content", new ArrayList<>());
        Page<Post> page = new PageImpl<>(List.of(post));

        when(postService.getAllPosts(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser
    void updatePost_ShouldReturnUpdatedPost() throws Exception {
        UpdatePostRequest request = new UpdatePostRequest();
        request.setTitle("New Title");
        request.setContent("New Content");
        request.setTags(List.of("new"));

        Post updatedPost = new Post("New Title", "New Content", List.of("new"));
        updatedPost.setPostId("123");

        when(postService.updatePost(eq("123"), any(UpdatePostRequest.class))).thenReturn(updatedPost);

        mockMvc.perform(put("/api/v1/posts/123")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Title"));
    }

    @Test
    @WithMockUser
    void deletePost_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/posts/123")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void createPost_WhenInvalidInput_ShouldReturnBadRequest() throws Exception {
        CreatePostRequest request = new CreatePostRequest("sh", "sh", List.of()); // Too short

        mockMvc.perform(post("/api/v1/posts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
