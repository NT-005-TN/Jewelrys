package com.jewelry.workshop.util;

import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class PaginationInterceptor implements HandlerInterceptor {
    @Override public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        check(request.getParameter("page"), 0, 1_000_000, "page");
        check(request.getParameter("size"), 1, 100, "size");
        check(request.getParameter("days"), 1, 3660, "days");
        return true;
    }
    private void check(String value, int min, int max, String field) {
        if (value == null) return;
        try {
            long number = Long.parseLong(value);
            if (number < min || number > max) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(field + " должен быть в диапазоне " + min + ".." + max);
        }
    }
}
