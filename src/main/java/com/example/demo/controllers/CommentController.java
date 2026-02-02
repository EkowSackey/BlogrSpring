package com.example.demo.controllers;

import com.example.demo.domain.Comment;
import com.example.demo.dto.CreateCommentRequest;
import com.example.demo.services.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/comments", produces = "application/json")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/")
    public ResponseEntity<Comment> createComment(@Valid @RequestBody CreateCommentRequest request){
        return new ResponseEntity<Comment>(commentService.createComment(
                request.getCommentBody(),
                request.getPostId()),
                HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public void deleteComment(@PathVariable String id){
        commentService.deleteComment(id);
    }
}
