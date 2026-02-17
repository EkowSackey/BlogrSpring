package com.example.demo.services;

import com.example.demo.domain.Comment;
import com.example.demo.domain.Post;
import com.example.demo.repositories.CommentRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService {

    private final CommentRepository commentRepo;
    private final MongoTemplate mongoTemplate;

    public CommentService(CommentRepository commentRepo, MongoTemplate mongoTemplate) {
        this.commentRepo = commentRepo;
        this.mongoTemplate = mongoTemplate;
    }

    @Transactional
    public Comment createComment(String commentBody, String postId){
        Comment comment = new Comment(commentBody);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String author = authentication.getName();

        comment.setAuthor(author);
        comment.setParentId(postId);

        commentRepo.insert(comment);

        mongoTemplate.update(Post.class)
                .matching(Criteria.where("_id").is(postId))
                .apply(new Update().push("comments").value(comment))
                .first();

        return comment;
    }

    public void deleteComment(String id){
        Comment comment = commentRepo.findById(id);
        if (comment != null) {
            String postId = comment.getParentId();
            commentRepo.deleteById(id);
            
            if (postId != null) {
                mongoTemplate.update(Post.class)
                        .matching(Criteria.where("_id").is(postId))
                        .apply(new Update().pull("comments", comment))
                        .first();
            }
        }
    }

}
