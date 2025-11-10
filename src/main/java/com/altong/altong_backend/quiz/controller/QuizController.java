package com.altong.altong_backend.quiz.controller;

import com.altong.altong_backend.global.response.ApiResponse;
import com.altong.altong_backend.quiz.dto.response.QuizDetailResponse;
import com.altong.altong_backend.quiz.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trainings")
public class QuizController {

    private final QuizService quizService;

    // 퀴즈 상세 조회
    @GetMapping("/{trainingId}/quiz")
    public ResponseEntity<ApiResponse<List<QuizDetailResponse>>> getQuizByTrainingId(
            @RequestHeader("Authorization") String token,
            @PathVariable Long trainingId
    ) {
        List<QuizDetailResponse> response = quizService.getQuizByTrainingId(token, trainingId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}