package com.example.demo.controllers;

import com.example.demo.domain.Post;
import com.example.demo.domain.User;
import com.example.demo.dto.PostResponse;
import com.example.demo.dto.UserRegistrationRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.mapper.PostMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.services.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRegistrationRequest request){
        User user = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserMapper.toResponse(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getPost(@PathVariable String id){
        User user = userService.getUserById(id);
        return ResponseEntity.ok(UserMapper.toResponse(user));
    }
}
