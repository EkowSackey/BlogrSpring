package com.example.demo.services;

import com.example.demo.domain.Post;
import com.example.demo.domain.Tag;
import com.example.demo.repositories.PostRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepo;

    public List<Post> allPosts(){
        return postRepo.findAll();
    }

    public Optional<Post> singlePost(String postId){
        return postRepo.findPostByPostId(postId);
    }

    public Post createPost(String title, String content, List<String> tags){
        List<Tag> postTags = new ArrayList<>();
        for (String s: tags){
            postTags.add(new Tag(s));
        }

        Post post = new Post(title, content, postTags);
        post.setDateCreated(new Date());
        post.setLastUpdate(new Date());

//        todo: replace with userId at security
        post.setAuthorId(new ObjectId());
        postRepo.save(post);
        return post;
    }

    public Optional<Post> deletePost(String id){
        return postRepo.deleteByPostId(id);
    }}
