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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
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
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private PostService postService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockAuthentication(String username) {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(username);
    }

    @Test
    void createPost_ShouldReturnSavedPost() {
        // Arrange
        CreatePostRequest request = new CreatePostRequest("Valid Title", "Valid Content", List.of("tag1"));
        mockAuthentication("testUser");
        when(postRepo.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Post result = postService.createPost(request);

        // Assert
        assertNotNull(result);
        assertEquals("Valid Title", result.getTitle());
        assertEquals("testUser", result.getAuthor());
        verify(postRepo, times(1)).save(any(Post.class));
    }

    @Test
    void getPostById_WhenPostExists_ShouldReturnPost() {
        // Arrange
        String id = "1";
        Post post = new Post("Title", "Content", new ArrayList<>());
        post.setPostId(id);
        when(postRepo.findPostByPostId(id)).thenReturn(Optional.of(post));

        // Act
        Post result = postService.getPostById(id);

        // Assert
        assertEquals(id, result.getPostId());
    }

    @Test
    void getPostById_WhenPostDoesNotExist_ShouldThrowException() {
        // Arrange
        String id = "1";
        when(postRepo.findPostByPostId(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> postService.getPostById(id));
    }

    @Test
    void getAllPosts_ShouldReturnPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Post> posts = List.of(new Post("T1", "C1", List.of()), new Post("T2", "C2", List.of()));
        Page<Post> page = new PageImpl<>(posts, pageable, posts.size());
        when(postRepo.findAll(pageable)).thenReturn(page);

        // Act
        Page<Post> result = postService.getAllPosts(pageable);

        // Assert
        assertEquals(2, result.getContent().size());
        verify(postRepo, times(1)).findAll(pageable);
    }

    @Test
    void updatePost_ShouldReturnUpdatedPost() {
        // Arrange
        String id = "1";
        Post existingPost = new Post("Old Title", "Old Content", List.of("old"));
        existingPost.setPostId(id);
        
        UpdatePostRequest request = new UpdatePostRequest();
        request.setTitle("New Title");
        request.setContent("New Content");
        request.setTags(List.of("new"));
        
        when(postRepo.findPostByPostId(id)).thenReturn(Optional.of(existingPost));
        when(postRepo.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Post result = postService.updatePost(id, request);

        // Assert
        assertEquals("New Title", result.getTitle());
        assertEquals("New Content", result.getContent());
        assertEquals(List.of("new"), result.getTagSlugs());
        verify(postRepo, times(1)).save(any(Post.class));
    }

    @Test
    void addReview_WhenAuthorReviewsOwnPost_ShouldThrowException() {
        // Arrange
        String id = "1";
        Post post = new Post("Title", "Content", new ArrayList<>());
        post.setAuthor("testUser");
        when(postRepo.findPostByPostId(id)).thenReturn(Optional.of(post));
        mockAuthentication("testUser");
        ReviewRequest request = new ReviewRequest(5.0);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> postService.addReview(id, request));
    }

    @Test
    void addReview_WhenValid_ShouldAddReview() {
        // Arrange
        String id = "1";
        Post post = new Post("Title", "Content", new ArrayList<>());
        post.setAuthor("author");
        post.setReviews(new ArrayList<>());
        when(postRepo.findPostByPostId(id)).thenReturn(Optional.of(post));
        mockAuthentication("reviewer");
        ReviewRequest request = new ReviewRequest(4.5);
        when(postRepo.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Post result = postService.addReview(id, request);

        // Assert
        assertEquals(1, result.getReviews().size());
        assertEquals("reviewer", result.getReviews().get(0).getUserId());
        assertEquals(4.5, result.getReviews().get(0).getStars());
        verify(postRepo, times(1)).save(any(Post.class));
    }

    @Test
    void deletePost_ShouldCallRepository() {
        // Arrange
        String id = "1";

        // Act
        postService.deletePost(id);

        // Assert
        verify(postRepo, times(1)).deleteByPostId(id);
    }
}
