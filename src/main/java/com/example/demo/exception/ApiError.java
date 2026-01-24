package com.example.demo.exception;

import java.time.Instant;

public class ApiError {

    private final String code;
    private final Object message;
    private final Instant timestamp;

    public ApiError(String code, Object message) {
        this.code = code;
        this.message = message;
        this.timestamp = Instant.now();
    }

    public String getCode(){
        return code;
    }

    public Object getMessage() {
        return message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
