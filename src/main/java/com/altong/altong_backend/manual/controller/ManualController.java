package com.altong.altong_backend.manual.controller;

import com.altong.altong_backend.global.response.ApiResponse;
import com.altong.altong_backend.manual.dto.request.ManualRequest;
import com.altong.altong_backend.manual.dto.response.ManualDetailResponse;
import com.altong.altong_backend.manual.dto.response.ManualResponse;
import com.altong.altong_backend.manual.service.ManualService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Manual (사장님용 메뉴얼)", description = "사장님이 메뉴얼을 생성하고 조회하는 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trainings")
public class ManualController {

    private final ManualService manualService;

    // 메뉴얼 생성
    @Operation(summary = "메뉴얼 생성", description = "사장님이 AI를 통해 새로운 교육 메뉴얼을 생성합니다.")
    @PostMapping("/manual")
    public ResponseEntity<ApiResponse<ManualResponse>> createManual(
            @RequestHeader("Authorization") String token,
            @RequestBody ManualRequest request
    ) {
        ManualResponse response = manualService.generateManual(token, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 메뉴얼 상세 조회
    @Operation(summary = "메뉴얼 상세 조회", description = "특정 교육(training)에 속한 메뉴얼 내용을 조회합니다.")
    @GetMapping("/{trainingId}/manuals")
    public ResponseEntity<ApiResponse<ManualDetailResponse>> getManualByTrainingId(
            @RequestHeader("Authorization") String token,
            @PathVariable Long trainingId
    ) {
        ManualDetailResponse response = manualService.getManualByTrainingId(token, trainingId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
