package com.example.demo.dto;

import com.example.demo.domain.Comment;
import com.example.demo.domain.Review;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;
import java.util.List;

@Getter
@AllArgsConstructor
public class PostResponse {

    private String id;
    private String title;
    private String content;
    private String author;
    private List<String> tags;
    private List<Comment> comments;
    private List<Review> reviews;
    private Date createdAt;
    private Date lastUpdate;
}

