package com.example.demo.repositories;

import com.example.demo.domain.Post;
import com.example.demo.dto.PostSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostRepository extends MongoRepository<Post, String> {

    Optional<Post> findPostByPostId(String postId);
    void deleteByPostId(String postId);
    Page<Post> findByAuthor(String authorUsername, Pageable pageable);
    Page<Post> findByTagSlugsContaining(String tag, Pageable pageable);
    
    @Query("{ 'author' : ?0, 'reviews.stars' : { $gte : ?1 } }")
    Page<Post> findPostsByAuthorAndMinStars(String author, int minStars, Pageable pageable);

    @Query(value = "{}", fields = "{ 'content' : 0, 'reviews' : 0 }")
    Page<PostSummary> findAllProjectedBy(Pageable pageable);

    Page<Post> findAllBy(TextCriteria criteria, Pageable pageable);
}
