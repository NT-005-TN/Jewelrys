package com.jewelry.workshop.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RateLimitService {
    private final StringRedisTemplate redisTemplate;
    private static final DefaultRedisScript<Long> INCREMENT = new DefaultRedisScript<>("""
            local count = redis.call('INCR', KEYS[1])
            if count == 1 then redis.call('PEXPIRE', KEYS[1], ARGV[1]) end
            return count
            """, Long.class);

    public boolean isAllowed(String key, int maxAttempts, int windowMinutes) {
        if (maxAttempts <= 0 || windowMinutes <= 0) {
            throw new IllegalArgumentException("Rate limit and window must be positive");
        }
        Long count = redisTemplate.execute(INCREMENT, List.of("rate_limit:" + key),
                Long.toString(Duration.ofMinutes(windowMinutes).toMillis()));
        return count != null && count <= maxAttempts;
    }
}
