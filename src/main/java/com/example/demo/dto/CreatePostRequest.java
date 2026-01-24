package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class CreatePostRequest {
    @NotBlank
    @Size(min=5, message = "Title must be at least 5 characters long")
    private String title;

    @NotBlank
    @Size(min=5, message = "Content must be at least 5 characters long")
    private String content;

    @NotBlank
    private String authorId;

    private List<String> tags;
}
