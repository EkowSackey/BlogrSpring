package com.example.demo.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    @Primary
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                "posts",
                "users",
                "post-pages"
        );
        
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(1000)
                .recordStats());
        
        // Disable async mode for stability with complex objects like UserDetails
        cacheManager.setAsyncCacheMode(false);
        
        return cacheManager;
    }

    @Bean
    public CacheManager asyncCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                "analytics-posts",
                "analytics-users",
                "analytics-authors",
                "analytics-tags",
                "analytics-reviews"
        );

        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(1000)
                .recordStats());

        // Enable async mode for AnalyticsService which returns CompletableFuture
        cacheManager.setAsyncCacheMode(true);

        return cacheManager;
    }
}
