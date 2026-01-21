package com.jewelry.workshop.presentation.controller;

import com.jewelry.workshop.domain.model.dto.client.*;
import com.jewelry.workshop.presentation.exception.error.ErrorResponseDTO;
import com.jewelry.workshop.security.auth.UserDetailsImpl;
import com.jewelry.workshop.service.interfaces.ClientService;
import com.jewelry.workshop.util.RateLimit;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/clients")
@Tag(name = "Клиенты", description = "Управление профилем клиента")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @GetMapping("/me")
    @Operation(summary = "Просмотреть свой профиль")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @ApiResponse(responseCode = "200", description = "Профиль успешно получен",
            content = @Content(schema = @Schema(implementation = ClientProfileDTO.class)))
    @ApiResponse(responseCode = "401", description = "Токен отсутствует или недействителен",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "403", description = "Доступ запрещён (пользователь не является клиентом)",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Профиль клиента не найден",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    public ResponseEntity<ClientProfileDTO> getOwnProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long userId = userDetails.getId();
        ClientProfileDTO profile = clientService.getOwnProfile(userId);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/me")
    @Operation(summary = "Обновить свой профиль")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @ApiResponse(responseCode = "200", description = "Профиль успешно обновлён",
            content = @Content(schema = @Schema(implementation = ClientProfileDTO.class)))
    @ApiResponse(responseCode = "400", description = "Некорректные данные (валидация, email/телефон уже используются)",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "401", description = "Токен отсутствует или недействителен",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "403", description = "Доступ запрещён",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Профиль клиента не найден",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    public ResponseEntity<ClientProfileDTO> updateOwnProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody ClientUpdateDTO dto
    ) {
        Long userId = userDetails.getId();
        ClientProfileDTO updated = clientService.updateOwnProfile(userId, dto);
        return ResponseEntity.ok(updated);
    }


    @PatchMapping("/me/password")
    @RateLimit(maxAttempts = 2, windowMinutes = 1440, keyPrefix = "change_password")
    @Operation(summary = "Сменить пароль")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @ApiResponse(responseCode = "200", description = "Пароль успешно изменён")
    @ApiResponse(responseCode = "400", description = "Ошибки валидации или логики (неверный пароль, совпадение и т.д.)",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "401", description = "Токен недействителен",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "403", description = "Доступ запрещён",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    public ResponseEntity<String> changePassword(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody PasswordChangeDTO dto
    ) {
        Long userId = userDetails.getId();
        clientService.changePassword(userId, dto);
        return ResponseEntity.ok("Пароль успешно изменён");
    }

    @GetMapping
    @Operation(summary = "Получить список всех клиентов")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @ApiResponse(responseCode = "200", description = "Список клиентов успешно получен",
            content = @Content(schema = @Schema(implementation = ClientResponseDTO.class)))
    @ApiResponse(responseCode = "403", description = "Доступ запрещен",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    public ResponseEntity<Page<ClientResponseDTO>> getAllClients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        Page<ClientResponseDTO> clients = clientService.getAllClients(PageRequest.of(page, size));
        return ResponseEntity.ok(clients);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить детали клиента")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @ApiResponse(
            responseCode = "200",
            description = "Детали клиента успешно получены",
            content = @Content(schema = @Schema(implementation = ClientResponseDTO.class))
    )
    @ApiResponse(
            responseCode = "401",
            description = "Неавторизованный доступ: токен отсутствует, недействителен или просрочен",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "Клиент с указанным ID не найдено",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
    )
    public ResponseEntity<ClientResponseDTO> getClientById(
            @PathVariable Long id
    ) {
        ClientResponseDTO client = clientService.getClientById(id);
        return ResponseEntity.ok(client);
    }


    @GetMapping("/search")
    @Operation(summary = "Поиск клиентов по критериям")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @ApiResponse(
            responseCode = "200",
            description = "Список клиентов успешно получен",
            content = @Content(schema = @Schema(implementation = ClientResponseDTO.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Некорректные параметры поиска",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
    )
    @ApiResponse(
            responseCode = "403",
            description = "Доступ запрещён",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
    )
    public ResponseEntity<Page<ClientResponseDTO>> searchClients(
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Boolean isPermanent,
            @RequestParam(required = false) Integer minOrders,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<ClientResponseDTO> clients = clientService.searchClients(
                lastName, firstName, phone, isPermanent, minOrders, sortBy, page, size
        );
        return ResponseEntity.ok(clients);
    }

    @PutMapping("/{id}/permanent")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @Operation(summary = "Обновить статус клиента на постоянного")
    @ApiResponse(responseCode = "200", description = "Статус успешно обновлён")
    @ApiResponse(responseCode = "400", description = "Некорректный ID клиента",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "403", description = "Доступ запрещён (требуется роль SELLER или ADMIN)",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Клиент не найден",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    public ResponseEntity<String> setClientPermanent(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean isPermanent
    ) {
        clientService.setClientPermanent(id, isPermanent);
        String status = isPermanent ? "постоянным" : "обычным";
        return ResponseEntity.ok("Клиент #" + id + " успешно установлен как " + status);
    }
}