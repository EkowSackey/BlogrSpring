package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;

@Getter
@AllArgsConstructor
public class UserResponse {
    private String userId;
    private String username;
    private String email;
    private String role;
    private Date createdAt;
}
