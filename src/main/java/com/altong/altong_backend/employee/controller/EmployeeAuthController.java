package com.altong.altong_backend.employee.controller;

import com.altong.altong_backend.global.response.ApiResponse;
import com.altong.altong_backend.employee.service.EmployeeAuthService;
import com.altong.altong_backend.employee.dto.request.EmployeeLoginRequest;
import com.altong.altong_backend.employee.dto.request.EmployeePasswordUpdateRequest;
import com.altong.altong_backend.employee.dto.request.EmployeeUnlinkStoreRequest;
import com.altong.altong_backend.employee.dto.request.EmployeeLogoutRequest;
import com.altong.altong_backend.employee.dto.response.EmployeeLoginResponse;
import com.altong.altong_backend.employee.dto.response.EmployeePasswordUpdateResponse;
import com.altong.altong_backend.employee.dto.response.EmployeeLogoutResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeAuthController {

    private final EmployeeAuthService service;

    @PostMapping("/login")
    public ApiResponse<EmployeeLoginResponse> login(@RequestBody @Valid EmployeeLoginRequest req) {
        return ApiResponse.success(service.login(req));
    }

    @PatchMapping("/password")
    public ApiResponse<EmployeePasswordUpdateResponse> updatePassword(
            @RequestHeader("X-EMPLOYEE-ID") Long empId,
            @RequestBody @Valid EmployeePasswordUpdateRequest req
    ) {
        return ApiResponse.success(service.updatePassword(empId, req));
    }

    @DeleteMapping("/{employeeId}/unlink-store")
    public ApiResponse<EmployeePasswordUpdateResponse> unlinkStore(
            @PathVariable Long employeeId,
            @RequestParam Long storeId
    ) {
        EmployeeUnlinkStoreRequest req = new EmployeeUnlinkStoreRequest(employeeId, storeId);
        return ApiResponse.success(service.unlinkStore(req));
    }

    @PostMapping("/logout")
    public ApiResponse<EmployeeLogoutResponse> logout(@RequestBody @Valid EmployeeLogoutRequest req) {
        return ApiResponse.success(service.logout(req));
    }
}
