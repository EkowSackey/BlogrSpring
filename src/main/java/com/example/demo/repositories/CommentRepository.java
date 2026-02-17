package com.example.demo.repositories;


import com.example.demo.domain.Comment;


public interface CommentRepository{
    Object insert(Comment comment);
    Comment findById(String id);
    void deleteById(String id);
    void deleteAll();
}
