package com.altong.altong_backend.store.repository;

import com.altong.altong_backend.owner.model.Owner;
import com.altong.altong_backend.store.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {
    boolean existsByName(String name);

    Optional<Store> findByOwner(Owner owner);
}
