package com.example.demo.services;

import com.example.demo.domain.Post;
import com.example.demo.domain.Review;
import com.example.demo.dto.CreatePostRequest;
import com.example.demo.dto.PostSummary;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {


    private final PostRepository postRepo;

    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHOR')")
    @Transactional
    @CacheEvict(value = "post-pages", allEntries = true)
    public Post createPost(CreatePostRequest request){

        List<String> tags = (request.getTags() == null) ? new ArrayList<>() : request.getTags();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authorUsername = authentication.getName();

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .tagSlugs(tags)
                .author(authorUsername)
                .dateCreated(Instant.now())
                .lastUpdate(Instant.now())
                .reviews(new ArrayList<>())
                .build();

        postRepo.save(post);
        return post;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHOR', 'READER')")
    @Transactional(readOnly = true)
    @Cacheable(value = "posts", key = "#id")
    public Post getPostById(String id){
        return postRepo.findPostByPostId(id)
                .orElseThrow(
                        ()-> new ResourceNotFoundException("Post not found with id: " + id)
                );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHOR', 'READER')")
    @Transactional(readOnly = true)
    @Cacheable(value = "post-pages", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<Post> getAllPosts(Pageable pageable) {
        return postRepo.findAll(pageable);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHOR', 'READER')")
    @Transactional(readOnly = true)
    public Page<Post> getPostsByAuthor(String authorUsername, Pageable pageable){
        return postRepo.findByAuthor(authorUsername, pageable);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHOR', 'READER')")
    @Transactional(readOnly = true)
    public Page<Post> getPostsByTag(String tag, Pageable pageable){
        TextCriteria criteria = TextCriteria.forDefaultLanguage().matchingAny(tag);
        return postRepo.findAllBy(criteria, pageable);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHOR', 'READER')")
    @Transactional(readOnly = true)
    public Page<Post> getPostsByAuthorAndMinStars(String author, int minStars, Pageable pageable) {
        return postRepo.findPostsByAuthorAndMinStars(author, minStars, pageable);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHOR', 'READER')")
    @Transactional(readOnly = true)
    public Page<PostSummary> getPostSummaries(Pageable pageable){
        return postRepo.findAllProjectedBy(pageable);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHOR')")
    @Transactional
    @CachePut(value = "posts", key = "#id")
    public Post updatePost(String id, UpdatePostRequest request){
        Post post = getPostById(id);
        validatePostOwnership(post);

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setTagSlugs(request.getTags());
        post.setLastUpdate(Instant.now());

        return postRepo.save(post);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHOR', 'READER')")
    @Transactional
    public Post addReview(String id, ReviewRequest request){
        Post post = getPostById(id);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String user = authentication.getName();

        if (Objects.equals(post.getAuthor(), user)){
            throw new BadRequestException("Author cannot review their own post");
        }
        
        if (post.getReviews() == null) {
            post.setReviews(new ArrayList<>());
        }

        List<Review> reviews = post.getReviews();

        Review review = new Review(request.getStars(), user, id);
        reviews.add(review);

        post.setReviews(reviews);
        return postRepo.save(post);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHOR')")
    @Transactional
    @CacheEvict(value = "posts", key = "#id")
    public void deletePost(String id){
        Post post = getPostById(id);
        validatePostOwnership(post);
        postRepo.deleteByPostId(id);
    }

    private void validatePostOwnership(Post post) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        
        if (!Objects.equals(post.getAuthor(), currentUsername)) {
            log.warn("User {} attempted to modify post {} owned by {}", currentUsername, post.getPostId(), post.getAuthor());
            throw new AccessDeniedException("You do not have permission to modify this post");
        }
    }

}
