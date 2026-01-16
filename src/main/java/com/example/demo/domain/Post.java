package com.example.demo.domain;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Data
@Document(collation = "posts")
@AllArgsConstructor
@NoArgsConstructor(access= AccessLevel.PRIVATE, force=true)
public class Post{
    @Id
    private String postId;

    @NotNull
    @Size(min=5, message = "Title must be at least 5 characters long")
    private String title;

    @NotNull
    @Size(min=5, message = "Content must be at least 5 characters long")
    private String content;

    @NotNull
    @CreatedDate
    private Date dateCreated;

    @NotNull
    @LastModifiedDate
    private Date lastUpdate;

    @NotNull
    private ObjectId authorId;

    private List<Comment> comments;
    private int commentCount;
    private List<Tag> tags;
    private List<Review> reviews;
    private double avgRating;
}

