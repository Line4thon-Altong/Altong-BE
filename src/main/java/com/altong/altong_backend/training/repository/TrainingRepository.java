package com.altong.altong_backend.training.repository;

import com.altong.altong_backend.store.model.Store;
import com.altong.altong_backend.training.model.Training;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingRepository extends JpaRepository<Training, Long> {
    List<Training> findByStore(Store store);
}