package com.example.demo.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;

import java.util.Date;

@Data

public class Comment{

    private ObjectId commentId;

    @NotBlank(message = "Content is required")
    private String content;

    @NotNull
    private ObjectId authorId;

    @NotNull
    private ObjectId parentId;

    @CreatedDate
    private Date createdAt = new Date();
}
