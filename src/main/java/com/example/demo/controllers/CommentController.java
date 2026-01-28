package com.example.demo.controllers;

import com.example.demo.domain.Comment;
import com.example.demo.services.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(path = "/api/v1/comments", produces = "application/json")
public class CommentController {


    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/")
    public ResponseEntity<Comment> createComment(@Valid @RequestBody Map<String, String> payload){
        return new ResponseEntity<Comment>(commentService.createComment(
                payload.get("commentBody"),
                payload.get("postId")),
                HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public void deleteComment(@PathVariable String id){
        commentService.deleteComment(id);
    }
}
