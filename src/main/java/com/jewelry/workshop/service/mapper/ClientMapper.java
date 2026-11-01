package com.jewelry.workshop.service.mapper;

import com.jewelry.workshop.domain.model.dto.client.ClientResponseDTO;
import com.jewelry.workshop.domain.model.entity.Client;
import com.jewelry.workshop.domain.model.entity.User;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class ClientMapper {
    public ClientResponseDTO toDto(Client client){
        if(client == null) return null;
        User user = client.getUser();
        return ClientResponseDTO.builder()
                .id(client.getId())
                .firstName(client.getFirstName())
                .lastName(client.getLastName())
                .patronymic(client.getPatronymic())
                .email(user.getEmail())
                .phone(client.getPhone())
                .isPermanent(client.getIsPermanent())
                .fullName(client.getFullName())
                .userId(user.getId())
                .createdAt(toLocalDateTime(client.getCreatedAt()))
                .updatedAt(toLocalDateTime(client.getUpdatedAt()))
                .build();
    }

    private LocalDateTime toLocalDateTime(Instant instant){
        return instant != null ? LocalDateTime.ofInstant(instant, ZoneId.systemDefault()) : null;
    }
}
