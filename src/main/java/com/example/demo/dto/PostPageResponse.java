package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PostPageResponse {
    private List<PostResponse> content;
    private int totalPages;
    private long totalElements;
    private boolean isLast;
}