package com.altong.altong_backend.owner.controller;

import com.altong.altong_backend.global.response.ApiResponse;
import com.altong.altong_backend.owner.service.OwnerAuthService;

import com.altong.altong_backend.owner.dto.request.OwnerLoginRequest;
import com.altong.altong_backend.owner.dto.request.OwnerPasswordUpdateRequest;
import com.altong.altong_backend.owner.dto.request.OwnerStoreNameUpdateRequest;
import com.altong.altong_backend.owner.dto.request.OwnerLogoutRequest;

import com.altong.altong_backend.owner.dto.response.OwnerLoginResponse;
import com.altong.altong_backend.owner.dto.response.OwnerPasswordUpdateResponse;
import com.altong.altong_backend.owner.dto.response.OwnerLogoutResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/owners")
@RequiredArgsConstructor
public class OwnerAuthController {
    private final OwnerAuthService service;

    @PostMapping("/login")
    public ApiResponse<OwnerLoginResponse> login(@RequestBody @Valid OwnerLoginRequest req){
        return ApiResponse.success(service.login(req));
    }

    @PatchMapping("/password")
    public ApiResponse<OwnerPasswordUpdateResponse> updatePassword(@RequestHeader("X-OWNER-ID") Long ownerId,
                                                                   @RequestBody @Valid OwnerPasswordUpdateRequest req){
        return ApiResponse.success(service.updatePassword(ownerId, req));
    }

    @PatchMapping("/store-name")
    public ApiResponse<OwnerPasswordUpdateResponse> updateStoreName(@RequestHeader("X-OWNER-ID") Long ownerId,
                                                                    @RequestBody @Valid OwnerStoreNameUpdateRequest req){
        return ApiResponse.success(service.updateStoreName(ownerId, req));
    }

    @PostMapping("/logout")
    public ApiResponse<OwnerLogoutResponse> logout(@RequestBody @Valid OwnerLogoutRequest req){
        return ApiResponse.success(service.logout(req));
    }
}
