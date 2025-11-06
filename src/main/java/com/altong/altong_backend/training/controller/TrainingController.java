package com.altong.altong_backend.training.controller;

import com.altong.altong_backend.global.response.ApiResponse;
import com.altong.altong_backend.training.dto.request.TrainingManualRequest;
import com.altong.altong_backend.training.dto.response.TrainingManualResponse;
import com.altong.altong_backend.training.service.TrainingManualService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trainings")
public class TrainingController {

    private final TrainingManualService trainingManualService;

    // 메뉴얼 생성
    @PostMapping("/manual")
    public ResponseEntity<ApiResponse<TrainingManualResponse>> createManual(
            @AuthenticationPrincipal(expression = "id") Long ownerId,
            @RequestBody TrainingManualRequest request
    ) {
        TrainingManualResponse response = trainingManualService.generateManual(ownerId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
