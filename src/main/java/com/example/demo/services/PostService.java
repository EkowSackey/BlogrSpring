package com.example.demo.services;

import com.example.demo.domain.Post;
import com.example.demo.dto.CreatePostRequest;
import com.example.demo.dto.UpdatePostRequest;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repositories.PostRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;


@Slf4j
@Service
public class PostService {


    private final PostRepository postRepo;

    public PostService(PostRepository postRepo) {
        this.postRepo = postRepo;
    }

    public Post createPost(CreatePostRequest request){

        Post post = new Post(request.getTitle(), request.getContent(), request.getTags());
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

    public Page<Post> getPostsByAuthor(String authorId, Pageable pageable){
        return postRepo.findByAuthorId(authorId, pageable);
    }

    public Page<Post> getPostsByTag(String tag, Pageable pageable){
        return postRepo.findByTagSlugsContaining(tag, pageable);
    }

    public Post updatePost(String id, UpdatePostRequest request){
        Post post = getPostById(id);

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setTagSlugs(request.getTags());
        post.setLastUpdate(Date.from(Instant.now()));

        return postRepo.save(post);
    }

    public void deletePost(String id){
        postRepo.deleteByPostId(id);
    }

   }
