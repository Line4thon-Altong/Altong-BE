package com.altong.altong_backend.employee.controller;

import com.altong.altong_backend.employee.dto.request.EmployeeAddRequest;
import com.altong.altong_backend.employee.dto.response.EmployeeAddResponse;
import com.altong.altong_backend.employee.service.EmployeeManageService;
import com.altong.altong_backend.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeManageController {

    private final EmployeeManageService employeeManageService;

    // 알바생 추가
    @PostMapping
    public ApiResponse<EmployeeAddResponse> addEmployee(
            @RequestHeader("Authorization") String token,
            @RequestBody @Valid EmployeeAddRequest req
    ) {
        return ApiResponse.success(employeeManageService.addEmployee(token, req));
    }
}
