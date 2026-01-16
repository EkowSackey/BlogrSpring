package com.example.demo.domain;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Tag {

    @NotNull
    String name;
}