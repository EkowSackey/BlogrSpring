package com.example.demo.dto;

import java.util.Date;
import java.util.List;

public interface PostSummary {
    String getPostId();
    String getTitle();
    Date getDateCreated();
    String getAuthor();
    List<String> getTagSlugs();
    // Excludes 'content' and 'reviews' for performance
}
