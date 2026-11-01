package com.jewelry.workshop.service.interfaces;

import com.jewelry.workshop.domain.model.dto.auth.AuthResponseDTO;
import com.jewelry.workshop.domain.model.dto.auth.LoginRequestDTO;
import com.jewelry.workshop.domain.model.dto.auth.RefreshTokenRequestDTO;
import com.jewelry.workshop.domain.model.dto.auth.RegisterRequestDTO;

public interface AuthService {
    AuthResponseDTO register(RegisterRequestDTO request);
    AuthResponseDTO login(LoginRequestDTO request);
    boolean verifyEmail(String token);

    void initiatePasswordReset(String email);
    boolean resetPassword(String token, String newPassword);

    AuthResponseDTO refreshToken(RefreshTokenRequestDTO request);
    void logout(String refreshToken);
    void resetUserPassword(Long userId, String newPassword);
}
