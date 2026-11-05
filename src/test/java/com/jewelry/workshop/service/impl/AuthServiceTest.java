package com.jewelry.workshop.service.impl;

import com.jewelry.workshop.domain.model.dto.auth.RefreshTokenRequestDTO;
import com.jewelry.workshop.domain.model.entity.User;
import com.jewelry.workshop.domain.repository.*;
import com.jewelry.workshop.infrastructure.email.EmailService;
import com.jewelry.workshop.presentation.exception.InvalidTokenException;
import com.jewelry.workshop.security.jwt.JwtTokenProvider;
import com.jewelry.workshop.util.PasswordUtil;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.AuthenticationManager;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {
    @Test void disabledUserCannotRefreshEvenWithStoredToken() {
        UserRepository users = mock(UserRepository.class);
        JwtTokenProvider tokens = mock(JwtTokenProvider.class);
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        when(tokens.validateRefreshToken("refresh")).thenReturn(true);
        when(tokens.getSubject("refresh")).thenReturn("1");
        when(values.get("refresh_token:test@example.com")).thenReturn("refresh");
        User user = new User();
        user.setEnabled(false);
        user.setEmailVerified(true);
        when(users.findLockedById(1L)).thenReturn(Optional.of(user));
        AuthServiceImpl service = new AuthServiceImpl(users, mock(ClientRepository.class),
                mock(PasswordUtil.class), tokens, mock(AuthenticationManager.class), mock(EmailService.class));
        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO();
        request.setRefreshToken("refresh");
        assertThrows(InvalidTokenException.class, () -> service.refreshToken(request));
        verify(tokens, never()).generateToken(any());
        verify(tokens, never()).generateRefreshToken(any());
    }
}
