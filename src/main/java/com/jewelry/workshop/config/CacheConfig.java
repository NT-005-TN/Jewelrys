package com.jewelry.workshop.config;

// Product reads deliberately use PostgreSQL; Redis is used only for rate limiting.
// Jackson is configured by Spring Boot rather than an unsafe polymorphic cache mapper.
public final class CacheConfig {
    private CacheConfig() {}
}
