package com.example.demo.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateCommentRequest {

    @NotBlank
    @Schema(description = "Content of the comment", example = "Great post!")
    private String commentBody;

    @NotBlank
    @Schema(description = "ID of the post to comment on", example = "60f1b2b3c9e9b3001f8e4b1a")
    private String postId;
}
