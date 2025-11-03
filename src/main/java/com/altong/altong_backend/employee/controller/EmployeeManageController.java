package com.altong.altong_backend.employee.controller;

import com.altong.altong_backend.employee.dto.request.EmployeeAddRequest;
import com.altong.altong_backend.employee.dto.response.EmployeeAddResponse;
import com.altong.altong_backend.employee.dto.response.EmployeeDeleteResponse;
import com.altong.altong_backend.employee.dto.response.EmployeeListResponse;
import com.altong.altong_backend.employee.service.EmployeeManageService;
import com.altong.altong_backend.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeManageController {

    private final EmployeeManageService employeeManageService;

    // 알바생 추가
    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeAddResponse>> addEmployee(
            @RequestHeader("Authorization") String token,
            @RequestBody @Valid EmployeeAddRequest req
    ) {
        EmployeeAddResponse response = employeeManageService.addEmployee(token, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));    }

    // 알바생 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<EmployeeListResponse>>> getEmployees(
            @RequestHeader("Authorization") String token
    ) {
        List<EmployeeListResponse> employees = employeeManageService.getEmployees(token);
        return ResponseEntity.ok(ApiResponse.success(employees));    }

    // 알바생 삭제
    @DeleteMapping("/{employeeId}")
    public ResponseEntity<ApiResponse<EmployeeDeleteResponse>> deleteEmployee(
            @RequestHeader("Authorization") String token,
            @PathVariable Long employeeId
    ) {
        EmployeeDeleteResponse response = employeeManageService.deleteEmployee(token, employeeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
