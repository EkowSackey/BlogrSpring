package com.example.demo.controllers;

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
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping()
    public ResponseEntity<PostResponse> createPost(
            @Valid @RequestBody CreatePostRequest request
    ){
        Post post = postService.createPost(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PostMapper.toResponse(post));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPost(@PathVariable String id){
        Post post = postService.getPostById(id);
        return ResponseEntity.ok(PostMapper.toResponse(post));
    }

    @GetMapping
    public ResponseEntity<Page<PostResponse>> getAllPosts(
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String tag,
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

    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable String id,
            @Valid @RequestBody UpdatePostRequest request
    ) {
        Post updated = postService.updatePost(id, request);
        return ResponseEntity.ok(PostMapper.toResponse(updated));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PostResponse> reviewPost(@RequestBody ReviewRequest request, @PathVariable String id){
        Post post= postService.addReview(id, request);
        return ResponseEntity.ok(PostMapper.toResponse(post));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable String id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
}
