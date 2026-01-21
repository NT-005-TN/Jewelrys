package com.jewelry.workshop.domain.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "employees")
@Getter
@Setter
@ToString(exclude = "user")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "position", length = 50)
    private String position;

    @Column(name = "department", length = 50)
    private String department;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public String getFullInfo() {
        return String.format("%s %s - %s (%s)",
                user != null ? user.getUsername() : "Неизвестно",
                position != null ? position : "Без должности",
                department != null ? department : "Без отдела");
    }

    public boolean isInDepartment(String departmentName) {
        return department != null && department.equalsIgnoreCase(departmentName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        return id != null && id.equals(employee.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}