package com.jewelry.workshop.service.impl;

import com.jewelry.workshop.domain.model.dto.auth.*;
import com.jewelry.workshop.domain.model.entity.Client;
import com.jewelry.workshop.domain.model.entity.User;
import com.jewelry.workshop.domain.repository.ClientRepository;
import com.jewelry.workshop.domain.repository.UserRepository;
import com.jewelry.workshop.infrastructure.email.EmailService;
import com.jewelry.workshop.presentation.exception.EmailAlreadyUsedException;
import com.jewelry.workshop.presentation.exception.InvalidTokenException;
import com.jewelry.workshop.presentation.exception.PasswordMismatchException;
import com.jewelry.workshop.presentation.exception.UserNotFoundException;
import com.jewelry.workshop.security.auth.UserDetailsImpl;
import com.jewelry.workshop.security.jwt.JwtTokenProvider;
import com.jewelry.workshop.service.interfaces.AuthService;
import com.jewelry.workshop.util.Constants;
import com.jewelry.workshop.util.PasswordUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final PasswordUtil passwordUtil;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final StringRedisTemplate redisTemplate;

    @Override
    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyUsedException(request.getEmail());
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException();
        }

        String username = generateUniqueUsername(request.getFirstName(), request.getLastName());

        User user = new User();
        user.setUsername(username);
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordUtil.encode(request.getPassword()));
        user.setRole(User.Role.valueOf(Constants.ROLE_CLIENT));
        user.setEnabled(false);
        user.setEmailVerified(false);
        user.setVerificationToken(UUID.randomUUID().toString());
        user.setVerificationTokenExpiresAt(Instant.now().plus(24, ChronoUnit.HOURS));
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());

        User savedUser = userRepository.save(user);

        Client client = new Client();
        client.setFirstName(request.getFirstName());
        client.setLastName(request.getLastName());
        client.setPatronymic(request.getPatronymic());
        client.setPhone(request.getPhone());
        client.setUser(savedUser);
        client.setCreatedAt(Instant.now());
        client.setUpdatedAt(Instant.now());

        clientRepository.save(client);

        emailService.sendVerificationEmail(user.getEmail(), user.getVerificationToken());

        return AuthResponseDTO.builder()
                .message("Регистрация успешна. Проверьте ваш email для подтверждения.")
                .build();
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getEmail())
                .orElseThrow(() -> new UserNotFoundException("Пользователь с таким email не найден"));

        if (!user.isEmailVerified()) {
            throw new InvalidTokenException("Email не подтверждён. Проверьте вашу почту.");
        }

        String accessToken = jwtTokenProvider.generateToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        redisTemplate.opsForValue().set(
                "refresh_token:" + userDetails.getEmail(),
                refreshToken,
                Duration.ofMillis(jwtTokenProvider.getRefreshExpiration())
        );

        String fullName = user.getUsername();
        if (user.getRole() == User.Role.CLIENT) {
            Client client = clientRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Профиль клиента не найден"));
            fullName = client.getFullName();
        }

        AuthUserDTO authUser = new AuthUserDTO();
        authUser.setId(user.getId());
        authUser.setEmail(user.getEmail());
        authUser.setRole(user.getRole().name());
        authUser.setFullName(fullName);

        return AuthResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(Constants.ACCESS_TOKEN_EXPIRE / 1000)
                .user(authUser)
                .build();
    }

    @Override
    @Transactional
    public boolean verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new InvalidTokenException("Неверный токен"));

        if (user.getVerificationTokenExpiresAt().isBefore(Instant.now())) {
            throw new InvalidTokenException("Токен подтверждения email истёк");
        }

        user.setEmailVerified(true);
        user.setEnabled(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiresAt(null);
        userRepository.save(user);

        emailService.sendWelcomeEmail(user.getEmail());

        return true;
    }

    private String generateUniqueUsername(String firstName, String lastName) {
        String base = (firstName + "." + lastName).toLowerCase().replaceAll("[^a-z0-9.]", "");
        String username = base;
        int counter = 1;
        while (userRepository.existsByUsername(username)) {
            username = base + counter++;
        }
        return username;
    }

    @Override
    public void initiatePasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с таким email не найден"));

        String token = UUID.randomUUID().toString();
        user.setPasswordResetToken(token);
        user.setPasswordResetTokenExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS));

        userRepository.save(user);

        emailService.sendPasswordResetEmail(user.getEmail(), token);
    }

    @Override
    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        User user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new InvalidTokenException("Неверный токен сброса пароля"));

        if (user.getPasswordResetTokenExpiresAt().isBefore(Instant.now())) {
            throw new InvalidTokenException("Токен сброса пароля истёк");
        }

        user.setPasswordHash(passwordUtil.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiresAt(null);

        userRepository.save(user);

        return true;
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new InvalidTokenException("Неверный refresh токен");
        }
        String email = jwtTokenProvider.getEmailFromJwt(refreshToken);
        String storedToken = redisTemplate.opsForValue().get("refresh_token:" + email);
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new InvalidTokenException("Refresh токен недействителен");
        }
        redisTemplate.delete("refresh_token:" + email);
    }

    @Override
    @Transactional
    public AuthResponseDTO refreshToken(RefreshTokenRequestDTO request) {
        String refreshToken = request.getRefreshToken();
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new InvalidTokenException("Неверный refresh токен");
        }

        String email = jwtTokenProvider.getEmailFromJwt(refreshToken);
        String redisKey = "refresh_token:" + email;

        String storedToken = redisTemplate.opsForValue().get(redisKey);
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new InvalidTokenException("Refresh токен недействителен или просрочен");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                UserDetailsImpl.build(user), null, UserDetailsImpl.build(user).getAuthorities()
        );

        String newAccessToken = jwtTokenProvider.generateToken(authentication);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        redisTemplate.opsForValue().set(
                "refresh_token:" + email,
                newRefreshToken,
                Duration.ofMillis(jwtTokenProvider.getRefreshExpiration())
        );

        String fullName = user.getUsername();
        if (User.Role.CLIENT.equals(user.getRole())) {
            Client client = clientRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Профиль клиента не найден"));
            fullName = client.getFullName();
        }

        AuthUserDTO authUser = AuthUserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .fullName(fullName)
                .build();

        return AuthResponseDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(Constants.ACCESS_TOKEN_EXPIRE / 1000)
                .user(authUser)
                .build();
    }

    @Override
    @Transactional
    public void resetUserPassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        user.setPasswordHash(passwordUtil.encode(newPassword));
        userRepository.save(user);

        redisTemplate.delete("refresh_token:" + user.getEmail());
    }
}