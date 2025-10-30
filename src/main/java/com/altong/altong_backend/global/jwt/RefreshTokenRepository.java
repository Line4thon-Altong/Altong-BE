package com.altong.altong_backend.global.jwt;

import org.springframework.data.repository.CrudRepository;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
    void deleteByUsername(String username);
}
