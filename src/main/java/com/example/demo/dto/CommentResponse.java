package com.example.demo.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommentResponse {

    @Schema(description = "Unique identifier of the comment", example = "60f1b2b3c9e9b3001f8e4b1b")
    private String id;
    @Schema(description = "Content of the comment", example = "Great post!")
    private String content;
    @Schema(description = "ID of the author", example = "user123")
    private String authorId;
    @Schema(description = "ID of the parent comment (if reply)", example = "60f1b2b3c9e9b3001f8e4b1a")
    private String parentId;
    @Schema(description = "Creation date of the comment")
    private String createdAt;
}
