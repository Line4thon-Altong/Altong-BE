package com.altong.altong_backend.training.controller;

import com.altong.altong_backend.global.response.ApiResponse;
import com.altong.altong_backend.training.dto.response.EmployeeDashboardResponse;
import com.altong.altong_backend.training.dto.response.OwnerDashboardResponse;
import com.altong.altong_backend.training.dto.response.TrainingDeleteResponse;
import com.altong.altong_backend.training.service.TrainingService;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Training Dashboard", description = "사장님과 알바생 대시보드 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/trainings")
@RequiredArgsConstructor
public class TrainingController {

    private final TrainingService trainingService;

    // 사장님용 대시보드
    @Operation(summary = "사장님용 대시보드 조회", description = "현재 매장의 교육 요약 정보(총 알바생 수, 교육 목록 등)를 조회합니다.")
    @GetMapping("/dashboard/owner")
    public ResponseEntity<ApiResponse<OwnerDashboardResponse>> getOwnerDashboard(
            @RequestHeader("Authorization") String token
    ) {
        OwnerDashboardResponse response = trainingService.getOwnerDashboard(token);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 알바생용 대시보드
    @Operation(summary = "알바생용 대시보드 조회", description = "알바생 본인의 교육 현황과 목록을 조회합니다.")
    @GetMapping("/dashboard/employee")
    public ResponseEntity<ApiResponse<EmployeeDashboardResponse>> getEmployeeDashboard(
            @RequestHeader("Authorization") String token
    ) {
        EmployeeDashboardResponse response = trainingService.getEmployeeDashboard(token);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 교육(메뉴얼, 카드뉴스, 퀴즈) 삭제
    @Operation(summary = "교육 삭제", description = "해당 교육과 연결된 메뉴얼, 카드뉴스, 퀴즈를 모두 삭제합니다.")
    @DeleteMapping("/{trainingId}")
    public ResponseEntity<ApiResponse<TrainingDeleteResponse>> deleteTraining(
            @RequestHeader("Authorization") String token,
            @PathVariable Long trainingId
    ) {
        TrainingDeleteResponse response = trainingService.deleteTraining(token, trainingId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

}
