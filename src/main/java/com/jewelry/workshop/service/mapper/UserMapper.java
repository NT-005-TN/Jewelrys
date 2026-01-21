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

        return new UserResponseDTO() {{
            setId(user.getId());
            setUsername(user.getUsername());
            setEmail(user.getEmail());
            setRole(user.getRole().name());
            setEnabled(user.isEnabled());
            setCreatedAt(toLocalDateTime(user.getCreatedAt()));
            setUpdatedAt(toLocalDateTime(user.getUpdatedAt()));
            setClientId(clientId);
            setEmployeeId(employeeId);
        }};
    }

    private LocalDateTime toLocalDateTime(Instant instant) {
        return instant != null ? LocalDateTime.ofInstant(instant, ZoneId.systemDefault()) : null;
    }
}