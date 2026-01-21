package com.jewelry.workshop.domain.model.entity;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.core.util.Json;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@ToString()
@Getter
@Setter
@Table(name = "audit_log")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "action", length = 20, nullable = false)
    private String action;

    @Column(name = "table_name", length = 50, nullable = false)
    private String tableName;

    @Column(name = "record_id", nullable = false)
    private Long recordId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "old_values", columnDefinition = "json")
    private JsonNode oldValues;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "new_values", columnDefinition = "json")
    private JsonNode newValues;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    public String getLog(){
        return String.format(
                "user: %s, action: %s, time: %s, table: %s, record_id: %d",
                user != null ? user.getUsername() : "unknown",
                action,
                createdAt,
                tableName,
                recordId);
    }

}

