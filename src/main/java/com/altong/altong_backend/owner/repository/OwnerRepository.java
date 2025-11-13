package com.altong.altong_backend.owner.repository;

import com.altong.altong_backend.owner.model.Owner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OwnerRepository extends JpaRepository<Owner, Long> {
    Optional<Owner> findByUsername(String username);
    boolean existsByUsername(String username);
}