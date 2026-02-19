package com.example.demo.domain;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "comments")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Comment{

    @Id
    @Schema(description = "Unique identifier of the comment", example = "60f1b2b3c9e9b3001f8e4b1b")
    private String id;

    @Schema(description = "Content of the comment", example = "Great post!")
    private String content;

    @NotNull
    @Schema(description = "Author of the comment", example = "user123")
    private String author;

    @Schema(description = "ID of the parent comment (if reply)", example = "60f1b2b3c9e9b3001f8e4b1a")
    private String parentId;

    @Schema(description = "Creation date of the comment")
    private Instant createdAt = Instant.now();

    public Comment(String content){
        this.content = content;
    }
}
