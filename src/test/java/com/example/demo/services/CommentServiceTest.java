package com.example.demo.services;

import com.example.demo.domain.Comment;
import com.example.demo.domain.Post;
import com.example.demo.repositories.CommentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.ExecutableUpdateOperation;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepo;

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private CommentService commentService;

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
    @SuppressWarnings("unchecked")
    void createComment_ShouldInsertCommentAndPushToPost() {
        // Arrange
        String commentBody = "Great post!";
        String postId = "post123";
        mockAuthentication("testUser");

        // Mocking MongoTemplate fluent API
        ExecutableUpdateOperation.TerminatingUpdate<Post> terminatingUpdate = mock(ExecutableUpdateOperation.TerminatingUpdate.class);
        ExecutableUpdateOperation.UpdateWithUpdate<Post> updateWithUpdate = mock(ExecutableUpdateOperation.UpdateWithUpdate.class);
        ExecutableUpdateOperation.ExecutableUpdate<Post> executableUpdate = mock(ExecutableUpdateOperation.ExecutableUpdate.class);

        when(mongoTemplate.update(Post.class)).thenReturn(executableUpdate);
        when(executableUpdate.matching(any(Criteria.class))).thenReturn(updateWithUpdate);
        when(updateWithUpdate.apply(any(Update.class))).thenReturn(terminatingUpdate);

        // Act
        Comment result = commentService.createComment(commentBody, postId);

        // Assert
        assertNotNull(result);
        assertEquals(commentBody, result.getContent());
        assertEquals("testUser", result.getAuthor());
        assertEquals(postId, result.getParentId());

        verify(commentRepo, times(1)).insert(any(Comment.class));
        verify(terminatingUpdate, times(1)).first();
    }

    @Test
    @SuppressWarnings("unchecked")
    void deleteComment_WhenCommentExists_ShouldDeleteAndPullFromPost() {
        // Arrange
        String commentId = "comment123";
        String postId = "post123";
        Comment comment = new Comment("Content");
        comment.setId(commentId);
        comment.setParentId(postId);

        when(commentRepo.findById(commentId)).thenReturn(comment);

        // Mocking MongoTemplate fluent API
        ExecutableUpdateOperation.TerminatingUpdate<Post> terminatingUpdate = mock(ExecutableUpdateOperation.TerminatingUpdate.class);
        ExecutableUpdateOperation.UpdateWithUpdate<Post> updateWithUpdate = mock(ExecutableUpdateOperation.UpdateWithUpdate.class);
        ExecutableUpdateOperation.ExecutableUpdate<Post> executableUpdate = mock(ExecutableUpdateOperation.ExecutableUpdate.class);

        when(mongoTemplate.update(Post.class)).thenReturn(executableUpdate);
        when(executableUpdate.matching(any(Criteria.class))).thenReturn(updateWithUpdate);
        when(updateWithUpdate.apply(any(Update.class))).thenReturn(terminatingUpdate);

        // Act
        commentService.deleteComment(commentId);

        // Assert
        verify(commentRepo, times(1)).deleteById(commentId);
        verify(terminatingUpdate, times(1)).first();
    }

    @Test
    void deleteComment_WhenCommentDoesNotExist_ShouldDoNothing() {
        // Arrange
        String commentId = "nonexistent";
        when(commentRepo.findById(commentId)).thenReturn(null);

        // Act
        commentService.deleteComment(commentId);

        // Assert
        verify(commentRepo, never()).deleteById(anyString());
        verify(mongoTemplate, never()).update(any());
    }
}
