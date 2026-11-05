package com.jewelry.workshop.presentation.controller;

import com.jewelry.workshop.domain.model.dto.auth.*;
import com.jewelry.workshop.service.interfaces.AuthService;
import com.jewelry.workshop.util.RateLimit;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @GetMapping(value = "/reset-password-form", produces = "text/html;charset=UTF-8")
    public org.springframework.core.io.Resource resetForm() {
        return new org.springframework.core.io.ClassPathResource("web/reset-password.html");
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @RateLimit(keyPrefix = "register")
    public AuthResponseDTO register(@Valid @RequestBody RegisterRequestDTO request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    @RateLimit(keyPrefix = "login")
    public AuthResponseDTO login(@Valid @RequestBody LoginRequestDTO request) {
        return authService.login(request);
    }

    @GetMapping("/verify-email")
    @RateLimit(keyPrefix = "verify_email")
    public boolean verifyEmail(@RequestParam String token) {
        return authService.verifyEmail(token);
    }

    @PostMapping("/refresh")
    @RateLimit(maxAttempts = 20, keyPrefix = "refresh")
    public AuthResponseDTO refresh(@Valid @RequestBody RefreshTokenRequestDTO request) {
        return authService.refreshToken(request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody LogoutRequestDTO request) {
        authService.logout(request.getRefreshToken());
    }

    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @RateLimit(keyPrefix = "forgot_password")
    public void forgotPassword(@Valid @RequestBody PasswordResetRequestDTO request) {
        authService.initiatePasswordReset(request.getEmail());
    }

    @PostMapping("/reset-password")
    @RateLimit(keyPrefix = "reset_password")
    public boolean resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return authService.resetPassword(request.token(), request.newPassword());
    }

    public record ResetPasswordRequest(@NotBlank String token,
                                      @NotBlank @Size(min = 8, max = 72) String newPassword) {}
}
