package com.example.demo.repositories;

import com.example.demo.domain.Post;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostRepository extends MongoRepository<Post, String> {

    public Optional<Post> findPostByPostId(String postId);
    public Optional<Post> deleteByPostId(String postId);
}
