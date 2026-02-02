package com.example.demo.repositories;

import com.example.demo.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostRepository{

    Post save(Post p);
    Page<Post> findAll(Pageable pageable);
    Optional<Post> findPostByPostId(String postId);
    void deleteByPostId(String postId);
    void deleteAll();
    Page<Post> findByAuthor(String authorUsername, Pageable pageable);
    Page<Post> findByTagSlugsContaining(String tag, Pageable pageable);
}
