package com.example.demo.services;

import com.example.demo.domain.Comment;
import com.example.demo.domain.Post;
import com.example.demo.repositories.CommentRepository;
import com.example.demo.repositories.PostRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@Import(CommentService.class)
@TestPropertySource(properties = {
        "spring.mongodb.embedded.version=4.0.21"
})
class CommentServiceIntegrationTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private CommentRepository commentRepo;

    @Autowired
    private PostRepository postRepo;

    @Autowired
    private MongoTemplate mongoTemplate;

    private Post testPost;

    @BeforeEach
    void setUp() {
        // Set up authentication
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("testuser", null, List.of())
        );

        // Create test post
        testPost = new Post("Test Post", "Test Content", List.of());
        testPost.setComments(new ArrayList<>());
        testPost = postRepo.save(testPost);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        commentRepo.deleteAll();
        postRepo.deleteAll();
    }

    @Test
    void createComment_shouldAddCommentToPost() {

        Comment comment = commentService.createComment("Test comment", testPost.getPostId());

        assertNotNull(comment.getId());
        assertEquals("testuser", comment.getAuthor());

        Post updatedPost = postRepo.findPostByPostId(testPost.getPostId()).orElseThrow();
        assertEquals(1, updatedPost.getComments().size());
        assertEquals("Test comment", updatedPost.getComments().get(0).getContent());
    }

    @Test
    void deleteComment_shouldRemoveComment() {

        Comment comment = commentService.createComment("Test comment", testPost.getPostId());


        commentService.deleteComment(comment.getId());

//        assertFalse(commentRepo.findById(comment.getId()).isPresent());
    }
}
