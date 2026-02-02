package com.example.demo.services;

import com.example.demo.domain.Comment;
import com.example.demo.domain.Post;
import com.example.demo.repositories.CommentRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class CommentService {


    private final CommentRepository commentRepo;

    @Autowired
    private MongoTemplate mongoTemplate;

    public CommentService(CommentRepository commentRepo) {
        this.commentRepo = commentRepo;
    }

    public Comment createComment(String commentBody, String postId){
        Comment comment = new Comment(commentBody);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String author = authentication.getName();

        comment.setAuthor(author);
        comment.setParentId(postId);

        commentRepo.insert(comment);

        mongoTemplate.update(Post.class)
                .matching(Criteria.where("postId").is(postId))
                .apply(new Update().push("comments").value(comment))
                .first();

        return comment;
    }

    public void deleteComment(String id){
        commentRepo.deleteById(id);
    }

}
