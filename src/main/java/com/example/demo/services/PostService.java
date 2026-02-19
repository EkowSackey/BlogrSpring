package com.example.demo.services;

import com.example.demo.domain.Post;
import com.example.demo.domain.Review;
import com.example.demo.dto.CreatePostRequest;
import com.example.demo.dto.ReviewRequest;
import com.example.demo.dto.UpdatePostRequest;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repositories.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {


    private final PostRepository postRepo;

    @CacheEvict(value = "post-pages", allEntries = true)
    public Post createPost(CreatePostRequest request){

        List<String> tags = (request.getTags() == null) ? new ArrayList<>() : request.getTags();

        Post post = new Post(request.getTitle(), request.getContent(), tags);
        post.setDateCreated(Instant.now());
        post.setLastUpdate(Instant.now());

        String authorUsername = getCurrentUsername();
        post.setAuthor(authorUsername);

        postRepo.save(post);
        return post;
    }

    @Cacheable(value = "posts", key = "#id")
    public Post getPostById(String id){
        return postRepo.findPostByPostId(id)
                .orElseThrow(
                        ()-> new ResourceNotFoundException("Post not found with id: " + id)
                );
    }

    @Cacheable(value = "post-pages", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<Post> getAllPosts(Pageable pageable) {
        return postRepo.findAll(pageable);
    }

    public Page<Post> getPostsByAuthor(String authorUsername, Pageable pageable){
        return postRepo.findByAuthor(authorUsername, pageable);
    }

    public Page<Post> getPostsByTag(String tag, Pageable pageable){
        return postRepo.findByTagSlugsContaining(tag, pageable);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "post-pages", allEntries = true)
    }, put = {
            @CachePut(value = "posts", key = "#id")
    })
    public Post updatePost(String id, UpdatePostRequest request){
        Post post = getPostById(id);

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setTagSlugs(request.getTags());
        post.setLastUpdate(Instant.now());

        return postRepo.save(post);
    }

    @Transactional
    public Post addReview(String id, ReviewRequest request){
        Post post = getPostById(id);

        String reviewerUsername = getCurrentUsername();

        if (Objects.equals(post.getAuthor(), reviewerUsername)){
            throw new BadRequestException("Author cannot review their own post");
        }
        List<Review> reviews = post.getReviews();
        if (reviews == null) {
            reviews = new ArrayList<>();
        }

        Review review = new Review(request.getStars(), reviewerUsername, id);
        reviews.add(review);

        post.setReviews(reviews);
        return postRepo.save(post);
    }

    @Caching(evict = {
            @CacheEvict(value = "posts", key = "#id"),
            @CacheEvict(value = "post-pages", allEntries = true)
    })
    public void deletePost(String id){
        postRepo.deleteByPostId(id);
    }

    private String getCurrentUsername() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getName)
                .orElseThrow(() -> new BadRequestException("User must be authenticated"));
    }
   }
