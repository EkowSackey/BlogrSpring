package com.example.demo.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class UpdatePostRequest {

    @NotBlank
    @Schema(description = "Title of the post", example = "Updated Title")
    private String title;

    @NotBlank
    @Schema(description = "Content of the post", example = "Updated content")
    private String content;

    @NotNull
    @Schema(description = "Tags associated with the post", example = "[\"java\", \"spring\", \"update\"]")
    private List<String> tags;
}
