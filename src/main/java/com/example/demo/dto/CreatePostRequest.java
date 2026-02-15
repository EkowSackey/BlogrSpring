package com.example.demo.dto;


import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "Title of the post", example = "My First Post")
    private String title;

    @NotBlank
    @Size(min=5, message = "Content must be at least 5 characters long")
    @Schema(description = "Content of the post", example = "This is the content of my first post")
    private String content;

    @Schema(description = "Tags associated with the post", example = "[\"java\", \"spring\"]")
    private List<String> tags;
}
