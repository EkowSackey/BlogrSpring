package com.example.demo.controllers;

import com.example.demo.domain.Comment;
import com.example.demo.dto.CreateCommentRequest;
import com.example.demo.services.CommentService;
import com.example.demo.services.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
@AutoConfigureMockMvc(addFilters = false)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommentService commentService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createComment_ShouldReturnCreated() throws Exception {
        CreateCommentRequest request = new CreateCommentRequest("Great post!", "post123");
        Comment comment = new Comment("Great post!");
        comment.setId("comment123");

        when(commentService.createComment(anyString(), anyString())).thenReturn(comment);

        mockMvc.perform(post("/api/v1/comments/")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("comment123"))
                .andExpect(jsonPath("$.content").value("Great post!"));
    }

    @Test
    void deleteComment_ShouldReturnOk() throws Exception {
        mockMvc.perform(delete("/api/v1/comments/comment123")
                        .with(csrf()))
                .andExpect(status().isOk());
    }
}
