package com.jewelry.workshop.service.interfaces;

import com.jewelry.workshop.domain.model.dto.client.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClientService {
    ClientProfileDTO getOwnProfile(Long userId);
    ClientProfileDTO updateOwnProfile(Long userId, ClientUpdateDTO dto);
    void changePassword(Long userId, PasswordChangeDTO dto);

    Page<ClientResponseDTO> getAllClients(Pageable pageable);
    ClientResponseDTO getClientById(Long clientId);
    Page<ClientResponseDTO> searchClients(
            String lastName,
            String firstName,
            String phone,
            Boolean isPermanent,
            Integer minOrders,
            String sortBy,
            int page,
            int size
    );

    void setClientPermanent(Long clientId, boolean isPermanent);
}
