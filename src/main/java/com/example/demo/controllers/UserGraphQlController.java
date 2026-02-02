package com.example.demo.controllers;

import com.example.demo.dto.RegisterUserRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.mapper.UserMapper;
import com.example.demo.services.UserService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class UserGraphQlController {
    private final UserService userService;

    public UserGraphQlController(UserService userService) {
        this.userService = userService;
    }

    @QueryMapping
    public UserResponse getUser(@Argument String id) {
        return UserMapper.toResponse(userService.getUserById(id));
    }

    @MutationMapping
    public UserResponse registerUser(@Argument RegisterUserRequest request) {
        return UserMapper.toResponse(userService.registerUser(request));
    }
}
