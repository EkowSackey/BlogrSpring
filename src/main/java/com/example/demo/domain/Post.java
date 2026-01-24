package com.example.demo.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
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

    private String title;

    private String content;

    private Date dateCreated;

    private Date lastUpdate;

    private String authorId;

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














