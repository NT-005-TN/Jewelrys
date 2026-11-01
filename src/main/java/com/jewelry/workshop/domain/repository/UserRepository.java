package com.jewelry.workshop.domain.repository;

import com.jewelry.workshop.domain.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByEmailIgnoreCase(String email);

    Optional<User> findByVerificationToken(String verificationToken);
    Optional<User> findByPasswordResetToken(String passwordResetToken);

    long countByEnabled(Boolean enabled);
    long countByRoleAndEnabled(User.Role role, Boolean enabled);
}
