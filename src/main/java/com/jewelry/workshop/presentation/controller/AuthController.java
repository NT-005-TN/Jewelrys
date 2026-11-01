package com.jewelry.workshop.presentation.controller;

import com.jewelry.workshop.domain.model.dto.auth.AuthResponseDTO;
import com.jewelry.workshop.domain.model.dto.auth.EmailVerificationDTO;
import com.jewelry.workshop.domain.model.dto.auth.LoginRequestDTO;
import com.jewelry.workshop.domain.model.dto.auth.LogoutRequestDTO;
import com.jewelry.workshop.domain.model.dto.auth.PasswordResetConfirmDTO;
import com.jewelry.workshop.domain.model.dto.auth.PasswordResetRequestDTO;
import com.jewelry.workshop.domain.model.dto.auth.RefreshTokenRequestDTO;
import com.jewelry.workshop.domain.model.dto.auth.RegisterRequestDTO;
import com.jewelry.workshop.presentation.exception.PasswordMismatchException;
import com.jewelry.workshop.service.interfaces.AuthService;
import com.jewelry.workshop.util.RateLimit;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Аутентификация")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @RateLimit(maxAttempts = 5, windowMinutes = 60, keyPrefix = "auth_register")
    @Operation(summary = "Зарегистрировать клиента")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    @RateLimit(maxAttempts = 5, windowMinutes = 15, keyPrefix = "auth_login")
    @Operation(summary = "Войти и получить пару токенов")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/verify-email")
    @RateLimit(maxAttempts = 10, windowMinutes = 60, keyPrefix = "auth_verify")
    @Operation(summary = "Подтвердить email одноразовым токеном")
    public ResponseEntity<AuthResponseDTO> verifyEmail(@Valid @RequestBody EmailVerificationDTO request) {
        authService.verifyEmail(request.getToken());
        return ResponseEntity.ok(AuthResponseDTO.builder().message("Email подтверждён").build());
    }

    @PostMapping("/forgot-password")
    @RateLimit(maxAttempts = 3, windowMinutes = 60, keyPrefix = "auth_forgot_password")
    @Operation(summary = "Запросить сброс пароля")
    public ResponseEntity<AuthResponseDTO> forgotPassword(
            @Valid @RequestBody PasswordResetRequestDTO request
    ) {
        authService.initiatePasswordReset(request.getEmail());
        return ResponseEntity.accepted().body(AuthResponseDTO.builder()
                .message("Если аккаунт существует, инструкция отправлена на email.")
                .build());
    }

    @PostMapping("/reset-password")
    @RateLimit(maxAttempts = 5, windowMinutes = 60, keyPrefix = "auth_reset_password")
    @Operation(summary = "Установить новый пароль по одноразовому токену")
    public ResponseEntity<AuthResponseDTO> resetPassword(
            @Valid @RequestBody PasswordResetConfirmDTO request
    ) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException();
        }
        authService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(AuthResponseDTO.builder().message("Пароль изменён").build());
    }

    @PostMapping("/refresh")
    @RateLimit(maxAttempts = 30, windowMinutes = 1, keyPrefix = "auth_refresh")
    @Operation(summary = "Обновить пару токенов")
    public ResponseEntity<AuthResponseDTO> refresh(@Valid @RequestBody RefreshTokenRequestDTO request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @PostMapping("/logout")
    @RateLimit(maxAttempts = 30, windowMinutes = 1, keyPrefix = "auth_logout")
    @Operation(summary = "Отозвать refresh-токен")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequestDTO request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }
}
