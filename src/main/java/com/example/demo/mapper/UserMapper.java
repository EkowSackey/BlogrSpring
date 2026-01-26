package com.example.demo.mapper;

import com.example.demo.domain.User;
import com.example.demo.dto.UserResponse;

public class UserMapper {
    public static UserResponse toResponse(User user) {
        if (user == null) return null;

        return new UserResponse(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}