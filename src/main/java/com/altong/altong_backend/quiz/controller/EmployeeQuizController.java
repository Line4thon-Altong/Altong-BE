package com.altong.altong_backend.quiz.controller;

import com.altong.altong_backend.global.response.ApiResponse;
import com.altong.altong_backend.quiz.dto.response.QuizDetailResponse;
import com.altong.altong_backend.quiz.service.EmployeeQuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employees/trainings")
public class EmployeeQuizController {

    private final EmployeeQuizService employeeQuizService;

    // 알바생용 퀴즈 조회
    @GetMapping("/{trainingId}/quiz")
    public ResponseEntity<ApiResponse<List<QuizDetailResponse>>> getQuizForEmployee(
            @RequestHeader("Authorization") String token,
            @PathVariable Long trainingId
    ) {
        List<QuizDetailResponse> response = employeeQuizService.getQuizByTrainingId(token, trainingId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}