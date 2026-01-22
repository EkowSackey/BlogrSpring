package com.example.demo.controllers;

import com.example.demo.domain.Post;
import com.example.demo.services.PostService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;


@Slf4j
@RestController
@RequestMapping(path = "/api/v1/posts", produces = "application/json")
public class PostController {

    @Autowired
    private PostService postService;

    @GetMapping("/")
    public ResponseEntity<?> getAllPosts(){
        var posts = postService.allPosts();

        if (posts.isEmpty()){
            return new ResponseEntity<String>("No Posts available.", HttpStatus.OK);
        }
        return new ResponseEntity<List<Post>>(postService.allPosts(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<Post>> getPost(@PathVariable String id){
        return new ResponseEntity<Optional<Post>>( postService.singlePost(id), HttpStatus.OK);
    }

    @PostMapping("/")
    public ResponseEntity<?> createPost(@Valid @RequestBody Map<String, Object> payload){
        String title = String.valueOf(payload.get("title"));
        String content = String.valueOf(payload.get("content"));
        List<String> tags = (List<String>) payload.get("tags");

        Post createdPost = postService.createPost(title, content, tags);
        return new ResponseEntity<>(createdPost, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable String id){
        Optional<Post> deletedPost = postService.deletePost(id);
        return new ResponseEntity<>(deletedPost, HttpStatus.OK);    }

}
