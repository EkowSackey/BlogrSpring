package com.example.demo.services;

import com.example.demo.domain.Post;
import com.example.demo.domain.Tag;
import com.example.demo.dto.CreatePostRequest;
import com.example.demo.dto.UpdatePostRequest;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repositories.PostRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class PostService {


    private final PostRepository postRepo;

    public PostService(PostRepository postRepo) {
        this.postRepo = postRepo;
    }

    public Post createPost(CreatePostRequest request){
        List<Tag> postTags = new ArrayList<>();
        for (String s: request.getTags()){
            postTags.add(new Tag(s));
        }

        Post post = new Post(request.getTitle(), request.getContent(), postTags);
        post.setDateCreated(Date.from(Instant.now()));
        post.setLastUpdate(Date.from(Instant.now()));
//        todo: replace with userId at security
        post.setAuthorId("author_id");
        postRepo.save(post);
        return post;
    }

    public Post getPostById(String id){
        return postRepo.findPostByPostId(id)
                .orElseThrow(
                        ()-> new ResourceNotFoundException("Post not found with id: " + id)
                );
    }

    public Page<Post> getAllPosts(Pageable pageable) {
        return postRepo.findAll(pageable);
    }

    public Post updatePost(String id, UpdatePostRequest request){
        Post post = getPostById(id);
        List<Tag> tags = new ArrayList<>();

        for (String s : request.getTags()){
            tags.add(new Tag(s));
        }

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setTags(tags);
        post.setLastUpdate(Date.from(Instant.now()));

        return postRepo.save(post);
    }

    public void deletePost(String id){
        postRepo.deleteByPostId(id);
    }

   }
