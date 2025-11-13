package com.altong.altong_backend.manual.service;


import com.altong.altong_backend.cardnews.service.CardnewsService;
import com.altong.altong_backend.global.exception.BusinessException;
import com.altong.altong_backend.global.exception.ErrorCode;
import com.altong.altong_backend.global.jwt.JwtTokenProvider;
import com.altong.altong_backend.manual.dto.request.ManualUpdateRequest;
import com.altong.altong_backend.manual.dto.response.ManualDetailResponse;
import com.altong.altong_backend.manual.dto.response.ManualUpdateResponse;
import com.altong.altong_backend.owner.model.Owner;
import com.altong.altong_backend.owner.repository.OwnerRepository;
import com.altong.altong_backend.quiz.dto.response.QuizResponse;
import com.altong.altong_backend.quiz.service.QuizService;
import com.altong.altong_backend.store.model.Store;
import com.altong.altong_backend.store.repository.StoreRepository;
import com.altong.altong_backend.manual.dto.request.ManualRequest;
import com.altong.altong_backend.manual.dto.response.ManualResponse;
import com.altong.altong_backend.manual.model.Manual;
import com.altong.altong_backend.manual.repository.ManualRepository;
import com.altong.altong_backend.training.model.Training;
import com.altong.altong_backend.training.repository.TrainingRepository;
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
public class ManualService {

    private final ManualRepository manualRepository;
    private final TrainingRepository trainingRepository;
    private final StoreRepository storeRepository;
    private final OwnerRepository ownerRepository;
    private final RestTemplate restTemplate;
    private final JwtTokenProvider jwt;
    private final ObjectMapper objectMapper;
    private final QuizService quizService;
    private final CardnewsService cardnewsService;

    @Value("${ai.manual.api-url}")
    private String MANUAL_API_URL;

    @Value("${ai.quiz.api-url}")
    private String QUIZ_API_URL;

    // 메뉴얼 생성 + 퀴즈 자동 생성 + 카드뉴스 자동 생성
    @Transactional
    public ManualResponse generateManual(String token, ManualRequest request) {
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

        // 사장님 찾기
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.OWNER_NOT_FOUND));

        // 사장님의 가게 찾기
        Store store = storeRepository.findByOwner(owner)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        // FastAPI로 요청 보내기
        // 1. 메뉴얼 생성 요청
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ManualRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<ManualResponse> aiResponse =
                restTemplate.exchange(
                        MANUAL_API_URL,
                        HttpMethod.POST,
                        entity,
                        ManualResponse.class
                );

        ManualResponse responseBody = aiResponse.getBody();

        log.info("FastAPI 응답 수신 | title={}, goal={}, procedureCount={}, precautionCount={}",
                responseBody.getTitle(),
                responseBody.getGoal(),
                responseBody.getProcedure() != null ? responseBody.getProcedure().size() : 0,
                responseBody.getPrecaution() != null ? responseBody.getPrecaution().size() : 0
        );

        if (responseBody == null) {
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
        }

        // Training & Manual 저장
        try {
            // 2. Training 먼저 생성
            Training training = Training.builder()
                    .title(responseBody.getTitle())
                    .createdAt(LocalDateTime.now())
                    .store(store)
                    .build();

            trainingRepository.save(training);
            log.info("Training 생성 완료 | trainingId={}", training.getId());


            // 3. Manual 생성 후 training과 연결
            Manual manual = Manual.builder()
                    .title(responseBody.getTitle())
                    .goal(responseBody.getGoal())
                    .procedure(responseBody.getProcedure())
                    .precaution(responseBody.getPrecaution())
                    .aiRawResponse(objectMapper.writeValueAsString(responseBody))
                    .training(training)
                    .tone(request.getTone())
                    .createdAt(LocalDateTime.now())
                    .build();
            log.info("메뉴얼 저장 직전 데이터 확인");
            log.info("goal={}", manual.getGoal());
            log.info("procedure={}", objectMapper.writeValueAsString(manual.getProcedure()));
            log.info("⚠precaution={}", manual.getPrecaution());
            manualRepository.save(manual);
            log.info("[ManualService] 메뉴얼 저장 성공 | manualId={}, title={}", manual.getId(), manual.getTitle());

            // 메뉴얼 임베딩
            try {
                log.info("[ManualService] FastAPI RAG 임베딩 요청 시작 | manualId={}", manual.getId());

                // FastAPI에 전송할 Body 구성
                Map<String, Object> embedBody = new HashMap<>();
                embedBody.put("manual_id", manual.getId());
                embedBody.put("manual_json", responseBody); // AI가 생성한 메뉴얼 JSON 그대로 전송

                HttpHeaders embedHeaders = new HttpHeaders();
                embedHeaders.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> embedEntity = new HttpEntity<>(embedBody, embedHeaders);

                // FastAPI 호출
                restTemplate.postForEntity("http://localhost:8000/rag/embed", embedEntity, String.class);
                log.info("[ManualService] RAG 임베딩 완료 | manualId={}", manual.getId());
            } catch (Exception e) {
                log.error("[ManualService] RAG 임베딩 요청 실패: {}", e.getMessage());
                // 그래도 퀴즈 생성은 계속 진행하도록 함 (임베딩 없으면 fallback으로 동작)
            }

            // 4. 퀴즈 생성
            log.info("퀴즈 생성 시작");
            QuizResponse quizResponse = quizService.generateQuiz(training.getId(), manual.getTone());
            log.info("퀴즈 생성 완료 | quizCount={}", quizResponse.getQuizzes() != null ? quizResponse.getQuizzes().size() : 0);


            training.setManual(manual);
            trainingRepository.save(training);
            // 5. 카드 뉴스 생성
            log.info("카드뉴스 생성 시작");
            cardnewsService.generateCardnews(training.getId());
            log.info("카드뉴스 생성 완료");

            return responseBody;

        } catch (Exception e) {
            log.error("퀴즈 또는 카드뉴스 생성 중 오류 발생: {}", e.getMessage());
            log.error("StackTrace:", e);
            throw new BusinessException(ErrorCode.DATABASE_ERROR);
        }
    }

    // 메뉴얼 상세 조회
    @Transactional(readOnly = true)
    public ManualDetailResponse getManualByTrainingId(String token, Long trainingId) {
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

        // 메뉴얼 조회
        Manual manual = manualRepository.findByTraining_Id(trainingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MANUAL_NOT_FOUND));

        // 본인 가게의 메뉴얼인지 확인
        Store store = manual.getTraining().getStore();
        if (!store.getOwner().getId().equals(owner.getId())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        String cardnewsUrl = null;
        if (manual.getTraining().getCardNews() != null) {
            cardnewsUrl = manual.getTraining().getCardNews().getImageUrl();
        }

        // DTO 변환
        return ManualDetailResponse.from(manual,cardnewsUrl);
    }

    // 메뉴얼 수정
    @Transactional
    public ManualUpdateResponse updateManual(String token, Long trainingId, ManualUpdateRequest request) {
        // JWT 검증
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

        // 사장님 & Training & Manual 조회
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.OWNER_NOT_FOUND));

        Training training = trainingRepository.findById(trainingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRAINING_NOT_FOUND));

        Manual manual = manualRepository.findByTraining_Id(trainingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MANUAL_NOT_FOUND));

        // 권한 확인
        if (!training.getStore().getOwner().getId().equals(owner.getId())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        // 수정 필드 반영
        if (request.getTitle() != null) manual = manual.toBuilder().title(request.getTitle()).build();
        if (request.getGoal() != null) manual = manual.toBuilder().goal(request.getGoal()).build();
        if (request.getProcedure() != null) {
            List<Manual.ProcedureStep> mappedProcedure = request.getProcedure().stream()
                    .map(step -> new Manual.ProcedureStep(
                            (String) step.get("step"),
                            (List<String>) step.get("details")
                    ))
                    .collect(Collectors.toList());
            manual = manual.toBuilder().procedure(mappedProcedure).build();
        }
        if (request.getPrecaution() != null)
            manual = manual.toBuilder().precaution(request.getPrecaution()).build();

        manualRepository.save(manual);

        return ManualUpdateResponse.builder()
                .manualId(manual.getId())
                .message("메뉴얼이 성공적으로 수정되었습니다.")
                .build();
    }
}
