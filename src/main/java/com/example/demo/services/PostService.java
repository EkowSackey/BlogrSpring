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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authorUsername = authentication.getName();
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

    @Cacheable(value = "posts", key = "#tag")
    public Page<Post> getPostsByTag(String tag, Pageable pageable){
        return postRepo.findByTagSlugsContaining(tag, pageable);
    }

    @Transactional
    @CachePut(value = "posts", key = "#id")
    public Post updatePost(String id, UpdatePostRequest request){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUser = authentication.getName();

        Post post = getPostById(id);

        if (!Objects.equals(post.getAuthor(), currentUser)){
            throw new BadRequestException("You cannot edit another author's post");
        }

        post.setTitle(request.getTitle() + "(Edit)");
        post.setContent(request.getContent() + "This post was last edited at: " + Date.from(Instant.now()));
        post.setTagSlugs(request.getTags());
        post.setLastUpdate(Date.from(Instant.now()));

        return postRepo.save(post);
    }

    @CacheEvict(value = "posts", key = "#id")
    public Post addReview(String id, ReviewRequest request){
        Post post = getPostById(id);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String user = authentication.getName();

        if (Objects.equals(post.getAuthor(), user)){
            throw new BadRequestException("Author cannot review their own post");
        }
        List<Review> reviews = post.getReviews();

        Review review = new Review(request.getStars(), user, id);
        reviews.add(review);

        int size = reviews.size();
        double sum = 0.0;

        for (Review r: reviews){
            sum += r.getStars();
        }

        post.setAvgRating(sum / size);

        post.setReviews(reviews);
        return postRepo.save(post);
    }

    @CacheEvict(value = "posts", key = "#id")
    public void deletePost(String id){
        postRepo.deleteByPostId(id);
    }
   }
