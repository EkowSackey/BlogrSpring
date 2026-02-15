package com.example.demo.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PostPageResponse {
    @Schema(description = "List of posts in the current page")
    private List<PostResponse> content;
    @Schema(description = "Total number of pages", example = "5")
    private int totalPages;
    @Schema(description = "Total number of elements", example = "50")
    private long totalElements;
    @Schema(description = "Whether this is the last page", example = "false")
    private boolean isLast;
}