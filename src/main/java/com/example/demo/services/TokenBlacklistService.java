package com.example.demo.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class TokenBlacklistService {

    private final Cache<String, Boolean> blacklistedTokens = Caffeine.newBuilder()
            .expireAfterWrite(24, TimeUnit.HOURS)
            .maximumSize(10000)
            .build();

    public void blacklistToken(String token) {
        blacklistedTokens.put(token, true);
    }

    public boolean isBlacklisted(String token) {
        return blacklistedTokens.getIfPresent(token) != null;
    }
}
