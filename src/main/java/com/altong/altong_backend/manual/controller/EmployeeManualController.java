package com.altong.altong_backend.manual.controller;

import com.altong.altong_backend.global.response.ApiResponse;
import com.altong.altong_backend.manual.dto.response.ManualDetailResponse;
import com.altong.altong_backend.manual.service.EmployeeManualService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Manual (알바생용 메뉴얼)", description = "알바생이 자신이 속한 교육의 메뉴얼을 조회하는 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employees/trainings")
public class EmployeeManualController {

    private final EmployeeManualService employeeManualService;

    // 알바생용 메뉴얼 조회
    @Operation(summary = "알바생용 메뉴얼 조회", description = "알바생이 속한 교육(training)의 메뉴얼 내용을 조회합니다.")
    @GetMapping("/{trainingId}/manuals")
    public ResponseEntity<ApiResponse<ManualDetailResponse>> getManualForEmployee(
            @RequestHeader("Authorization") String token,
            @PathVariable Long trainingId
    ) {
        ManualDetailResponse response = employeeManualService.getManualByTrainingId(token, trainingId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}