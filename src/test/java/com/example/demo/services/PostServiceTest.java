package com.example.demo.services;

import com.example.demo.domain.Post;
import com.example.demo.dto.CreatePostRequest;
import com.example.demo.dto.UpdatePostRequest;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repositories.PostRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;


import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {
    @Mock
    PostRepository postRepository;

    @InjectMocks
    PostService postService;

    @Mock
    Pageable pageable;

    @BeforeEach
    void setUp(){
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "testUser",
                null,
                List.of()
        );

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown(){
        SecurityContextHolder.clearContext();
    }

    @Test
    void createPostTest() {
        String[] tags = {"tag1", "tag2"};
        CreatePostRequest postRequest = new CreatePostRequest("post title", "post content", List.of(tags) );


        Post post = postService.createPost(postRequest);

        assertNotNull(post);
        assertEquals(post.getTitle(), postRequest.getTitle());
        assertEquals(post.getContent(), postRequest.getContent());
        assertEquals(post.getTagSlugs(), postRequest.getTags());
        assertEquals("testUser", post.getAuthor());

        verify(postRepository, Mockito.times(1))
                .save(any(Post.class));
    }

    @Test
    void getPostByIdTest() {
        Assertions.assertThrows(ResourceNotFoundException.class,()-> postService.getPostById("1"));
        verify(postRepository, Mockito.times(1))
                .findPostByPostId("1");
    }

    @Test
    void getAllPostsTest() {
        postService.getAllPosts(pageable);
        verify(postRepository, Mockito.times(1))
                .findAll(pageable);
    }

    @Test
    void getPostsByAuthorTest() {
        postService.getPostsByAuthor("authorUsername", pageable);
        verify(postRepository, Mockito.times(1))
                .findByAuthor(any(String.class), any(Pageable.class));
    }

    @Test
    void getPostsByTagTest() {
        postService.getPostsByTag("tag", pageable);
        verify(postRepository, Mockito.times(1))
                .findByTagSlugsContaining(any(String.class), any(Pageable.class));
    }

    @Test
    void updatePostTest() {
        assertThrows(ResourceNotFoundException.class, ()->postService.updatePost( "id", new UpdatePostRequest()));

        verify(postRepository, Mockito.times(0)).save(any(Post.class));
    }

    @Test
    void deletePostTest() {
        Mockito.doNothing().when(postRepository).deleteByPostId("1");
        postService.deletePost("1");

        verify(postRepository, Mockito.times(1))
                .deleteByPostId("1");
    }
}