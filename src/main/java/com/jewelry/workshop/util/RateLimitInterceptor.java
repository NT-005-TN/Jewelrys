package com.jewelry.workshop.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitService rateLimitService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);

        if(rateLimit == null) {
            return true;
        }

        String ip = getClientIpAddress(request);
        String key = rateLimit.keyPrefix() + ":" + ip;

        if (!rateLimitService.isAllowed(key, rateLimit.maxAttempts(), rateLimit.windowMinutes())) {
            response.setStatus(429);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("Слишком много запросов. Попробуйте позже.");
            return false;
        }

        return true;
    }

    private String getClientIpAddress(HttpServletRequest request) {
        // Only trust forwarded headers after configuring a trusted proxy.
        return request.getRemoteAddr();
    }
}