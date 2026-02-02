package com.example.demo.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
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

    @NotBlank(message = "Content is required")
    private String content;

    @NotNull
    private String author;

    @NotNull
    private String parentId;

    @CreatedDate
    private Date createdAt = new Date();

    public Comment(String content){
        this.content = content;
    }
}
