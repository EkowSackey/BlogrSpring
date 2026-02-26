package com.example.demo.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.example.demo.domain.Comment;
import com.example.demo.dto.CreateCommentRequest;
import com.example.demo.services.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/comments", produces = "application/json")
@Tag(name = "Comments", description = "API for managing comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @Operation(summary = "Create a new comment", description = "Creates a new comment for a post")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Comment created successfully",
                    content = @Content(schema = @Schema(implementation = Comment.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping("/")
    public ResponseEntity<Comment> createComment(@Valid @RequestBody CreateCommentRequest request){
        return new ResponseEntity<Comment>(commentService.createComment(
                request.getCommentBody(),
                request.getPostId()),
                HttpStatus.CREATED);
    }

    @Operation(summary = "Delete a comment", description = "Deletes a comment by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comment deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    @DeleteMapping("/{id}")
    public void deleteComment(@Parameter(description = "ID of the comment to be deleted") @PathVariable String id){
        commentService.deleteComment(id);
    }
}
