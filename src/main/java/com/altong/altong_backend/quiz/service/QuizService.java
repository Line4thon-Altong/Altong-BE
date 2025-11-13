package com.altong.altong_backend.quiz.service;

import com.altong.altong_backend.global.exception.BusinessException;
import com.altong.altong_backend.global.exception.ErrorCode;
import com.altong.altong_backend.global.jwt.JwtTokenProvider;
import com.altong.altong_backend.owner.model.Owner;
import com.altong.altong_backend.owner.repository.OwnerRepository;
import com.altong.altong_backend.quiz.dto.response.QuizDetailResponse;
import com.altong.altong_backend.quiz.dto.response.QuizResponse;
import com.altong.altong_backend.quiz.model.Quiz;
import com.altong.altong_backend.quiz.repository.QuizRepository;
import com.altong.altong_backend.store.model.Store;
import com.altong.altong_backend.training.model.Training;
import com.altong.altong_backend.training.repository.TrainingRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final QuizRepository quizRepository;
    private final TrainingRepository trainingRepository;
    private final JwtTokenProvider jwt;
    private final OwnerRepository ownerRepository;

    @Value("${AI_QUIZ_API_URL}")
    private String QUIZ_API_URL;

    // 퀴즈 생성
    public QuizResponse generateQuiz(Long trainingId, String tone) {
        log.info("🎯 [QuizService] FastAPI 퀴즈 생성 요청 시작 | trainingId={}, tone={}", trainingId, tone);

        try {
            // 1. 요청 바디 구성
            Map<String, Object> body = new HashMap<>();
            body.put("manual_id", trainingId);
            body.put("tone", tone);
            body.put("focus", "procedure");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            log.info("[QuizService] 요청 본문: {}", objectMapper.writeValueAsString(body));
            log.info("[QuizService] 요청 URL: {}", QUIZ_API_URL);

            // 2. FastAPI 호출
            ResponseEntity<String> response = restTemplate.postForEntity(QUIZ_API_URL, entity, String.class);
            log.info("[QuizService] FastAPI 응답 코드: {}", response.getStatusCode());
            log.debug("[QuizService] FastAPI 원문 응답: {}", response.getBody());

            if (response.getStatusCode() != HttpStatus.OK) {
                log.error("[QuizService] AI 서버 응답 실패: {}", response.getStatusCode());
                throw new RuntimeException("AI 서버 응답 실패: " + response.getStatusCode());
            }

            // 3. 응답 JSON → DTO 변환
            QuizResponse quizResponse = objectMapper.readValue(response.getBody(), QuizResponse.class);
            log.info("[QuizService] 응답 파싱 완료 | quizCount={}",
                    quizResponse.getQuizzes() != null ? quizResponse.getQuizzes().size() : 0);

            // 4. Training 조회
            Training training = trainingRepository.findById(trainingId)
                    .orElseThrow(() -> new RuntimeException("해당 training이 존재하지 않습니다."));
            log.info("[QuizService] Training 조회 완료 | trainingId={}", training.getId());

            // 5. 퀴즈 저장
            for (int i = 0; i < quizResponse.getQuizzes().size(); i++) {
                var q = quizResponse.getQuizzes().get(i);
                try {
                    String optionsJson = objectMapper.writeValueAsString(q.getOptions());
                    Quiz quiz = Quiz.builder()
                            .type(q.getType().equalsIgnoreCase("OX")
                                    ? com.altong.altong_backend.quiz.model.QuizType.OX
                                    : com.altong.altong_backend.quiz.model.QuizType.MULTIPLE)
                            .question(q.getQuestion())
                            .options(optionsJson)
                            .answer(q.getAnswer())
                            .explanation(q.getExplanation())
                            .createdAt(LocalDateTime.now())
                            .training(training)
                            .build();

                    quizRepository.save(quiz);
                    log.info("[QuizService] 퀴즈 {} 저장 완료 | question='{}'", i + 1, q.getQuestion());

                } catch (JsonProcessingException e) {
                    log.error("[QuizService] 퀴즈 옵션 직렬화 실패: {}", e.getMessage(), e);
                    throw new RuntimeException("퀴즈 옵션 직렬화 실패: " + e.getMessage(), e);
                } catch (Exception e) {
                    log.error("[QuizService] 퀴즈 저장 실패 (index={}): {}", i, e.getMessage(), e);
                    throw new RuntimeException("퀴즈 저장 실패: " + e.getMessage(), e);
                }
            }

            log.info("[QuizService] 퀴즈 전체 저장 완료 | trainingId={}", trainingId);
            return quizResponse;

        } catch (Exception e) {
            log.error("[QuizService] FastAPI 퀴즈 생성 중 예외 발생: {}", e.getMessage(), e);
            throw new RuntimeException("퀴즈 생성 또는 저장 실패: " + e.getMessage(), e);
        }
    }

    // 퀴즈 상세 조회
    @Transactional(readOnly = true)
    public List<QuizDetailResponse> getQuizByTrainingId(String token, Long trainingId) {
        // JWT 파싱
        String accessToken = token.replace("Bearer ", "");
        Claims claims;
        try {
            claims = jwt.parse(accessToken).getBody();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        String subject = claims.getSubject();
        if (!subject.startsWith("OWNER:")) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ROLE);
        }
        Long ownerId = Long.parseLong(subject.substring(6));

        // 사장님 확인
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.OWNER_NOT_FOUND));

        // 퀴즈 조회
        List<Quiz> quizzes = quizRepository.findByTraining_IdOrderByIdAsc(trainingId);
        if (quizzes.isEmpty()) {
            throw new BusinessException(ErrorCode.QUIZ_NOT_FOUND);
        }

        // 본인 가게 소유 여부 확인
        Store store = quizzes.get(0).getTraining().getStore();
        if (!store.getOwner().getId().equals(owner.getId())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        // DTO 변환 후 반환
        return quizzes.stream()
                .map(QuizDetailResponse::from)
                .collect(Collectors.toList());
    }
}
