package com.example.demo.repositories;

import com.example.demo.domain.Comment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class CommentRepositoryImpl implements CommentRepository {

    private final MongoTemplate mongoTemplate;

    public CommentRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Comment insert(Comment comment) {
        return mongoTemplate.insert(comment);
    }

    @Override
    public Comment findById(String id) {
        return mongoTemplate.findById(id, Comment.class);
    }

    @Override
    public void deleteById(String id) {
        Query query = new Query(Criteria.where("_id").is(id));
        mongoTemplate.remove(query, Comment.class);
    }

    @Override
    public void deleteAll() {}
}
