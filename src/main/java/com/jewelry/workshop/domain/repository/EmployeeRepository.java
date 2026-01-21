package com.jewelry.workshop.domain.repository;

import com.jewelry.workshop.domain.model.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}


