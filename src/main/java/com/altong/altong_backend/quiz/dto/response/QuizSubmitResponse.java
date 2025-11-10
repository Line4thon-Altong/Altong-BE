package com.altong.altong_backend.quiz.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuizSubmitResponse {
    private final Long quizId;
    private final boolean isCorrect;
    private final String correctAnswer;
    private final String explanation;
}