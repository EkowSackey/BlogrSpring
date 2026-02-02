package com.example.demo.repositories;

import com.example.demo.domain.Comment;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository{
    Object insert(Comment comment);
    Comment findById(String id);
    void deleteById(String id);
    void deleteAll();
}
