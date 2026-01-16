package com.example.demo.domain;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class Review{
        @NotNull
        double stars;

        @NotNull
        ObjectId userId;

        @NotNull
        ObjectId postId;
}

