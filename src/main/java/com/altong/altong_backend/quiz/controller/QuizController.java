package com.altong.altong_backend.quiz.controller;

import com.altong.altong_backend.global.response.ApiResponse;
import com.altong.altong_backend.quiz.dto.response.QuizDetailResponse;
import com.altong.altong_backend.quiz.service.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Quiz (사장님용 퀴즈 관리)", description = "사장님이 AI가 생성한 퀴즈를 조회하는 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trainings")
public class QuizController {

    private final QuizService quizService;

    // 퀴즈 상세 조회
    @Operation(summary = "퀴즈 상세 조회", description = "특정 교육(training)에 속한 퀴즈를 사장님이 조회합니다.")
    @GetMapping("/{trainingId}/quiz")
    public ResponseEntity<ApiResponse<List<QuizDetailResponse>>> getQuizByTrainingId(
            @RequestHeader("Authorization") String token,
            @PathVariable Long trainingId
    ) {
        List<QuizDetailResponse> response = quizService.getQuizByTrainingId(token, trainingId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}