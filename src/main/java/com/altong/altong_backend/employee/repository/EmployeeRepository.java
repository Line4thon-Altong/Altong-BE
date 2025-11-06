package com.altong.altong_backend.employee.repository;

import com.altong.altong_backend.employee.model.Employee;
import com.altong.altong_backend.store.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    boolean existsByUsername(String username);
    Optional<Employee> findByUsername(String username);
    List<Employee> findByStore(Store store);
}
