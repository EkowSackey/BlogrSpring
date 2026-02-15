package com.example.demo.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticateUserRequest {

    @NotBlank
    @Schema(description = "Username for login", example = "user123")
    private String username;
    @NotBlank
    @Schema(description = "Password for login", example = "password123")
    private String password;
}
