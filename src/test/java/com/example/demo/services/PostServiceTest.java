package com.example.demo.services;

import com.example.demo.domain.Post;
import com.example.demo.dto.CreatePostRequest;
import com.example.demo.dto.ReviewRequest;
import com.example.demo.dto.UpdatePostRequest;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repositories.PostRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.UpdateDefinition;
import org.mockito.ArgumentCaptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepo;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private PostService postService;

    @BeforeEach
    void setUp() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "testuser",
                null,
                List.of()
        );
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createPost_shouldCreatePostWithAuthenticatedUser() {

        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("Test Title");
        request.setContent("Test Content");
        request.setTags(Arrays.asList("tag1", "tag2"));

        Post savedPost = new Post("Test Title", "Test Content", Arrays.asList("tag1", "tag2"));
        savedPost.setAuthor("testuser");
        when(postRepo.save(any(Post.class))).thenReturn(savedPost);


        Post result = postService.createPost(request);

        assertNotNull(result);
        assertEquals("Test Title", result.getTitle());
        assertEquals("Test Content", result.getContent());
        assertEquals("testuser", result.getAuthor());
        assertEquals(2, result.getTagSlugs().size());
        assertNotNull(result.getDateCreated());
        assertNotNull(result.getLastUpdate());

        verify(postRepo, times(1)).save(any(Post.class));
    }

    @Test
    void createPost_shouldHandleNullTags() {

        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("Test Title");
        request.setContent("Test Content");
        request.setTags(null);

        when(postRepo.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));


        Post result = postService.createPost(request);

        assertNotNull(result);
        assertTrue(result.getTagSlugs().isEmpty());
        verify(postRepo, times(1)).save(any(Post.class));
    }

    @Test
    void getPostById_shouldReturnPost_whenPostExists() {

        String postId = "post123";
        Post post = new Post("Title", "Content", List.of());
        post.setPostId(postId);

        when(postRepo.findPostByPostId(postId)).thenReturn(Optional.of(post));


        Post result = postService.getPostById(postId);

        assertNotNull(result);
        assertEquals(postId, result.getPostId());
        assertEquals("Title", result.getTitle());
        verify(postRepo, times(1)).findPostByPostId(postId);
    }

    @Test
    void getPostById_shouldThrowException_whenPostNotFound() {

        String postId = "nonexistent";
        when(postRepo.findPostByPostId(postId)).thenReturn(Optional.empty());


        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> postService.getPostById(postId)
        );

        assertEquals("Post not found with id: " + postId, exception.getMessage());
        verify(postRepo, times(1)).findPostByPostId(postId);
    }

    @Test
    void getAllPosts_shouldReturnPageOfPosts() {

        Pageable pageable = PageRequest.of(0, 10);
        List<Post> posts = Arrays.asList(
                new Post("Title1", "Content1", List.of()),
                new Post("Title2", "Content2", List.of())
        );
        Page<Post> postPage = new PageImpl<>(posts, pageable, posts.size());

        when(postRepo.findAll(pageable)).thenReturn(postPage);


        Page<Post> result = postService.getAllPosts(pageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalElements());
        verify(postRepo, times(1)).findAll(pageable);
    }

    @Test
    void getPostsByAuthor_shouldReturnPostsByAuthor() {

        String author = "testuser";
        Pageable pageable = PageRequest.of(0, 10);
        List<Post> posts = List.of(
                new Post("Title1", "Content1", List.of())
        );
        Page<Post> postPage = new PageImpl<>(posts, pageable, posts.size());

        when(postRepo.findByAuthor(author, pageable)).thenReturn(postPage);


        Page<Post> result = postService.getPostsByAuthor(author, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(postRepo, times(1)).findByAuthor(author, pageable);
    }

    @Test
    void getPostsByTag_shouldReturnPostsByTag() {

        String tag = "java";
        Pageable pageable = PageRequest.of(0, 10);
        List<Post> posts = List.of(
                new Post("Title1", "Content1", Arrays.asList("java", "spring"))
        );
        Page<Post> postPage = new PageImpl<>(posts, pageable, posts.size());

        when(postRepo.findAllBy(any(TextCriteria.class), eq(pageable))).thenReturn(postPage);


        Page<Post> result = postService.getPostsByTag(tag, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        
        ArgumentCaptor<TextCriteria> captor = ArgumentCaptor.forClass(TextCriteria.class);
        verify(postRepo, times(1)).findAllBy(captor.capture(), eq(pageable));
        assertNotNull(captor.getValue());
    }

    @Test
    void updatePost_shouldUpdatePostSuccessfully() {

        String postId = "post123";
        UpdatePostRequest request = new UpdatePostRequest();
        request.setTitle("Updated Title");
        request.setContent("Updated Content");
        request.setTags(List.of("newtag"));

        Post existingPost = new Post("Old Title", "Old Content", List.of());
        existingPost.setPostId(postId);
        existingPost.setAuthor("testuser"); // Set author to match authenticated user

        when(postRepo.findPostByPostId(postId)).thenReturn(Optional.of(existingPost));
        when(postRepo.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));


        Post result = postService.updatePost(postId, request);

        assertNotNull(result);
        assertTrue(result.getTitle().contains("Updated Title"));
        assertTrue(result.getContent().contains("Updated Content"));
        assertEquals(1, result.getTagSlugs().size());
        assertNotNull(result.getLastUpdate());

        verify(postRepo, times(1)).findPostByPostId(postId);
        verify(postRepo, times(1)).save(any(Post.class));
    }

    @Test
    void updatePost_shouldThrowException_whenUserNotAuthor() {
        String postId = "post123";
        UpdatePostRequest request = new UpdatePostRequest();
        
        Post existingPost = new Post("Title", "Content", List.of());
        existingPost.setPostId(postId);
        existingPost.setAuthor("differentuser"); // Different from "testuser"

        when(postRepo.findPostByPostId(postId)).thenReturn(Optional.of(existingPost));

        assertThrows(AccessDeniedException.class, () -> postService.updatePost(postId, request));
        verify(postRepo, never()).save(any(Post.class));
    }

    @Test
    void updatePost_shouldThrowException_whenPostNotFound() {

        String postId = "nonexistent";
        UpdatePostRequest request = new UpdatePostRequest();
        request.setTitle("Title");
        request.setContent("Content");

        when(postRepo.findPostByPostId(postId)).thenReturn(Optional.empty());


        assertThrows(
                ResourceNotFoundException.class,
                () -> postService.updatePost(postId, request)
        );

        verify(postRepo, times(1)).findPostByPostId(postId);
        verify(postRepo, never()).save(any(Post.class));
    }

    @Test
    void addReview_shouldAddReviewSuccessfully() {

        String postId = "post123";
        ReviewRequest request = new ReviewRequest();
        request.setStars(5);

        Post post = new Post("Title", "Content", List.of());
        post.setPostId(postId);
        post.setAuthor("anotheruser");
        post.setReviews(new ArrayList<>());

        Post updatedPost = new Post("Title", "Content", List.of());
        updatedPost.setPostId(postId);
        updatedPost.setAuthor("anotheruser");
        updatedPost.setReviews(List.of(new com.example.demo.domain.Review(5, "testuser", postId)));

        when(postRepo.findPostByPostId(postId)).thenReturn(Optional.of(post));
        when(mongoTemplate.findAndModify(any(Query.class), any(UpdateDefinition.class), any(), eq(Post.class)))
                .thenReturn(updatedPost);


        Post result = postService.addReview(postId, request);

        assertNotNull(result);
        assertEquals(1, result.getReviews().size());
        assertEquals(5, result.getReviews().get(0).getStars());
        assertEquals("testuser", result.getReviews().get(0).getUserId());

        verify(postRepo, times(1)).findPostByPostId(postId);
        verify(mongoTemplate, times(1)).findAndModify(any(Query.class), any(UpdateDefinition.class), any(), eq(Post.class));
    }

    @Test
    void addReview_shouldThrowException_whenAuthorReviewsOwnPost() {

        String postId = "post123";
        ReviewRequest request = new ReviewRequest();
        request.setStars(5);

        Post post = new Post("Title", "Content", List.of());
        post.setPostId(postId);
        post.setAuthor("testuser"); // Same as authenticated user
        post.setReviews(new ArrayList<>());

        when(postRepo.findPostByPostId(postId)).thenReturn(Optional.of(post));


        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> postService.addReview(postId, request)
        );

        assertEquals("Author cannot review their own post", exception.getMessage());
        verify(postRepo, times(1)).findPostByPostId(postId);
        verify(mongoTemplate, never()).findAndModify(any(Query.class), any(UpdateDefinition.class), any(), eq(Post.class));
    }

    @Test
    void addReview_shouldThrowException_whenPostNotFound() {

        String postId = "nonexistent";
        ReviewRequest request = new ReviewRequest();
        request.setStars(5);

        when(postRepo.findPostByPostId(postId)).thenReturn(Optional.empty());


        assertThrows(
                ResourceNotFoundException.class,
                () -> postService.addReview(postId, request)
        );

        verify(postRepo, times(1)).findPostByPostId(postId);
        verify(mongoTemplate, never()).findAndModify(any(Query.class), any(UpdateDefinition.class), any(), eq(Post.class));
    }

    @Test
    void deletePost_shouldDeletePostSuccessfully() {

        String postId = "post123";
        Post post = new Post("Title", "Content", List.of());
        post.setPostId(postId);
        post.setAuthor("testuser");

        when(postRepo.findPostByPostId(postId)).thenReturn(Optional.of(post));
        doNothing().when(postRepo).deleteByPostId(postId);


        postService.deletePost(postId);

        verify(postRepo, times(1)).deleteByPostId(postId);
    }

    @Test
    void deletePost_shouldThrowException_whenUserNotAuthor() {
        String postId = "post123";
        Post post = new Post("Title", "Content", List.of());
        post.setPostId(postId);
        post.setAuthor("differentuser");

        when(postRepo.findPostByPostId(postId)).thenReturn(Optional.of(post));

        assertThrows(AccessDeniedException.class, () -> postService.deletePost(postId));
        verify(postRepo, never()).deleteByPostId(postId);
    }

    @Test
    void createPost_shouldSetCorrectTimestamps() {

        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("Title");
        request.setContent("Content");
        request.setTags(List.of());

        when(postRepo.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));


        Post result = postService.createPost(request);

        assertNotNull(result.getDateCreated());
        assertNotNull(result.getLastUpdate());
        assertEquals(result.getDateCreated(), result.getLastUpdate());
    }

    @Test
    void updatePost_shouldUpdateTimestamp() {

        String postId = "post123";
        UpdatePostRequest request = new UpdatePostRequest();
        request.setTitle("Updated");
        request.setContent("Updated");
        request.setTags(List.of());

        Post existingPost = new Post("Old", "Old", List.of());
        existingPost.setPostId(postId);
        existingPost.setAuthor("testuser");

        when(postRepo.findPostByPostId(postId)).thenReturn(Optional.of(existingPost));
        when(postRepo.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));


        Post result = postService.updatePost(postId, request);

        assertNotNull(result.getLastUpdate());
    }

    @Test
    void getPostsByAuthorAndMinStars_shouldCallRepoMethod() {
        String author = "john_doe";
        int minStars = 4;
        Pageable pageable = PageRequest.of(0, 10);
        Page<Post> page = new PageImpl<>(List.of(new Post()));
        when(postRepo.findPostsByAuthorAndMinStars(author, minStars, pageable)).thenReturn(page);

        Page<Post> result = postService.getPostsByAuthorAndMinStars(author, minStars, pageable);

        assertEquals(1, result.getTotalElements());
        verify(postRepo).findPostsByAuthorAndMinStars(author, minStars, pageable);
    }

    @Test
    void getPostSummaries_shouldCallRepoProjectionMethod() {
        Pageable pageable = PageRequest.of(0, 5);
        com.example.demo.dto.PostSummary summary = mock(com.example.demo.dto.PostSummary.class);
        when(summary.getTitle()).thenReturn("Summary Title");
        
        Page<com.example.demo.dto.PostSummary> page = new PageImpl<>(java.util.Collections.singletonList(summary));
        when(postRepo.findAllProjectedBy(pageable)).thenReturn(page);

        Page<com.example.demo.dto.PostSummary> result = postService.getPostSummaries(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Summary Title", result.getContent().get(0).getTitle());
        verify(postRepo).findAllProjectedBy(pageable);
    }
}
