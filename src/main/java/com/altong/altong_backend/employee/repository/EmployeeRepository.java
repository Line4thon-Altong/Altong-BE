package com.altong.altong_backend.employee.repository;

import com.altong.altong_backend.employee.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    boolean existsByUsername(String username);
    Optional<Employee> findByUsername(String username);
}
