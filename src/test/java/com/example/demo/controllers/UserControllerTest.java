package com.example.demo.controllers;

import com.example.demo.config.OAuth2LoginSuccessHandler;
import com.example.demo.config.SecurityConfig;
import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.dto.AuthenticateUserRequest;
import com.example.demo.dto.RegisterUserRequest;
import com.example.demo.filter.JwtAuthFilter;
import com.example.demo.services.CustomOAuth2UserService;
import com.example.demo.services.JwtService;
import com.example.demo.services.TokenBlacklistService;
import com.example.demo.services.UserService;
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
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

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

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private User sampleUser;

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

        sampleUser = new User();
        sampleUser.setUserId("user123");
        sampleUser.setUsername("testuser");
        sampleUser.setEmail("test@example.com");
        sampleUser.setRoles(List.of(Role.READER));
        sampleUser.setCreatedAt(Instant.now());
    }

    @Test
    void register_shouldReturnCreated() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(userService.registerUser(any(RegisterUserRequest.class))).thenReturn(sampleUser);

        mockMvc.perform(post("/api/v1/users/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value("user123"))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void login_shouldReturnToken() throws Exception {
        AuthenticateUserRequest request = new AuthenticateUserRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        when(userService.authenticateUser(any(AuthenticateUserRequest.class))).thenReturn("mock-jwt-token");

        mockMvc.perform(post("/api/v1/users/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("mock-jwt-token"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void getAllUsers_shouldReturnPageForAdmin() throws Exception {
        Page<User> page = new PageImpl<>(Collections.singletonList(sampleUser));
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].userId").value("user123"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_READER")
    void getAllUsers_shouldReturnForbiddenForUser() throws Exception {

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void getUser_shouldReturnUser() throws Exception {

        when(userService.getUserById("user123")).thenReturn(sampleUser);

        mockMvc.perform(get("/api/v1/users/user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("user123"));
    }
}
