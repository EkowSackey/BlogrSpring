package com.example.demo.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document(collection = "posts")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Post{
    @Id
    private String postId;

    @NotBlank
    @Size(min=5, message = "Title must be at least 5 characters long")
    private String title;

    @NotBlank
    @Size(min=5, message = "Content must be at least 5 characters long")
    private String content;

    @CreatedDate
    private Date dateCreated;


    @LastModifiedDate
    private Date lastUpdate;


    private ObjectId authorId;

    @DocumentReference
    private List<Comment> comments;

    private List<Tag> tags;
    private List<Review> reviews;

    public Post(String title, String content, List<Tag> tags){
        this.title = title;
        this.content = content;
        this.tags = new ArrayList<>(tags);
    }

}














