package com.jewelry.workshop.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final StringRedisTemplate redisTemplate;

    public boolean isAllowed(String key, int maxAttempts, int windowMinutes) {
        String attemptsKey = "rate_limit:" + key;
        String timeKey = "rate_limit_time:" + key;

        String attemptsStr = redisTemplate.opsForValue().get(attemptsKey);
        String timeStr = redisTemplate.opsForValue().get(timeKey);

        long attempts = attemptsStr != null ? Long.parseLong(attemptsStr) : 0;
        long now = System.currentTimeMillis();

        if (timeStr != null) {
            long time = Long.parseLong(timeStr);
            if (now - time > Duration.ofMinutes(windowMinutes).toMillis()) {
                redisTemplate.delete(attemptsKey);
                redisTemplate.delete(timeKey);
                attempts = 0;
            }
        }

        if (attempts >= maxAttempts) {
            return false;
        }

        redisTemplate.opsForValue().set(attemptsKey, String.valueOf(attempts + 1), Duration.ofMinutes(windowMinutes));
        if (attempts == 0) {
            redisTemplate.opsForValue().set(timeKey, String.valueOf(now), Duration.ofMinutes(windowMinutes));
        }

        return true;
    }
}