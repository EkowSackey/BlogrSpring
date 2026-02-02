package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommentResponse {

    private String id;
    private String content;
    private String authorId;
    private String parentId;
    private String createdAt;
}
