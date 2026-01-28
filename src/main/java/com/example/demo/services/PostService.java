package com.example.demo.services;

import com.example.demo.domain.Post;
import com.example.demo.domain.Review;
import com.example.demo.dto.CreatePostRequest;
import com.example.demo.dto.ReviewRequest;
import com.example.demo.dto.UpdatePostRequest;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repositories.PostRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;


@Slf4j
@Service
public class PostService {


    private final PostRepository postRepo;

    public PostService(PostRepository postRepo) {
        this.postRepo = postRepo;
    }

    @CacheEvict(value = "post-pages", allEntries = true)
    public Post createPost(CreatePostRequest request){

        List<String> tags = (request.getTags() == null) ? new ArrayList<>() : request.getTags();

        Post post = new Post(request.getTitle(), request.getContent(), tags);
        post.setDateCreated(Date.from(Instant.now()));
        post.setLastUpdate(Date.from(Instant.now()));
//        todo: replace with userId at security
        post.setAuthorId("author_id");
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

    public Page<Post> getPostsByAuthor(String authorId, Pageable pageable){
        return postRepo.findByAuthorId(authorId, pageable);
    }

    public Page<Post> getPostsByTag(String tag, Pageable pageable){
        return postRepo.findByTagSlugsContaining(tag, pageable);
    }

    @Transactional
    @CachePut(value = "posts", key = "#id")
    public Post updatePost(String id, UpdatePostRequest request){
        Post post = getPostById(id);

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setTagSlugs(request.getTags());
        post.setLastUpdate(Date.from(Instant.now()));

        return postRepo.save(post);
    }

    public Post addReview(String id, ReviewRequest request){
        Post post = getPostById(id);

        if (Objects.equals(post.getAuthorId(), request.getUserId())){
            throw new BadRequestException("Author cannot review their own post");
        }
        List<Review> reviews = post.getReviews();

        Review review = new Review(request.getStars(), request.getUserId(), request.getPostId());
        reviews.add(review);

        post.setReviews(reviews);
        return postRepo.save(post);
    }

    @CacheEvict(value = "posts", key = "#id")
    public void deletePost(String id){
        postRepo.deleteByPostId(id);
    }
   }
