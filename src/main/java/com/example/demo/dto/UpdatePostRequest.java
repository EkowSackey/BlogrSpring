package com.example.demo.dto;

import com.example.demo.validation.UniqueElements;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class UpdatePostRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    @NotNull
    @UniqueElements
    private List<String> tags;
}
