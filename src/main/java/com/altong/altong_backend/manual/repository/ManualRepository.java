package com.altong.altong_backend.manual.repository;

import com.altong.altong_backend.manual.model.Manual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ManualRepository extends JpaRepository<Manual, Long> {
    Optional<Manual> findByTraining_Id(Long trainingId);
}