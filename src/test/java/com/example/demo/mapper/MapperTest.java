package com.example.demo.mapper;

import com.example.demo.domain.Comment;
import com.example.demo.domain.Post;
import com.example.demo.domain.Review;
import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.dto.PostResponse;
import com.example.demo.dto.UserResponse;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MapperTest {

    @Test
    void postMapper_toResponse_shouldMapAllFields() {
        // Arrange
        Post post = new Post();
        post.setPostId("post123");
        post.setTitle("Test Title");
        post.setContent("Test Content");
        post.setAuthor("testuser");
        post.setTagSlugs(List.of("java", "spring"));
        post.setComments(new ArrayList<>());
        post.setReviews(new ArrayList<>());
        Date now = new Date();
        post.setDateCreated(now);
        post.setLastUpdate(now);

        // Act
        PostResponse response = PostMapper.toResponse(post);

        // Assert
        assertNotNull(response);
        assertEquals("post123", response.getId());
        assertEquals("Test Title", response.getTitle());
        assertEquals("Test Content", response.getContent());
        assertEquals("testuser", response.getAuthor());
        assertEquals(2, response.getTags().size());
        assertEquals(now, response.getCreatedAt());
        assertEquals(now, response.getLastUpdate());
    }

    @Test
    void postMapper_toResponse_shouldHandleNullTags() {
        // Arrange
        Post post = new Post();
        post.setPostId("post123");
        post.setTitle("Title");
        post.setContent("Content");
        post.setTagSlugs(null);

        // Act
        PostResponse response = PostMapper.toResponse(post);

        // Assert
        assertNotNull(response);
        assertNull(response.getTags());
    }

    @Test
    void postMapper_toResponse_shouldHandleEmptyTags() {
        // Arrange
        Post post = new Post();
        post.setPostId("post123");
        post.setTitle("Title");
        post.setContent("Content");
        post.setTagSlugs(new ArrayList<>());

        // Act
        PostResponse response = PostMapper.toResponse(post);

        // Assert
        assertNotNull(response);
        assertTrue(response.getTags().isEmpty());
    }

    @Test
    void postMapper_toResponse_shouldHandleNullComments() {
        // Arrange
        Post post = new Post();
        post.setPostId("post123");
        post.setComments(null);

        // Act
        PostResponse response = PostMapper.toResponse(post);

        // Assert
        assertNotNull(response);
        assertNull(response.getComments());
    }

    @Test
    void postMapper_toResponse_shouldIncludeComments() {
        // Arrange
        Post post = new Post();
        post.setPostId("post123");
        
        Comment comment = new Comment("Test comment");
        comment.setId("comment123");
        post.setComments(List.of(comment));

        // Act
        PostResponse response = PostMapper.toResponse(post);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getComments().size());
        assertEquals("comment123", response.getComments().get(0).getId());
    }

    @Test
    void postMapper_toResponse_shouldIncludeReviews() {
        // Arrange
        Post post = new Post();
        post.setPostId("post123");
        
        Review review = new Review(5.0, "user1", "post123");
        post.setReviews(List.of(review));

        // Act
        PostResponse response = PostMapper.toResponse(post);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getReviews().size());
        assertEquals(5.0, response.getReviews().get(0).getStars());
    }

    @Test
    void userMapper_toResponse_shouldMapAllFields() {
        // Arrange
        User user = new User();
        user.setUserId("user123");
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setRoles(List.of(Role.USER));
        Date now = new Date();
        user.setCreatedAt(now);

        // Act
        UserResponse response = UserMapper.toResponse(user);

        // Assert
        assertNotNull(response);
        assertEquals("user123", response.getUserId());
        assertEquals("testuser", response.getUsername());
        assertEquals("test@example.com", response.getEmail());
        assertEquals(1, response.getRoles().size());
        assertEquals(Role.USER, response.getRoles().get(0));
        assertEquals(now, response.getCreatedAt());
    }

    @Test
    void userMapper_toResponse_shouldHandleNullUser() {
        // Act
        UserResponse response = UserMapper.toResponse(null);

        // Assert
        assertNull(response);
    }

    @Test
    void userMapper_toResponse_shouldHandleMultipleRoles() {
        // Arrange
        User user = new User();
        user.setUserId("admin123");
        user.setUsername("admin");
        user.setEmail("admin@example.com");
        user.setRoles(List.of(Role.USER, Role.ADMIN));

        // Act
        UserResponse response = UserMapper.toResponse(user);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.getRoles().size());
        assertTrue(response.getRoles().contains(Role.USER));
        assertTrue(response.getRoles().contains(Role.ADMIN));
    }

    @Test
    void userMapper_toResponse_shouldHandleNullFields() {
        // Arrange
        User user = new User();
        user.setUserId("user123");
        user.setUsername(null);
        user.setEmail(null);
        user.setRoles(null);
        user.setCreatedAt(null);

        // Act
        UserResponse response = UserMapper.toResponse(user);

        // Assert
        assertNotNull(response);
        assertEquals("user123", response.getUserId());
        assertNull(response.getUsername());
        assertNull(response.getEmail());
        assertNull(response.getRoles());
        assertNull(response.getCreatedAt());
    }

    @Test
    void postMapper_toResponse_shouldHandleSpecialCharactersInContent() {
        // Arrange
        Post post = new Post();
        post.setPostId("post123");
        post.setTitle("Test !@#$%");
        post.setContent("Content with special chars: <>&\"'");

        // Act
        PostResponse response = PostMapper.toResponse(post);

        // Assert
        assertEquals("Test !@#$%", response.getTitle());
        assertEquals("Content with special chars: <>&\"'", response.getContent());
    }

    @Test
    void userMapper_toResponse_shouldHandleSpecialCharactersInEmail() {
        // Arrange
        User user = new User();
        user.setUserId("user123");
        user.setUsername("user+test");
        user.setEmail("user+test@example.com");

        // Act
        UserResponse response = UserMapper.toResponse(user);

        // Assert
        assertEquals("user+test", response.getUsername());
        assertEquals("user+test@example.com", response.getEmail());
    }

    @Test
    void postMapper_toResponse_shouldHandleEmptyContent() {
        // Arrange
        Post post = new Post();
        post.setPostId("post123");
        post.setTitle("");
        post.setContent("");

        // Act
        PostResponse response = PostMapper.toResponse(post);

        // Assert
        assertEquals("", response.getTitle());
        assertEquals("", response.getContent());
    }

    @Test
    void postMapper_toResponse_shouldHandleMultipleReviews() {
        // Arrange
        Post post = new Post();
        post.setPostId("post123");
        
        List<Review> reviews = List.of(
                new Review(5.0, "user1", "post123"),
                new Review(4.0, "user2", "post123"),
                new Review(3.5, "user3", "post123")
        );
        post.setReviews(reviews);

        // Act
        PostResponse response = PostMapper.toResponse(post);

        // Assert
        assertEquals(3, response.getReviews().size());
    }

    @Test
    void userMapper_toResponse_shouldHandleEmptyRolesList() {
        // Arrange
        User user = new User();
        user.setUserId("user123");
        user.setRoles(new ArrayList<>());

        // Act
        UserResponse response = UserMapper.toResponse(user);

        // Assert
        assertNotNull(response.getRoles());
        assertTrue(response.getRoles().isEmpty());
    }
}
