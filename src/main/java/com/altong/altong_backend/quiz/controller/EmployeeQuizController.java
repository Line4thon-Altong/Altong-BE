package com.altong.altong_backend.quiz.controller;

import com.altong.altong_backend.global.response.ApiResponse;
import com.altong.altong_backend.quiz.dto.request.QuizSubmitRequest;
import com.altong.altong_backend.quiz.dto.response.QuizDetailResponse;
import com.altong.altong_backend.quiz.dto.response.QuizSubmitResponse;
import com.altong.altong_backend.quiz.service.EmployeeQuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Quiz (알바생용 퀴즈)", description = "알바생이 교육(training)에 속한 퀴즈를 조회하고 제출하는 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employees/trainings/{trainingId}/quiz")
public class EmployeeQuizController {

    private final EmployeeQuizService employeeQuizService;

    // 알바생용 퀴즈 조회
    @Operation(summary = "알바생용 퀴즈 조회", description = "알바생이 자신의 교육(training)에 속한 퀴즈 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<QuizDetailResponse>>> getQuizForEmployee(
            @RequestHeader("Authorization") String token,
            @PathVariable Long trainingId
    ) {
        List<QuizDetailResponse> response = employeeQuizService.getQuizByTrainingId(token, trainingId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 퀴즈 제출
    @Operation(summary = "퀴즈 제출", description = "알바생이 특정 퀴즈를 풀고 답안을 제출합니다.")
    @PostMapping("/{quizId}")
    public ResponseEntity<ApiResponse<QuizSubmitResponse>> submitQuiz(
            @RequestHeader("Authorization") String token,
            @PathVariable Long trainingId,
            @PathVariable Long quizId,
            @RequestBody QuizSubmitRequest request
    ) {
        QuizSubmitResponse response = employeeQuizService.submitQuiz(token, trainingId, quizId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}