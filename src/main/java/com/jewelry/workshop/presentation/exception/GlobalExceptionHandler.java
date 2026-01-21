package com.jewelry.workshop.presentation.exception;

import com.jewelry.workshop.presentation.exception.error.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponseDTO> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
        log.warn("Bad request error at {}: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                null,
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                sb.append(error.getField()).append(": ").append(error.getDefaultMessage()).append("; ")
        );
        String message = sb.toString().trim();

        log.warn("Validation error at {}: {}", request.getRequestURI(), message);
        ErrorResponseDTO error = new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_ERROR",
                message,
                request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDTO> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        log.warn("Authentication failed at {}: invalid credentials", request.getRequestURI());
        ErrorResponseDTO error = new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                "INVALID_CREDENTIALS",
                "Неверный email или пароль",
                request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied at {}: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.FORBIDDEN.value(),
                "ACCESS_DENIED",
                "У вас нет прав для выполнения этого действия",
                request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Internal server error at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        ErrorResponseDTO error = new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_ERROR",
                "Произошла внутренняя ошибка сервера",
                request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(EmailAlreadyUsedException.class)
    public ResponseEntity<ErrorResponseDTO> handleEmailAlreadyUsed(EmailAlreadyUsedException ex, HttpServletRequest request) {
        log.warn("Email conflict at {}: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                "EMAIL_ALREADY_USED",
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> handlePasswordMismatch(PasswordMismatchException ex, HttpServletRequest request) {
        log.warn("Password mismatch at {}: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "PASSWORD_MISMATCH",
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidToken(InvalidTokenException ex, HttpServletRequest request) {
        log.warn("Invalid token at {}: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "INVALID_TOKEN",
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserNotFound(UserNotFoundException ex, HttpServletRequest request) {
        log.warn("User not found at {}: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "USER_NOT_FOUND",
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ClientProfileNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleClientProfileNotFound(ClientProfileNotFoundException ex, HttpServletRequest request) {
        log.warn("Client profile not found at {}: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "CLIENT_PROFILE_NOT_FOUND",
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ErrorResponseDTO> handleUnauthorizedAccess(UnauthorizedAccessException ex, HttpServletRequest request) {
        log.warn("Unauthorized access at {}: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.FORBIDDEN.value(),
                "UNAUTHORIZED_ACCESS",
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(PhoneAlreadyUsedException.class)
    public ResponseEntity<ErrorResponseDTO> handlePhoneAlreadyUsed(PhoneAlreadyUsedException ex, HttpServletRequest request) {
        log.warn("Phone conflict at {}: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                "PHONE_ALREADY_USED",
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidCredentials(
            InvalidCredentialsException ex, HttpServletRequest request) {
        ErrorResponseDTO error = new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "INVALID_CREDENTIALS",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.badRequest().body(error);
    }
}