package com.jewelry.workshop.service.impl;

import com.jewelry.workshop.domain.model.dto.report.AuditLogDTO;
import com.jewelry.workshop.domain.model.dto.user.UserImportDTO;
import com.jewelry.workshop.domain.model.entity.User;
import com.jewelry.workshop.domain.repository.*;
import com.jewelry.workshop.service.interfaces.AdminService;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Map<String, Object> getSystemStats() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByEnabled(true);
        long totalClients = clientRepository.count();
        long permanentClients = clientRepository.countPermanentClients();
        long totalProducts = productRepository.count();
        int lowStockProducts = productRepository.findLowStockProducts(5).size();
        long totalOrders = orderRepository.count();
        long pendingOrders = orderRepository.findByStatus("PENDING").size();

        return Map.of(
                "totalUsers", totalUsers,
                "activeUsers", activeUsers,
                "totalClients", totalClients,
                "permanentClients", permanentClients,
                "totalProducts", totalProducts,
                "lowStockProducts", lowStockProducts,
                "totalOrders", totalOrders,
                "pendingOrders", pendingOrders
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDTO> getAuditLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable)
                .map(log -> new AuditLogDTO(
                        log.getId(),
                        log.getUser().getUsername(),
                        log.getAction(),
                        log.getTableName(),
                        log.getRecordId(),
                        log.getIpAddress(),
                        log.getCreatedAt()
                ));
    }
}