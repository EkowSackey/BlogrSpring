package com.example.demo.domain;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "comments")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Comment{

    @Id
    private String id;

    private String content;

    @NotNull
    private String author;

    private String parentId;

    private Date createdAt = new Date();

    public Comment(String content){
        this.content = content;
    }
}
