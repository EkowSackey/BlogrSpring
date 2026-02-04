package com.example.demo.controllers;

import com.example.demo.domain.Comment;
import com.example.demo.dto.CommentResponse;
import com.example.demo.dto.CreateCommentRequest;
import com.example.demo.services.CommentService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class CommentGraphQlController {

    private final CommentService commentService;

    public CommentGraphQlController(CommentService commentService) {
        this.commentService = commentService;
    }


    @MutationMapping
    public ResponseEntity<CommentResponse> createComment(@Argument CreateCommentRequest request){
        Comment comment =  commentService.createComment(request.getCommentBody(), request.getPostId());
        return new ResponseEntity<>(
                new CommentResponse(comment.getId(), comment.getContent(), comment.getAuthor(), comment.getParentId(), String.valueOf(comment.getCreatedAt())),
                HttpStatus.CREATED
        );
    }

    @MutationMapping
    public Boolean deleteComment(@Argument String id){
        commentService.deleteComment(id);
        return true;
    }
}
