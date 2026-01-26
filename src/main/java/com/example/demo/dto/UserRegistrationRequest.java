package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserRegistrationRequest {
    @NotBlank
    @Size(min = 4)
    private String username;

    @Email
    private String email;

    @NotBlank
    @Size(min = 8)
    private String password;
}
