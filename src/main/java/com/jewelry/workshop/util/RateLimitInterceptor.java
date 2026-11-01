package com.jewelry.workshop.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitService rateLimitService;
    private final boolean trustForwardedHeaders;

    public RateLimitInterceptor(
            RateLimitService rateLimitService,
            @Value("${app.security.trust-forwarded-headers:false}") boolean trustForwardedHeaders
    ) {
        this.rateLimitService = rateLimitService;
        this.trustForwardedHeaders = trustForwardedHeaders;
    }

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

        try {
            if (!rateLimitService.isAllowed(key, rateLimit.maxAttempts(), rateLimit.windowMinutes())) {
                writeError(response, 429, "RATE_LIMIT_EXCEEDED", "Слишком много запросов. Попробуйте позже.");
                response.setHeader("Retry-After", Integer.toString(rateLimit.windowMinutes() * 60));
                return false;
            }
        } catch (DataAccessException exception) {
            writeError(response, 503, "RATE_LIMIT_UNAVAILABLE", "Сервис временно недоступен");
            return false;
        }

        return true;
    }

    private String getClientIpAddress(HttpServletRequest request) {
        if (!trustForwardedHeaders) {
            return request.getRemoteAddr();
        }

        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return normalizeAddress(xForwardedFor.split(",")[0], request.getRemoteAddr());
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return normalizeAddress(xRealIp, request.getRemoteAddr());
        }

        return request.getRemoteAddr();
    }

    private String normalizeAddress(String candidate, String fallback) {
        String normalized = candidate.trim();
        return normalized.length() <= 64 && normalized.matches("[0-9a-fA-F:.]+") ? normalized : fallback;
    }

    private void writeError(HttpServletResponse response, int status, String code, String message) throws Exception {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"errorCode\":\"" + code + "\",\"message\":\"" + message + "\"}");
    }
}
