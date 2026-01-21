package com.jewelry.workshop.domain.repository;

import com.jewelry.workshop.domain.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    Optional<User> findByVerificationToken(String verificationToken);
    Optional<User> findByPasswordResetToken(String passwordResetToken);

    long countByEnabled(Boolean enabled);
}
