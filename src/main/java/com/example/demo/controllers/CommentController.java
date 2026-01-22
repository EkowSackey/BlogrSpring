package com.example.demo.controllers;

import com.example.demo.domain.Comment;
import com.example.demo.services.CommentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(path = "/api/v1/comments", produces = "application/json")
public class CommentController {

    @Autowired
    private CommentService commentService;

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
