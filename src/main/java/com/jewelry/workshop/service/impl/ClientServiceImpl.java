package com.jewelry.workshop.service.impl;

import com.jewelry.workshop.domain.model.dto.client.*;
import com.jewelry.workshop.domain.model.entity.Client;
import com.jewelry.workshop.domain.model.entity.User;
import com.jewelry.workshop.domain.repository.ClientRepository;
import com.jewelry.workshop.domain.repository.OrderRepository;
import com.jewelry.workshop.domain.repository.UserRepository;
import com.jewelry.workshop.presentation.exception.*;
import com.jewelry.workshop.service.interfaces.ClientService;
import com.jewelry.workshop.service.mapper.ClientMapper;
import com.jewelry.workshop.util.PasswordUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final PasswordUtil passwordUtil;
    private final ClientMapper clientMapper;
    private final StringRedisTemplate redisTemplate;

    @Override
    public ClientProfileDTO getOwnProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        if (!user.getRole().equals(User.Role.CLIENT)) {
            throw new UnauthorizedAccessException("Только клиенты могут просматривать профиль");
        }

        Client client = clientRepository.findByUserId(user.getId())
                .orElseThrow(ClientProfileNotFoundException::new);

        Long orderCount = orderRepository.countOrdersByClientId(client.getId());
        BigDecimal totalSpent = orderRepository.getTotalSpentByClientId(client.getId());
        LocalDateTime lastOrderDate = orderRepository.getLastOrderDateByClientId(client.getId());

        return ClientProfileDTO.builder()
                .firstName(client.getFirstName())
                .lastName(client.getLastName())
                .patronymic(client.getPatronymic())
                .email(user.getEmail())
                .phone(client.getPhone())
                .fullName(client.getFullName())
                .orderCount(orderCount)
                .totalSpent(totalSpent)
                .lastOrderDate(lastOrderDate)
                .build();
    }

    @Override
    public ClientProfileDTO updateOwnProfile(Long userId, ClientUpdateDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        if (!user.getRole().equals(User.Role.CLIENT)) {
            throw new UnauthorizedAccessException("Только клиенты могут обновлять профиль");
        }

        Client client = clientRepository.findByUserId(user.getId())
                .orElseThrow(ClientProfileNotFoundException::new);

        if (dto.getEmail() != null && !dto.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmail(dto.getEmail())) {
                throw new EmailAlreadyUsedException(dto.getEmail());
            }
            user.setEmail(dto.getEmail());
        }

        if (dto.getPhone() != null && !dto.getPhone().equals(client.getPhone())) {
            if (clientRepository.findByPhone(dto.getPhone()).isPresent()) {
                throw new PhoneAlreadyUsedException(dto.getPhone());
            }
            client.setPhone(dto.getPhone());
        }

        if (dto.getFirstName() != null) client.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) client.setLastName(dto.getLastName());
        client.setPatronymic(dto.getPatronymic());

        userRepository.save(user);
        clientRepository.save(client);

        Long orderCount = orderRepository.countOrdersByClientId(client.getId());
        BigDecimal totalSpent = orderRepository.getTotalSpentByClientId(client.getId());
        LocalDateTime lastOrderDate = orderRepository.getLastOrderDateByClientId(client.getId());

        return ClientProfileDTO.builder()
                .firstName(client.getFirstName())
                .lastName(client.getLastName())
                .patronymic(client.getPatronymic())
                .email(user.getEmail())
                .phone(client.getPhone())
                .fullName(client.getFullName())
                .orderCount(orderCount)
                .totalSpent(totalSpent)
                .lastOrderDate(lastOrderDate)
                .build();
    }

    @Override
    @Transactional
    public void changePassword(Long userId, PasswordChangeDTO dto){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        if(!user.getRole().equals(User.Role.CLIENT)){
            throw new UnauthorizedAccessException("Только клиенты могут менять пароль");
        }

        if (!passwordUtil.matches(dto.getCurrentPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Текущий пароль неверен");
        }

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new PasswordMismatchException();
        }

        if (passwordUtil.matches(dto.getNewPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Новый пароль не может совпадать со старым");
        }

        user.setPasswordHash(passwordUtil.encode(dto.getNewPassword()));
        userRepository.save(user);

        redisTemplate.delete("refresh_token:" + user.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClientResponseDTO> getAllClients(Pageable pageable){
        Page<Client> clients = clientRepository.findAll(pageable);

        List<ClientResponseDTO> dtos = clients.getContent().stream()
                .map(clientMapper::toDto)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, clients.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public ClientResponseDTO getClientById(Long clientId){
        if (clientId == null || clientId <= 0) {
            throw new IllegalArgumentException("ID клиента должен быть положительным числом");
        }

        Optional<Client> client = clientRepository.findById(clientId);

        if(client.isEmpty()){
            throw new EntityNotFoundException("Клиент не найден");
        }

        return clientMapper.toDto(client.get());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClientResponseDTO> searchClients(
            String lastName,
            String firstName,
            String phone,
            Boolean isPermanent,
            Integer minOrders,
            String sortBy,
            int page,
            int size
    ) {
        if (sortBy != null && !List.of("name", "created", "orders").contains(sortBy)) {
            throw new IllegalArgumentException("Недопустимое значение sortBy: " + sortBy);
        }

        Pageable pageable = PageRequest.of(page, size);

        String phonePattern = phone != null ? "%" + phone + "%" : null;

        Page<Client> clients = clientRepository.findClientsByCriteria(
                lastName,
                firstName,
                phonePattern,
                isPermanent,
                minOrders,
                sortBy,
                pageable
        );

        List<ClientResponseDTO> dtos = clients.getContent().stream()
                .map(clientMapper::toDto)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, clients.getTotalElements());
    }
    @Override
    @Transactional
    public void setClientPermanent(Long clientId, boolean isPermanent) {
        if (clientId == null || clientId <= 0) {
            throw new IllegalArgumentException("ID клиента должен быть положительным числом");
        }

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Клиент не найден"));

        if (client.isPermanentClient() == isPermanent) {
            return;
        }

        client.setIsPermanent(isPermanent);
        clientRepository.save(client);
    }
}