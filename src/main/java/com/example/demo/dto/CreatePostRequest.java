package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostRequest {
    @NotBlank
    @Size(min=5, message = "Title must be at least 5 characters long")
    private String title;

    @NotBlank
    @Size(min=5, message = "Content must be at least 5 characters long")
    private String content;

    private List<String> tags;
}
