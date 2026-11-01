package com.jewelry.workshop.service.interfaces;

import com.jewelry.workshop.domain.model.dto.report.AuditLogDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface AdminService {
    Map<String, Object> getSystemStats();
    Page<AuditLogDTO> getAuditLogs(Pageable pageable);
}