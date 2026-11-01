package com.jewelry.workshop.util;

import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;

@Service
public class RateLimitService {

    private static final DefaultRedisScript<Long> INCREMENT_SCRIPT = new DefaultRedisScript<>("""
            local current = redis.call('INCR', KEYS[1])
            if current == 1 then
                redis.call('PEXPIRE', KEYS[1], ARGV[1])
            end
            return current
            """, Long.class);

    private final StringRedisTemplate redisTemplate;

    public RateLimitService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isAllowed(String key, int maxAttempts, int windowMinutes) {
        long windowMillis = Duration.ofMinutes(windowMinutes).toMillis();
        Long attempts = redisTemplate.execute(
                INCREMENT_SCRIPT,
                Collections.singletonList("rate-limit:" + key),
                Long.toString(windowMillis)
        );
        return attempts != null && attempts <= maxAttempts;
    }
}
