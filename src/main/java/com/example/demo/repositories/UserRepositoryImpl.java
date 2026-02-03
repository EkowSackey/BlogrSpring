package com.example.demo.repositories;

import com.example.demo.domain.User;
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
public class UserRepositoryImpl implements UserRepository {

    private final MongoTemplate mongoTemplate;

    public UserRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public User save(User user) {
        return mongoTemplate.save(user);
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        Query query = new Query();
        query.with(pageable);

        List<User> users = mongoTemplate.find(query, User.class);
        long total = mongoTemplate.count(new Query(), User.class);

        return new PageImpl<>(users, pageable, total);
    }

    @Override
    public Optional<User> findById(String id) {
        User user = mongoTemplate.findById(id, User.class);
        return Optional.ofNullable(user);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        Query query = new Query(Criteria.where("username").is(username));
        User user = mongoTemplate.findOne(query, User.class);
        return Optional.ofNullable(user);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        Query query = new Query(Criteria.where("email").is(email));
        User user = mongoTemplate.findOne(query, User.class);
        return Optional.ofNullable(user);
    }

    @Override
    public boolean existsByUsername(String username) {
        Query query = new Query(Criteria.where("username").is(username));
        return mongoTemplate.exists(query, User.class);
    }

    @Override
    public boolean existsByEmail(String email) {
        Query query = new Query(Criteria.where("email").is(email));
        return mongoTemplate.exists(query, User.class);
    }
}
