package com.jewelry.workshop.domain.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "clients")
@Getter
@Setter
@ToString(exclude = "orders")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "patronymic", nullable = true, length = 50)
    private String patronymic;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "is_permanent")
    private Boolean isPermanent = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
    private Set<Order> orders = new HashSet<>();

    public String getFullName(){
        return String.format("%s %s", lastName, firstName) + (patronymic == null || patronymic.isEmpty() ? "": " " + patronymic);
    }

    public boolean isPermanentClient(){
        return Boolean.TRUE.equals(isPermanent);
    }

}
