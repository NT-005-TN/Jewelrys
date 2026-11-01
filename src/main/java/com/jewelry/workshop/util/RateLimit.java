package com.jewelry.workshop.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    int maxAttempts() default 5;
    int windowMinutes() default 1;
    String keyPrefix() default "rate_limit";
}
