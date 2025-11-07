package com.altong.altong_backend.manual.repository;

import com.altong.altong_backend.manual.model.Manual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ManualRepository extends JpaRepository<Manual, Long> {
}