package com.altong.altong_backend.quiz.service;

import com.altong.altong_backend.quiz.dto.response.QuizResponse;
import com.altong.altong_backend.quiz.model.Quiz;
import com.altong.altong_backend.quiz.repository.QuizRepository;
import com.altong.altong_backend.training.model.Training;
import com.altong.altong_backend.training.repository.TrainingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final QuizRepository quizRepository;
    private final TrainingRepository trainingRepository;

    @Value("${AI_QUIZ_API_URL}")
    private String QUIZ_API_URL;

    public QuizResponse generateQuiz(Long trainingId, String tone) {
        try {
            // FastAPI 요청 바디
            Map<String, Object> body = new HashMap<>();
            body.put("manual_id", trainingId);
            body.put("tone", tone);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            // FastAPI 호출
            ResponseEntity<String> response = restTemplate.postForEntity(QUIZ_API_URL, entity, String.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("AI 서버 응답 실패: " + response.getStatusCode());
            }

            QuizResponse quizResponse = objectMapper.readValue(response.getBody(), QuizResponse.class);

            // DB 저장
            Training training = trainingRepository.findById(trainingId)
                    .orElseThrow(() -> new RuntimeException("해당 training이 존재하지 않습니다."));

            quizResponse.getQuizzes().forEach(q -> {
                Quiz quiz = Quiz.builder()
                        .type(q.getType().equalsIgnoreCase("OX") ? com.altong.altong_backend.quiz.model.QuizType.OX : com.altong.altong_backend.quiz.model.QuizType.MULTIPLE)
                        .question(q.getQuestion())
                        .options(objectMapper.valueToTree(q.getOptions()).toString())
                        .answer(q.getAnswer())
                        .explanation(q.getExplanation())
                        .createdAt(LocalDateTime.now())
                        .isCompleted(false)
                        .training(training)
                        .build();
                quizRepository.save(quiz);
            });

            return quizResponse;

        } catch (Exception e) {
            throw new RuntimeException("퀴즈 생성 또는 저장 실패: " + e.getMessage(), e);
        }
    }
}
