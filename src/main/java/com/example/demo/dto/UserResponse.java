package com.example.demo.dto;

import com.example.demo.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;
import java.util.List;

@Getter
@AllArgsConstructor
public class UserResponse {
    private String userId;
    private String username;
    private String email;
    private List<Role> roles;
    private Date createdAt;
}
