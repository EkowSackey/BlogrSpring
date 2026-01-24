package com.example.demo.mapper;

import com.example.demo.domain.Post;
import com.example.demo.dto.PostResponse;

public class PostMapper {

    private PostMapper(){}

    public static PostResponse toResponse(Post post){
        return new PostResponse(
                post.getPostId(),
                post.getTitle(),
                post.getContent(),
                post.getAuthorId(),
                post.getTagSlugs(),
                post.getComments(),
                post.getReviews(),
                post.getDateCreated(),
                post.getLastUpdate()
        );
    }
}
