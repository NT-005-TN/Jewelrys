package com.jewelry.workshop.service.impl;

import com.jewelry.workshop.domain.model.dto.auth.AuthResponseDTO;
import com.jewelry.workshop.domain.model.dto.auth.AuthUserDTO;
import com.jewelry.workshop.domain.model.dto.auth.LoginRequestDTO;
import com.jewelry.workshop.domain.model.dto.auth.RefreshTokenRequestDTO;
import com.jewelry.workshop.domain.model.dto.auth.RegisterRequestDTO;
import com.jewelry.workshop.domain.model.entity.Client;
import com.jewelry.workshop.domain.model.entity.User;
import com.jewelry.workshop.domain.repository.ClientRepository;
import com.jewelry.workshop.domain.repository.UserRepository;
import com.jewelry.workshop.infrastructure.email.EmailService;
import com.jewelry.workshop.presentation.exception.EmailAlreadyUsedException;
import com.jewelry.workshop.presentation.exception.InvalidTokenException;
import com.jewelry.workshop.presentation.exception.PasswordMismatchException;
import com.jewelry.workshop.presentation.exception.PhoneAlreadyUsedException;
import com.jewelry.workshop.presentation.exception.UserNotFoundException;
import com.jewelry.workshop.security.auth.UserDetailsImpl;
import com.jewelry.workshop.security.jwt.JwtTokenProvider;
import com.jewelry.workshop.security.token.TokenHashService;
import com.jewelry.workshop.service.interfaces.AuthService;
import com.jewelry.workshop.util.PasswordUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private static final String REFRESH_TOKEN_PREFIX = "auth:refresh:";

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final PasswordUtil passwordUtil;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final StringRedisTemplate redisTemplate;
    private final TokenHashService tokenHashService;

    @Override
    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        String email = normalizeEmail(request.getEmail());
        String phone = request.getPhone().trim();

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyUsedException(email);
        }
        if (clientRepository.findByPhone(phone).isPresent()) {
            throw new PhoneAlreadyUsedException(phone);
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException();
        }

        String rawVerificationToken = tokenHashService.generateToken();
        User user = new User();
        user.setUsername(generateUniqueUsername(request.getFirstName(), request.getLastName(), email));
        user.setEmail(email);
        user.setPasswordHash(passwordUtil.encode(request.getPassword()));
        user.setRole(User.Role.CLIENT);
        user.setEnabled(false);
        user.setEmailVerified(false);
        user.setVerificationToken(tokenHashService.hash(rawVerificationToken));
        user.setVerificationTokenExpiresAt(Instant.now().plus(24, ChronoUnit.HOURS));

        User savedUser = userRepository.save(user);

        Client client = new Client();
        client.setFirstName(request.getFirstName().trim());
        client.setLastName(request.getLastName().trim());
        client.setPatronymic(trimToNull(request.getPatronymic()));
        client.setPhone(phone);
        client.setUser(savedUser);
        client.setIsPermanent(Boolean.TRUE.equals(request.getIsPermanent()));
        clientRepository.save(client);

        runAfterCommit(() -> emailService.sendVerificationEmail(email, rawVerificationToken));

        return AuthResponseDTO.builder()
                .message("Регистрация успешна. Проверьте email для подтверждения.")
                .build();
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO request) {
        String email = normalizeEmail(request.getEmail());
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword())
            );
        } catch (AuthenticationException exception) {
            throw new BadCredentialsException("Неверный email или пароль");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Неверный email или пароль"));
        requireActiveVerifiedUser(user);
        return issueTokenPair(authentication, user);
    }

    @Override
    @Transactional
    public boolean verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(tokenHashService.hash(token))
                .orElseThrow(() -> new InvalidTokenException("Недействительный токен подтверждения"));

        if (user.getVerificationTokenExpiresAt() == null
                || !user.getVerificationTokenExpiresAt().isAfter(Instant.now())) {
            throw new InvalidTokenException("Токен подтверждения email истёк");
        }

        user.setEmailVerified(true);
        user.setEnabled(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiresAt(null);
        userRepository.save(user);
        runAfterCommit(() -> emailService.sendWelcomeEmail(user.getEmail()));
        return true;
    }

    @Override
    public void initiatePasswordReset(String rawEmail) {
        String email = normalizeEmail(rawEmail);
        userRepository.findByEmail(email).ifPresent(user -> {
            String rawToken = tokenHashService.generateToken();
            user.setPasswordResetToken(tokenHashService.hash(rawToken));
            user.setPasswordResetTokenExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS));
            userRepository.save(user);
            try {
                emailService.sendPasswordResetEmail(user.getEmail(), rawToken);
            } catch (RuntimeException exception) {
                log.error("Не удалось отправить письмо для сброса пароля");
            }
        });
    }

    @Override
    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        User user = userRepository.findByPasswordResetToken(tokenHashService.hash(token))
                .orElseThrow(() -> new InvalidTokenException("Недействительный токен сброса пароля"));

        if (user.getPasswordResetTokenExpiresAt() == null
                || !user.getPasswordResetTokenExpiresAt().isAfter(Instant.now())) {
            throw new InvalidTokenException("Токен сброса пароля истёк");
        }

        user.setPasswordHash(passwordUtil.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiresAt(null);
        userRepository.save(user);
        revokeRefreshToken(user.getEmail());
        return true;
    }

    @Override
    public void logout(String refreshToken) {
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw new InvalidTokenException("Недействительный refresh-токен");
        }
        String email = jwtTokenProvider.getEmailFromJwt(refreshToken);
        String key = refreshTokenKey(email);
        String storedHash = redisTemplate.opsForValue().get(key);
        if (!tokenHashService.matches(refreshToken, storedHash)) {
            throw new InvalidTokenException("Недействительный refresh-токен");
        }
        redisTemplate.delete(key);
    }

    @Override
    public AuthResponseDTO refreshToken(RefreshTokenRequestDTO request) {
        String refreshToken = request.getRefreshToken();
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw new InvalidTokenException("Недействительный refresh-токен");
        }

        String email = jwtTokenProvider.getEmailFromJwt(refreshToken);
        String storedHash = redisTemplate.opsForValue().get(refreshTokenKey(email));
        if (!tokenHashService.matches(refreshToken, storedHash)) {
            throw new InvalidTokenException("Недействительный или отозванный refresh-токен");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidTokenException("Недействительный refresh-токен"));
        requireActiveVerifiedUser(user);

        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        return issueTokenPair(authentication, user);
    }

    @Override
    @Transactional
    public void resetUserPassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
        user.setPasswordHash(passwordUtil.encode(newPassword));
        userRepository.save(user);
        revokeRefreshToken(user.getEmail());
    }

    private AuthResponseDTO issueTokenPair(Authentication authentication, User user) {
        String accessToken = jwtTokenProvider.generateToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);
        redisTemplate.opsForValue().set(
                refreshTokenKey(user.getEmail()),
                tokenHashService.hash(refreshToken),
                Duration.ofMillis(jwtTokenProvider.getRefreshExpiration())
        );

        return AuthResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtTokenProvider.getJwtExpiration() / 1000)
                .user(toAuthUser(user))
                .build();
    }

    private AuthUserDTO toAuthUser(User user) {
        String fullName = user.getUsername();
        if (User.Role.CLIENT.equals(user.getRole())) {
            fullName = clientRepository.findByUserId(user.getId())
                    .map(Client::getFullName)
                    .orElse(user.getUsername());
        }
        return AuthUserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .fullName(fullName)
                .build();
    }

    private void requireActiveVerifiedUser(User user) {
        if (!user.isEnabled() || !user.isEmailVerified()) {
            revokeRefreshToken(user.getEmail());
            throw new BadCredentialsException("Неверный email или пароль");
        }
    }

    private void revokeRefreshToken(String email) {
        redisTemplate.delete(refreshTokenKey(email));
    }

    private String refreshTokenKey(String email) {
        return REFRESH_TOKEN_PREFIX + normalizeEmail(email);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String generateUniqueUsername(String firstName, String lastName, String email) {
        String base = (firstName + "." + lastName)
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9._-]", "")
                .replaceAll("^[._-]+|[._-]+$", "");
        if (base.length() < 3) {
            base = email.substring(0, email.indexOf('@'))
                    .replaceAll("[^a-z0-9._-]", "")
                    .replaceAll("^[._-]+|[._-]+$", "");
        }
        if (base.length() < 3) {
            base = "client";
        }
        if (base.length() > 42) {
            base = base.substring(0, 42);
        }

        String username = base;
        int counter = 1;
        while (userRepository.existsByUsername(username)) {
            username = base + counter++;
        }
        return username;
    }

    private void runAfterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    runSafely(action);
                }
            });
        } else {
            runSafely(action);
        }
    }

    private void runSafely(Runnable action) {
        try {
            action.run();
        } catch (RuntimeException exception) {
            log.error("Не удалось отправить системное письмо");
        }
    }
}
