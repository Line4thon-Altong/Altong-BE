package com.altong.altong_backend.employee.controller;

import com.altong.altong_backend.employee.dto.request.EmployeeAddRequest;
import com.altong.altong_backend.employee.dto.response.EmployeeAddResponse;
import com.altong.altong_backend.employee.dto.response.EmployeeDeleteResponse;
import com.altong.altong_backend.employee.dto.response.EmployeeListResponse;
import com.altong.altong_backend.employee.service.EmployeeManageService;
import com.altong.altong_backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Employee Management (사장님용 알바생 관리)", description = "사장님이 알바생을 추가, 조회, 삭제하는 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeManageController {

    private final EmployeeManageService employeeManageService;

    // 알바생 추가
    @Operation(summary = "알바생 추가", description = "사장님이 새로운 알바생을 매장에 등록합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeAddResponse>> addEmployee(
            @RequestHeader("Authorization") String token,
            @RequestBody @Valid EmployeeAddRequest req
    ) {
        EmployeeAddResponse response = employeeManageService.addEmployee(token, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));    }

    // 알바생 목록 조회
    @Operation(summary = "알바생 목록 조회", description = "현재 매장에 등록된 모든 알바생 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<EmployeeListResponse>>> getEmployees(
            @RequestHeader("Authorization") String token
    ) {
        List<EmployeeListResponse> employees = employeeManageService.getEmployees(token);
        return ResponseEntity.ok(ApiResponse.success(employees));    }

    // 알바생 삭제
    @Operation(summary = "알바생 삭제", description = "특정 알바생을 매장에서 제거합니다.")
    @DeleteMapping("/{employeeId}")
    public ResponseEntity<ApiResponse<EmployeeDeleteResponse>> deleteEmployee(
            @RequestHeader("Authorization") String token,
            @PathVariable Long employeeId
    ) {
        EmployeeDeleteResponse response = employeeManageService.deleteEmployee(token, employeeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
