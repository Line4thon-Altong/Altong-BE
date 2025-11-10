package com.altong.altong_backend.quiz.dto.response;

import com.altong.altong_backend.quiz.model.Quiz;
import com.altong.altong_backend.quiz.model.QuizType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizDetailResponse {
    private Long id;
    private QuizType type;
    private String question;
    private String options;
    private String answer;
    private String explanation;
    private Boolean isCompleted;
    private String createdAt;

    // 사장님용
    public static QuizDetailResponse from(Quiz quiz) {
        return QuizDetailResponse.builder()
                .id(quiz.getId())
                .type(quiz.getType())
                .question(quiz.getQuestion())
                .options(quiz.getOptions()) // JSON 그대로 문자열로
                .answer(quiz.getAnswer())
                .explanation(quiz.getExplanation())
                .isCompleted(quiz.getIsCompleted())
                .createdAt(quiz.getCreatedAt().toString())
                .build();
    }

    // 알바생용
    public static QuizDetailResponse forEmployee(Quiz quiz) {
        return QuizDetailResponse.builder()
                .id(quiz.getId())
                .type(quiz.getType())
                .question(quiz.getQuestion())
                .options(quiz.getOptions())
                .isCompleted(quiz.getIsCompleted())
                .createdAt(quiz.getCreatedAt().toString())
                .build();
    }
}
