package com.example.demo.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import com.example.demo.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;
import java.util.List;

@Getter
@AllArgsConstructor
public class UserResponse {
    @Schema(description = "Unique identifier of the user", example = "60f1b2b3c9e9b3001f8e4b1a")
    private String userId;
    @Schema(description = "Username of the user", example = "user123")
    private String username;
    @Schema(description = "Email of the user", example = "user@example.com")
    private String email;
    @Schema(description = "Roles assigned to the user")
    private List<Role> roles;
    @Schema(description = "Creation date of the user account")
    private Date createdAt;
}
