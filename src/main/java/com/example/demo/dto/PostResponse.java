package com.example.demo.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import com.example.demo.domain.Comment;
import com.example.demo.domain.Review;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;
import java.util.List;

@Getter
@AllArgsConstructor
public class PostResponse {

    @Schema(description = "Unique identifier of the post", example = "60f1b2b3c9e9b3001f8e4b1a")
    private String id;
    @Schema(description = "Title of the post", example = "My First Post")
    private String title;
    @Schema(description = "Content of the post", example = "This is the content of my first post")
    private String content;
    @Schema(description = "Author of the post", example = "user123")
    private String author;
    @Schema(description = "Tags associated with the post", example = "[\"java\", \"spring\"]")
    private List<String> tags;
    @Schema(description = "Comments on the post")
    private List<Comment> comments;
    @Schema(description = "Reviews of the post")
    private List<Review> reviews;
    @Schema(description = "Creation date of the post")
    private Date createdAt;
    @Schema(description = "Last update date of the post")
    private Date lastUpdate;
}

