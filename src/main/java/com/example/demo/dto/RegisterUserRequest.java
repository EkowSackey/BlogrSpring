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
public class RegisterUserRequest {

    @NotBlank
    @Schema(description = "Username for registration", example = "newuser")
    private String username;
    @NotBlank
    @Schema(description = "Email address for registration", example = "newuser@example.com")
    private String email;
    @NotBlank
    @Schema(description = "Password for registration", example = "password123")
    private String password;
}
