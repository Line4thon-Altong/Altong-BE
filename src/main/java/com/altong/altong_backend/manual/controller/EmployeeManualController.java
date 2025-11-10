package com.altong.altong_backend.manual.controller;

import com.altong.altong_backend.global.response.ApiResponse;
import com.altong.altong_backend.manual.dto.response.ManualDetailResponse;
import com.altong.altong_backend.manual.service.EmployeeManualService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employees/trainings")
public class EmployeeManualController {

    private final EmployeeManualService employeeManualService;

    // 알바생용 메뉴얼 조회
    @GetMapping("/{trainingId}/manuals")
    public ResponseEntity<ApiResponse<ManualDetailResponse>> getManualForEmployee(
            @RequestHeader("Authorization") String token,
            @PathVariable Long trainingId
    ) {
        ManualDetailResponse response = employeeManualService.getManualByTrainingId(token, trainingId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}