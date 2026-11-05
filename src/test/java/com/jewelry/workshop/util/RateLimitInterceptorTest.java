package com.jewelry.workshop.util;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.*;
import org.springframework.web.method.HandlerMethod;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RateLimitInterceptorTest {
    @RateLimit(keyPrefix = "login")
    public void endpoint() {}

    @Test void spoofedForwardedHeadersDoNotChangeRateLimitKey() throws Exception {
        RateLimitService limits = mock(RateLimitService.class);
        when(limits.isAllowed("login:127.0.0.1", 5, 1)).thenReturn(false);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("X-Forwarded-For", "1.2.3.4");
        request.addHeader("X-Real-IP", "5.6.7.8");
        MockHttpServletResponse response = new MockHttpServletResponse();
        assertFalse(new RateLimitInterceptor(limits).preHandle(request, response,
                new HandlerMethod(this, getClass().getMethod("endpoint"))));
        assertEquals(429, response.getStatus());
        verify(limits).isAllowed("login:127.0.0.1", 5, 1);
    }
}
