package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class UpdatePostRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    @NotNull
    private List<String> tags;
}
