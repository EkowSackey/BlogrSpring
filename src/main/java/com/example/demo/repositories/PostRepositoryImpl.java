package com.example.demo.repositories;

import com.example.demo.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PostRepositoryImpl implements PostRepository {

    private final MongoTemplate mongoTemplate;

    public PostRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Post save(Post p) {
        return mongoTemplate.save(p);
    }

    @Override
    public Page<Post> findAll(Pageable pageable) {
        Query query = new Query().with(pageable);
        List<Post> posts = mongoTemplate.find(query, Post.class);
        long total = mongoTemplate.count(new Query(), Post.class);
        return new PageImpl<>(posts, pageable, total);
    }

    @Override
    public Optional<Post> findPostByPostId(String postId) {
        return Optional.ofNullable(mongoTemplate.findById(postId, Post.class));
    }

    @Override
    public void deleteByPostId(String postId) {
        Query query = new Query(Criteria.where("_id").is(postId));
        mongoTemplate.remove(query, Post.class);
    }

    @Override
    public void deleteAll() {
        mongoTemplate.remove(new Query(), Post.class);
    }

    @Override
    public Page<Post> findByAuthor(String authorUsername, Pageable pageable) {
        Query query = new Query(Criteria.where("author").is(authorUsername)).with(pageable);
        List<Post> posts = mongoTemplate.find(query, Post.class);
        long total = mongoTemplate.count(new Query(Criteria.where("author").is(authorUsername)), Post.class);
        return new PageImpl<>(posts, pageable, total);
    }

    @Override
    public Page<Post> findByTagSlugsContaining(String tag, Pageable pageable) {
        Query query = new Query(Criteria.where("tagSlugs").in(tag)).with(pageable);
        List<Post> posts = mongoTemplate.find(query, Post.class);
        long total = mongoTemplate.count(new Query(Criteria.where("tagSlugs").in(tag)), Post.class);
        return new PageImpl<>(posts, pageable, total);
    }
}
