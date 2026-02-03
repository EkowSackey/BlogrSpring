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
public class PostRepositoryImpl implements PostRepository{

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
        Query query = new Query();
        query.with(pageable);

        List<Post> posts = mongoTemplate.find(query, Post.class);
        long total = mongoTemplate.count(new Query(), Post.class);

        return new PageImpl<>(posts, pageable, total);
    }

    @Override
    public Optional<Post> findPostByPostId(String postId) {
        Post post = mongoTemplate.findById(postId, Post.class);
        return Optional.ofNullable(post);
    }

    @Override
    public void deleteByPostId(String postId) {
        Post post = mongoTemplate.findById(postId, Post.class);
        if (post != null)
            mongoTemplate.remove(post);
    }

    @Override
    public void deleteAll() {}

    @Override
    public Page<Post> findByAuthor(String authorUsername, Pageable pageable) {
        Query query = new Query(Criteria.where("author").is(authorUsername));
        List<Post> posts = mongoTemplate.find(query, Post.class);
        long total = mongoTemplate.count(new Query(), Post.class);
        return new PageImpl<>(posts, pageable, total );
    }

    @Override
    public Page<Post> findByTagSlugsContaining(String tag, Pageable pageable) {
        Query query = new Query(Criteria.where("tagSlugs").in(tag));
        query.with(pageable);

        List<Post> posts = mongoTemplate.find(query, Post.class);

        Query countQuery = new Query(Criteria.where("tagSlugs").in(tag));
        long total = mongoTemplate.count(countQuery, Post.class);

        return new PageImpl<>(posts, pageable, total);
    }
}
