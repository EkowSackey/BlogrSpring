package com.example.demo.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.example.demo.domain.Post;
import com.example.demo.dto.CreatePostRequest;
import com.example.demo.dto.PostResponse;
import com.example.demo.dto.ReviewRequest;
import com.example.demo.dto.UpdatePostRequest;
import com.example.demo.exception.BadRequestException;
import com.example.demo.mapper.PostMapper;
import com.example.demo.services.PostService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(path = "/api/v1/posts", produces = "application/json")
@Tag(name = "Posts", description = "API for managing posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @Operation(summary = "Create a new post", description = "Creates a new post with the given details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Post created successfully",
                    content = @Content(schema = @Schema(implementation = PostResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping()
    public ResponseEntity<PostResponse> createPost(
            @Valid @RequestBody CreatePostRequest request
    ){
        Post post = postService.createPost(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PostMapper.toResponse(post));
    }

    @Operation(summary = "Get a post by ID", description = "Retrieves a post by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Post found",
                    content = @Content(schema = @Schema(implementation = PostResponse.class))),
            @ApiResponse(responseCode = "404", description = "Post not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPost(@Parameter(description = "ID of the post to be retrieved") @PathVariable String id){
        Post post = postService.getPostById(id);
        return ResponseEntity.ok(PostMapper.toResponse(post));
    }

    @Operation(summary = "Get all posts", description = "Retrieves a paginated list of posts, optionally filtered by author or tag")
    @GetMapping
    public ResponseEntity<Page<PostResponse>> getAllPosts(
            @Parameter(description = "Filter posts by author") @RequestParam(required = false) String author,
            @Parameter(description = "Filter posts by tag") @RequestParam(required = false) String tag,
            @PageableDefault(size = 10, sort = "dateCreated", direction = Sort.Direction.DESC)
            @ParameterObject Pageable pageable
    ) {

        if (author != null && tag !=null){
            throw new BadRequestException("Cannot filter by author and tag at the same time");
        }

        if (author != null){
            Page<PostResponse> response = postService.getPostsByAuthor(author, pageable).map(PostMapper::toResponse);
            return ResponseEntity.ok(response);
        }
        if (tag != null){
            Page<PostResponse> response = postService.getPostsByTag(tag, pageable).map(PostMapper::toResponse);
            return ResponseEntity.ok(response);
        }
        Page<PostResponse> response =postService.getAllPosts(pageable)
                .map(PostMapper::toResponse);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update a post", description = "Updates an existing post by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Post updated successfully",
                    content = @Content(schema = @Schema(implementation = PostResponse.class))),
            @ApiResponse(responseCode = "404", description = "Post not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(
            @Parameter(description = "ID of the post to be updated") @PathVariable String id,
            @Valid @RequestBody UpdatePostRequest request
    ) {
        Post updated = postService.updatePost(id, request);
        return ResponseEntity.ok(PostMapper.toResponse(updated));
    }

    @Operation(summary = "Review a post", description = "Adds a review to a post")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Review added successfully",
                    content = @Content(schema = @Schema(implementation = PostResponse.class))),
            @ApiResponse(responseCode = "404", description = "Post not found")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<PostResponse> reviewPost( @Valid ReviewRequest request, @Parameter(description = "ID of the post to be reviewed") @PathVariable String id){
        Post post= postService.addReview(id, request);
        return ResponseEntity.ok(PostMapper.toResponse(post));
    }

    @Operation(summary = "Delete a post", description = "Deletes a post by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Post deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Post not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@Parameter(description = "ID of the post to be deleted") @PathVariable String id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
}
