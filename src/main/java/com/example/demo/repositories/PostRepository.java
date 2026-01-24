package com.example.demo.repositories;

import com.example.demo.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostRepository extends MongoRepository<Post, String> {

    public Optional<Post> findPostByPostId(String postId);
    public Optional<Post> deleteByPostId(String postId);
    Page<Post> findByAuthorId(String authorId, Pageable pageable);
    Page<Post> findByTagSlugsContaining(String tag, Pageable pageable);
}
