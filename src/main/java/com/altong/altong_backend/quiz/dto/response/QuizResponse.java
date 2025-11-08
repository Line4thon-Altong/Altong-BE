package com.altong.altong_backend.quiz.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizResponse {

    private List<QuizItem> quizzes;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuizItem {
        private String type;
        private String question;
        private List<String> options;
        private String answer;
        private String explanation;
    }
}
