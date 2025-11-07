package com.altong.altong_backend.training.controller;

import com.altong.altong_backend.global.response.ApiResponse;
import com.altong.altong_backend.training.dto.response.EmployeeDashboardResponse;
import com.altong.altong_backend.training.dto.response.OwnerDashboardResponse;
import com.altong.altong_backend.training.service.TrainingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trainings/dashboard")
@RequiredArgsConstructor
public class TrainingController {

    private final TrainingService trainingService;

    // 사장님용 대시보드
    @GetMapping("/owner")
    public ResponseEntity<ApiResponse<OwnerDashboardResponse>> getOwnerDashboard(
            @RequestHeader("Authorization") String token
    ) {
        OwnerDashboardResponse response = trainingService.getOwnerDashboard(token);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 알바생용 대시보드
    @GetMapping("/employee")
    public ResponseEntity<ApiResponse<EmployeeDashboardResponse>> getEmployeeDashboard(
            @RequestHeader("Authorization") String token
    ) {
        EmployeeDashboardResponse response = trainingService.getEmployeeDashboard(token);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
