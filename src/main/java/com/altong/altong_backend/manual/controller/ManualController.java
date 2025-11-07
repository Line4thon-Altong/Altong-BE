package com.altong.altong_backend.manual.controller;

import com.altong.altong_backend.global.response.ApiResponse;
import com.altong.altong_backend.manual.dto.request.ManualRequest;
import com.altong.altong_backend.manual.dto.response.ManualDetailResponse;
import com.altong.altong_backend.manual.dto.response.ManualResponse;
import com.altong.altong_backend.manual.service.ManualService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trainings")
public class ManualController {

    private final ManualService manualService;

    // 메뉴얼 생성
    @PostMapping("/manual")
    public ResponseEntity<ApiResponse<ManualResponse>> createManual(
            @RequestHeader("Authorization") String token,
            @RequestBody ManualRequest request
    ) {
        ManualResponse response = manualService.generateManual(token, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 메뉴얼 상세 조회
    @GetMapping("/{trainingId}/manuals")
    public ResponseEntity<ApiResponse<ManualDetailResponse>> getManualByTrainingId(
            @RequestHeader("Authorization") String token,
            @PathVariable Long trainingId
    ) {
        ManualDetailResponse response = manualService.getManualByTrainingId(token, trainingId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
