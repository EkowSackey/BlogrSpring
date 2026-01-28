package com.example.demo.exception;

import lombok.Getter;

import java.time.Instant;

@Getter
public class ApiError {

    private final String code;
    private final Object message;
    private final Instant timestamp;

    public ApiError(String code, Object message) {
        this.code = code;
        this.message = message;
        this.timestamp = Instant.now();
    }
}
