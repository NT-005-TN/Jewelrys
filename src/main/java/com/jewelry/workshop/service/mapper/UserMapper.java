package com.jewelry.workshop.service.mapper;

import com.jewelry.workshop.domain.model.dto.user.UserResponseDTO;
import com.jewelry.workshop.domain.model.entity.User;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class UserMapper {

    public UserResponseDTO toDto(User user, Long clientId, Long employeeId) {
        if (user == null) return null;

        UserResponseDTO response = new UserResponseDTO();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().name());
        response.setEnabled(user.isEnabled());
        response.setCreatedAt(toLocalDateTime(user.getCreatedAt()));
        response.setUpdatedAt(toLocalDateTime(user.getUpdatedAt()));
        response.setClientId(clientId);
        response.setEmployeeId(employeeId);
        return response;
    }

    private LocalDateTime toLocalDateTime(Instant instant) {
        return instant != null ? LocalDateTime.ofInstant(instant, ZoneId.systemDefault()) : null;
    }
}
