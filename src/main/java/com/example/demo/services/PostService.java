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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.Update;
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
    private final MongoTemplate mongoTemplate;

    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHOR')")
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "post-pages", allEntries = true),
            @CacheEvict(value = {"analytics-posts", "analytics-authors", "analytics-tags"}, allEntries = true, cacheManager = "asyncCacheManager")
    })
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
    @Caching(evict = {
            @CacheEvict(value = "posts", key = "#id"),
            @CacheEvict(value = {"analytics-tags"}, allEntries = true, cacheManager = "asyncCacheManager")
    })
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
    @CacheEvict(value = "analytics-reviews", allEntries = true, cacheManager = "asyncCacheManager")
    public Post addReview(String id, ReviewRequest request){
        // Fetch post to validate existence and author
        Post post = getPostById(id);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String user = authentication.getName();

        if (Objects.equals(post.getAuthor(), user)){
            throw new BadRequestException("Author cannot review their own post");
        }

        Review review = new Review(request.getStars(), user, id);

        // Use atomic update to push review to the array
        // This avoids race conditions where multiple users review simultaneously
        // and overwrite each other's updates
        Query query = new Query(Criteria.where("postId").is(id));
        Update update = new Update().push("reviews", review);
        
        // Find and modify returns the document *before* the update by default,
        // but we want the updated document.
        // Note: findAndModify is atomic.
        Post updatedPost = mongoTemplate.findAndModify(
                query,
                update,
                new org.springframework.data.mongodb.core.FindAndModifyOptions().returnNew(true),
                Post.class
        );
        
        if (updatedPost == null) {
             throw new ResourceNotFoundException("Post not found with id: " + id);
        }

        return updatedPost;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHOR')")
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "posts", key = "#id"),
            @CacheEvict(value = {"analytics-posts", "analytics-authors", "analytics-tags", "analytics-reviews"}, allEntries = true, cacheManager = "asyncCacheManager")
    })
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
