package com.jewelry.workshop.presentation.exception;

import com.jewelry.workshop.presentation.exception.error.ErrorResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .distinct()
                .sorted()
                .collect(Collectors.joining("; "));
        log.warn("Validation error at {}", request.getRequestURI());
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        String message = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .distinct()
                .sorted()
                .collect(Collectors.joining("; "));
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, request);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ErrorResponseDTO> handleMalformedRequest(Exception ex, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST", "Некорректный формат запроса", request);
    }

    @ExceptionHandler({BadCredentialsException.class, InvalidCredentialsException.class})
    public ResponseEntity<ErrorResponseDTO> handleBadCredentials(Exception ex, HttpServletRequest request) {
        log.warn("Authentication failed at {}", request.getRequestURI());
        return response(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Неверный email или пароль", request);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidToken(InvalidTokenException ex, HttpServletRequest request) {
        log.warn("Invalid token at {}", request.getRequestURI());
        return response(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "Токен недействителен или просрочен", request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied at {}", request.getRequestURI());
        return response(HttpStatus.FORBIDDEN, "ACCESS_DENIED",
                "У вас нет прав для выполнения этого действия", request);
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ErrorResponseDTO> handleUnauthorizedAccess(
            UnauthorizedAccessException ex,
            HttpServletRequest request
    ) {
        log.warn("Unauthorized access at {}", request.getRequestURI());
        return response(HttpStatus.FORBIDDEN, "ACCESS_DENIED", ex.getMessage(), request);
    }

    @ExceptionHandler({UserNotFoundException.class, ClientProfileNotFoundException.class, EntityNotFoundException.class})
    public ResponseEntity<ErrorResponseDTO> handleNotFound(RuntimeException ex, HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(EmailAlreadyUsedException.class)
    public ResponseEntity<ErrorResponseDTO> handleEmailAlreadyUsed(
            EmailAlreadyUsedException ex,
            HttpServletRequest request
    ) {
        log.warn("Email conflict at {}", request.getRequestURI());
        return response(HttpStatus.CONFLICT, "EMAIL_ALREADY_USED", "Email уже используется", request);
    }

    @ExceptionHandler(PhoneAlreadyUsedException.class)
    public ResponseEntity<ErrorResponseDTO> handlePhoneAlreadyUsed(
            PhoneAlreadyUsedException ex,
            HttpServletRequest request
    ) {
        log.warn("Phone conflict at {}", request.getRequestURI());
        return response(HttpStatus.CONFLICT, "PHONE_ALREADY_USED", "Телефон уже используется", request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleDataConflict(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {
        log.warn("Data conflict at {}", request.getRequestURI());
        return response(HttpStatus.CONFLICT, "DATA_CONFLICT",
                "Операция нарушает ограничения целостности данных", request);
    }

    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> handlePasswordMismatch(
            PasswordMismatchException ex,
            HttpServletRequest request
    ) {
        return response(HttpStatus.BAD_REQUEST, "PASSWORD_MISMATCH", ex.getMessage(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        return response(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", ex.getMessage(), request);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalState(
            IllegalStateException ex,
            HttpServletRequest request
    ) {
        return response(HttpStatus.CONFLICT, "INVALID_STATE", ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unexpected server error at {} ({})", request.getRequestURI(), ex.getClass().getSimpleName(), ex);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Произошла внутренняя ошибка сервера", request);
    }

    private ResponseEntity<ErrorResponseDTO> response(
            HttpStatus status,
            String errorCode,
            String message,
            HttpServletRequest request
    ) {
        ErrorResponseDTO error = new ErrorResponseDTO(
                LocalDateTime.now(),
                status.value(),
                errorCode,
                message,
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(error);
    }
}
