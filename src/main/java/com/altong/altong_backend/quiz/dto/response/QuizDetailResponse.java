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
    private Boolean isCorrect;
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
                .isCompleted(true)
                .isCorrect(null)
                .createdAt(quiz.getCreatedAt().toString())
                .build();
    }

    // 알바생용 (안 푼 경우)
    public static QuizDetailResponse forEmployee(Quiz quiz) {
        return QuizDetailResponse.builder()
                .id(quiz.getId())
                .type(quiz.getType())
                .question(quiz.getQuestion())
                .options(quiz.getOptions())
                .answer(null)
                .explanation(null)
                .isCompleted(false)
                .isCorrect(null)
                .createdAt(quiz.getCreatedAt().toString())
                .build();
    }

    // 알바생용 (푼 경우)
    public static QuizDetailResponse fromAttempt(Quiz quiz, boolean isCorrect) {
        return QuizDetailResponse.builder()
                .id(quiz.getId())
                .type(quiz.getType())
                .question(quiz.getQuestion())
                .options(quiz.getOptions())
                .answer(quiz.getAnswer())
                .explanation(quiz.getExplanation())
                .isCompleted(true)
                .isCorrect(isCorrect)
                .createdAt(quiz.getCreatedAt().toString())
                .build();
    }
}
