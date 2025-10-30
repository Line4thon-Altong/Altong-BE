package com.altong.altong_backend.store.repository;

import com.altong.altong_backend.store.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, Long> {
    boolean existsByName(String name);
}
