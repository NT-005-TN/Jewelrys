package com.jewelry.workshop.presentation.controller;

import com.jewelry.workshop.domain.model.dto.order.OrderCreateDTO;
import com.jewelry.workshop.domain.model.dto.order.OrderResponseDTO;
import com.jewelry.workshop.domain.model.dto.order.OrderStatusUpdateDTO;
import com.jewelry.workshop.domain.model.dto.report.DailyOrderStatsDTO;
import com.jewelry.workshop.domain.model.entity.Client;
import com.jewelry.workshop.domain.repository.ClientRepository;
import com.jewelry.workshop.presentation.exception.error.ErrorResponseDTO;
import com.jewelry.workshop.security.auth.UserDetailsImpl;
import com.jewelry.workshop.service.interfaces.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@Tag(name = "Заказы", description = "Управление заказами клиента")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final ClientRepository clientRepository;

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @Operation(summary = "Получить список текущих (активных) заказов клиента")
    @ApiResponse(responseCode = "200", description = "Список заказов успешно получен",
            content = @Content(schema = @Schema(implementation = OrderResponseDTO.class)))
    @ApiResponse(responseCode = "401", description = "Токен недействителен",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "403", description = "Доступ запрещён",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    public ResponseEntity<Page<OrderResponseDTO>> getMyOrders(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long clientId = getClientId(userDetails);
        Page<OrderResponseDTO> orders = orderService.getMyOrders(clientId, page, size);
        return ResponseEntity.ok(orders);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @Operation(summary = "Создать новый заказ")
    @ApiResponse(responseCode = "201", description = "Заказ успешно создан",
            content = @Content(schema = @Schema(implementation = OrderResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Ошибка валидации или недостаточно товара на складе",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "401/403", description = "Ошибка аутентификации/авторизации",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    public ResponseEntity<OrderResponseDTO> createOrder(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody OrderCreateDTO dto
    ) {
        Long clientId = getClientId(userDetails);
        OrderResponseDTO order = orderService.createOrder(clientId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @Operation(summary = "Отменить свой заказ")
    @ApiResponse(responseCode = "200", description = "Заказ успешно отменён")
    @ApiResponse(responseCode = "400", description = "Невозможно отменить заказ с текущим статусом",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "403/404", description = "Доступ запрещён или заказ не найден",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    public ResponseEntity<String> cancelOrder(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long id
    ) {
        Long clientId = getClientId(userDetails);
        orderService.cancelOrder(id, clientId);
        return ResponseEntity.ok("Заказ успешно отменён");
    }

    @GetMapping("/my/history")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @Operation(summary = "Получить историю всех своих заказов (включая отменённые)")
    @ApiResponse(
            responseCode = "200",
            description = "История заказов успешно получена",
            content = @Content(schema = @Schema(implementation = OrderResponseDTO.class)))
    @ApiResponse(
            responseCode = "401/403",
            description = "Ошибка аутентификации/авторизации",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    public ResponseEntity<Page<OrderResponseDTO>> getOrderHistory(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long clientId = getClientId(userDetails);
        Page<OrderResponseDTO> orders = orderService.getOrderHistory(clientId, page, size);
        return ResponseEntity.ok(orders);
    }

    private Long getClientId(UserDetailsImpl userDetails) {
        return clientRepository.findByUserId(userDetails.getId())
                .map(Client::getId)
                .orElseThrow(() -> new RuntimeException("Профиль клиента не найден"));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить список всех заказов (только для ADMIN)")
    @ApiResponse(responseCode = "200", description = "Список всех заказов успешно получен",
            content = @Content(schema = @Schema(implementation = OrderResponseDTO.class)))
    @ApiResponse(responseCode = "403", description = "Доступ запрещён (требуется роль ADMIN)",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    public ResponseEntity<Page<OrderResponseDTO>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderResponseDTO> orders = orderService.getAllOrders(pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'SELLER', 'ADMIN')")
    @Operation(summary = "Получить детали заказа по ID")
    @ApiResponse(responseCode = "200", description = "Детали заказа успешно получены",
            content = @Content(schema = @Schema(implementation = OrderResponseDTO.class)))
    @ApiResponse(responseCode = "403", description = "Доступ запрещён (клиент пытается получить чужой заказ)",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Заказ не найден",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    public ResponseEntity<OrderResponseDTO> getOrderById(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long id
    ) {
        String role = userDetails.getUserRole();
        Long clientId = null;

        if ("CLIENT".equals(role)) {
            clientId = clientRepository.findByUserId(userDetails.getId())
                    .map(Client::getId)
                    .orElseThrow(() -> new RuntimeException("Профиль клиента не найден"));
        }

        OrderResponseDTO order = orderService.getOrderById(id, clientId);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @Operation(summary = "Изменить статус заказа")
    @ApiResponse(responseCode = "200", description = "Статус заказа успешно обновлён")
    @ApiResponse(responseCode = "400", description = "Недопустимый статус или недопустимый переход",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "403", description = "Доступ запрещён (только SELLER/ADMIN)",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Заказ не найден",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    public ResponseEntity<String> updateOrderStatus(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateDTO dto
    ) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID заказа должен быть положительным числом");
        }
        orderService.changeStatus(id, dto.getStatus(), userDetails.getId());
        return ResponseEntity.ok("Статус заказа #" + id + " успешно обновлён на '" + dto.getStatus() + "'");
    }

    @GetMapping("/stats/daily")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @Operation(summary = "Ежедневная статистика заказов")
    @ApiResponse(responseCode = "200", description = "Статистика успешно получена",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = DailyOrderStatsDTO.class))))
    public ResponseEntity<List<DailyOrderStatsDTO>> getDailyOrderStats(
            @RequestParam(defaultValue = "7") Integer days
    ) {
        List<DailyOrderStatsDTO> stats = orderService.getDailyOrderStats(days);
        return ResponseEntity.ok(stats);
    }
}