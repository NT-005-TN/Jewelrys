package com.jewelry.workshop.domain.repository;

import com.jewelry.workshop.domain.model.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
