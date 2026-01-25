package com.example.demo.controllers;

import com.example.demo.domain.Post;
import com.example.demo.dto.CreatePostRequest;
import com.example.demo.dto.PostPageResponse;
import com.example.demo.dto.PostResponse;
import com.example.demo.dto.UpdatePostRequest;
import com.example.demo.exception.BadRequestException;
import com.example.demo.mapper.PostMapper;
import com.example.demo.services.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@Controller
public class PostGraphQlController {

    private final PostService postService;

    public PostGraphQlController(PostService postService) {
        this.postService = postService;
    }

    @QueryMapping
    public PostResponse getPost(@Argument String id){
        Post post = postService.getPostById(id);
        return PostMapper.toResponse(post);
    }

    @QueryMapping
    public PostPageResponse getAllPosts(
            @Argument String authorId,
            @Argument String tag,
            @Argument Integer page,
            @Argument Integer size,
            @Argument String sort
    ) {
        if (authorId != null && tag != null) {
            throw new BadRequestException("Cannot filter by author and tag at the same time");
        }

        int p = (page != null) ? page : 0;
        int s = (size != null) ? size : 10;
        Sort sorting = (sort != null) ? Sort.by(sort).descending() : Sort.by("dateCreated").descending();
        Pageable pageable = PageRequest.of(p, s, sorting);

        Page<Post> postPage;
        if (authorId != null) {
            postPage = postService.getPostsByAuthor(authorId, pageable);
        } else if (tag != null) {
            postPage = postService.getPostsByTag(tag, pageable);
        } else {
            postPage = postService.getAllPosts(pageable);
        }

        // Convert Page<Post> to PostPageResponse
        List<PostResponse> content = postPage.getContent().stream()
                .map(PostMapper::toResponse)
                .toList();

        return new PostPageResponse(
                content,
                postPage.getTotalPages(),
                postPage.getTotalElements(),
                postPage.isLast()
        );
    }

    @MutationMapping
    public PostResponse createPost(@Argument CreatePostRequest request) {
        Post post = postService.createPost(request);
        return PostMapper.toResponse(post);
    }

    @MutationMapping
    public PostResponse updatePost(@Argument String id, @Argument UpdatePostRequest request) {
        Post updated = postService.updatePost(id, request);
        return PostMapper.toResponse(updated);
    }

    @MutationMapping
    public Boolean deletePost(@Argument String id) {
        postService.deletePost(id);
        return true;
    }
}
