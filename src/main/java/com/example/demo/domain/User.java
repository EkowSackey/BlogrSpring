package com.example.demo.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document(collection = "users")
@NoArgsConstructor
public class User{

    @Id
    private String userId;

    @Indexed(unique = true)
    private String username;

    @Indexed(unique = true)
    private String email;

    private String password;
    private String role;
    private Date createdAt;

    public User(String username, String email, String password){
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = String.valueOf(Role.REGULAR);
        this.createdAt = new Date();
    }
}