package com.jewelry.workshop.presentation.controller;

import com.jewelry.workshop.domain.model.dto.user.*;
import com.jewelry.workshop.presentation.exception.error.ErrorResponseDTO;
import com.jewelry.workshop.security.auth.UserDetailsImpl;
import com.jewelry.workshop.service.interfaces.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@Tag(name = "Пользователи", description = "Управление пользователями (только для SELLER и ADMIN)")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить список всех пользователей")
    @ApiResponse(responseCode = "200", description = "Список пользователей",
            content = @Content(schema = @Schema(implementation = UserResponseDTO.class)))
    @ApiResponse(responseCode = "403", description = "Доступ запрещён",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    public ResponseEntity<Page<UserResponseDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<UserResponseDTO> users = userService.getAllUsers(PageRequest.of(page, size));
        return ResponseEntity.ok(users);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Создать нового пользователя")
    @ApiResponse(responseCode = "201", description = "Пользователь успешно создан",
            content = @Content(schema = @Schema(implementation = UserResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Некорректные данные или конфликт (email/username заняты)",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "403", description = "Доступ запрещён",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserCreateDTO dto) {
        UserResponseDTO created = userService.createUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @Operation(summary = "Получить детали пользователя по ID")
    @ApiResponse(responseCode = "200", description = "Данные пользователя",
            content = @Content(schema = @Schema(implementation = UserResponseDTO.class)))
    @ApiResponse(responseCode = "403", description = "Доступ запрещён",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Пользователь не найден",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        UserResponseDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Обновить пользователя")
    @ApiResponse(responseCode = "200", description = "Пользователь успешно обновлён",
            content = @Content(schema = @Schema(implementation = UserResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Некорректные данные или конфликт",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "403", description = "Доступ запрещён",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Пользователь не найден",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateDTO dto
    ) {
        UserResponseDTO updated = userService.updateUser(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Деактивировать пользователя (soft delete)")
    @ApiResponse(responseCode = "200", description = "Пользователь деактивирован",
            content = @Content(schema = @Schema(implementation = UserDeleteResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Попытка удалить самого себя",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "403", description = "Доступ запрещён",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Пользователь не найден",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    public ResponseEntity<UserDeleteResponseDTO> deleteUser(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long currentUserId = ((UserDetailsImpl) authentication.getPrincipal()).getId();

        UserDeleteResponseDTO response = userService.deleteUser(id, currentUserId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Изменить роль пользователя")
    @ApiResponse(responseCode = "200", description = "Роль успешно изменена",
            content = @Content(schema = @Schema(implementation = UserResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Недопустимая роль",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "403", description = "Доступ запрещён",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Пользователь не найден",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    public ResponseEntity<UserResponseDTO> updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody UserRoleUpdateDTO dto
    ) {
        UserResponseDTO updated = userService.updateUserRole(id, dto.getRole());
        return ResponseEntity.ok(updated);
    }
}