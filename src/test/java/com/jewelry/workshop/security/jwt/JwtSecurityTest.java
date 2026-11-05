package com.jewelry.workshop.security.jwt;

import com.jewelry.workshop.domain.model.entity.User;
import com.jewelry.workshop.security.auth.UserDetailsImpl;
import com.jewelry.workshop.security.auth.UserDetailsServiceImpl;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Base64;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtSecurityTest {
    private JwtTokenProvider provider;
    private User user;
    private Authentication authentication;

    @BeforeEach void setup() {
        provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "jwtSecret", Base64.getEncoder().encodeToString(new byte[64]));
        ReflectionTestUtils.setField(provider, "jwtExpiration", 900000L);
        ReflectionTestUtils.setField(provider, "refreshExpiration", 604800000L);
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setUsername("test");
        user.setRole(User.Role.CLIENT);
        user.setEnabled(true);
        authentication = new UsernamePasswordAuthenticationToken(UserDetailsImpl.build(user), null);
        SecurityContextHolder.clearContext();
    }

    @AfterEach void cleanup() { SecurityContextHolder.clearContext(); }

    @Test void accessAndRefreshTokensAreNotInterchangeable() {
        String access = provider.generateToken(authentication);
        String refresh = provider.generateRefreshToken(authentication);
        assertTrue(provider.validateToken(access));
        assertFalse(provider.validateRefreshToken(access));
        assertTrue(provider.validateRefreshToken(refresh));
        assertFalse(provider.validateToken(refresh));
        assertEquals(user.getId().toString(), provider.getSubject(access));
    }

    @Test void refreshTokensAreUniqueEvenWhenIssuedTogether() {
        assertNotEquals(provider.generateRefreshToken(authentication), provider.generateRefreshToken(authentication));
    }

    @Test void expiredAndMalformedTokensAreRejected() {
        ReflectionTestUtils.setField(provider, "jwtExpiration", -1000L);
        assertFalse(provider.validateToken(provider.generateToken(authentication)));
        assertFalse(provider.validateToken("not-a-jwt"));
        assertFalse(provider.validateRefreshToken(""));
    }

    @Test void disabledAccountCannotUsePreviouslyIssuedAccessToken() throws Exception {
        String token = provider.generateToken(authentication);
        user.setEnabled(false);
        UserDetailsServiceImpl users = mock(UserDetailsServiceImpl.class);
        when(users.loadUserById(user.getId())).thenReturn(UserDetailsImpl.build(user));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        new JwtAuthenticationFilter(provider, users).doFilter(request, new MockHttpServletResponse(), new MockFilterChain());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test void refreshTokenDoesNotAuthenticateHttpRequests() throws Exception {
        UserDetailsServiceImpl users = mock(UserDetailsServiceImpl.class);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + provider.generateRefreshToken(authentication));
        new JwtAuthenticationFilter(provider, users).doFilter(request, new MockHttpServletResponse(), new MockFilterChain());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(users);
    }

    @Test void enabledAccountCanUseAccessToken() throws Exception {
        UserDetailsServiceImpl users = mock(UserDetailsServiceImpl.class);
        when(users.loadUserById(user.getId())).thenReturn(UserDetailsImpl.build(user));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + provider.generateToken(authentication));
        new JwtAuthenticationFilter(provider, users).doFilter(request, new MockHttpServletResponse(), new MockFilterChain());
        assertTrue(SecurityContextHolder.getContext().getAuthentication().isAuthenticated());
    }

    @Test void passwordChangeRevokesPreviouslyIssuedAccessToken() throws Exception {
        String token = provider.generateToken(authentication);
        user.setPasswordHash("new-hash");
        UserDetailsServiceImpl users = mock(UserDetailsServiceImpl.class);
        when(users.loadUserById(user.getId())).thenReturn(UserDetailsImpl.build(user));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        new JwtAuthenticationFilter(provider, users).doFilter(request, new MockHttpServletResponse(), new MockFilterChain());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
