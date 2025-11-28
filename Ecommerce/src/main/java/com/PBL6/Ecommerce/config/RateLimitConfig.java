package com.PBL6.Ecommerce.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate Limiting Configuration using Bucket4j
 * Implements token bucket algorithm for API rate limiting
 */
@Configuration
@ConfigurationProperties(prefix = "rate-limit.image-upload")
@Data
public class RateLimitConfig {

    private int perMinute = 10; // Default: 10 uploads per minute
    private int perHour = 50;   // Default: 50 uploads per hour

    /**
     * Cache to store rate limit buckets per user
     * Key: userId, Value: Bucket instance
     */
    private final Map<Long, Bucket> bucketCache = new ConcurrentHashMap<>();

    /**
     * Resolves or creates a rate limit bucket for a user
     * 
     * @param userId User ID
     * @return Bucket for rate limiting
     */
    public Bucket resolveBucket(Long userId) {
        return bucketCache.computeIfAbsent(userId, this::newBucket);
    }

    /**
     * Creates a new bucket with configured rate limits
     * Uses two bandwidth limits: per-minute and per-hour
     * 
     * @param userId User ID (for logging/debugging)
     * @return New Bucket instance
     */
    private Bucket newBucket(Long userId) {
        // Per-minute limit: 10 tokens, refill 10 tokens every minute
        Bandwidth perMinuteLimit = Bandwidth.classic(
            perMinute,
            Refill.intervally(perMinute, Duration.ofMinutes(1))
        );

        // Per-hour limit: 50 tokens, refill 50 tokens every hour
        Bandwidth perHourLimit = Bandwidth.classic(
            perHour,
            Refill.intervally(perHour, Duration.ofHours(1))
        );

        return Bucket.builder()
            .addLimit(perMinuteLimit)
            .addLimit(perHourLimit)
            .build();
    }

    /**
     * Clears the bucket cache (useful for testing or admin operations)
     */
    public void clearCache() {
        bucketCache.clear();
    }
}
