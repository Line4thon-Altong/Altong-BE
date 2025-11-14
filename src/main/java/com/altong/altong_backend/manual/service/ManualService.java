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

    public ManualResponse generateManual(String token, ManualRequest request) {

        // 0. JWT 검증
        String accessToken = token.replace("Bearer ", "");
        Claims claims;
        try {
            claims = jwt.parse(accessToken).getBody();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        if (!claims.getSubject().startsWith("OWNER:")) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ROLE);
        }
        Long ownerId = Long.parseLong(claims.getSubject().substring(6));

        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.OWNER_NOT_FOUND));

        Store store = storeRepository.findByOwner(owner)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));


        // 1. FastAPI 메뉴얼 생성 호출
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ManualRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<ManualResponse> aiResponse = restTemplate.exchange(
                MANUAL_API_URL,
                HttpMethod.POST,
                entity,
                ManualResponse.class
        );

        ManualResponse responseBody = aiResponse.getBody();
        if (responseBody == null)
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);

        log.info("FastAPI 메뉴얼 생성 완료");


        // 2. Training + Manual 저장 (트랜잭션 분리 → 커밋 보장)
        Manual savedManual = saveTrainingAndManual(owner, store, responseBody, request);

        Long trainingId = savedManual.getTraining().getId();
        Long manualId = savedManual.getId();


        // ---------------------------------------------------------
        // 트랜잭션 밖 — 실패해도 절대 롤백되면 안 되는 영역
        // ---------------------------------------------------------

        // 3. RAG 임베딩
        try {
            log.info("[ManualService] RAG 임베딩 요청 시작 | manualId={}", manualId);

            Map<String, Object> embedBody = new HashMap<>();
            embedBody.put("manual_id", manualId);
            embedBody.put("manual_json", responseBody);

            HttpHeaders embedHeaders = new HttpHeaders();
            embedHeaders.setContentType(MediaType.APPLICATION_JSON);

            restTemplate.postForEntity(
                    "http://15.165.210.249:8000/rag/embed",
                    new HttpEntity<>(embedBody, embedHeaders),
                    String.class
            );
            log.info("[ManualService] RAG 임베딩 완료");

        } catch (Exception e) {
            log.error("RAG 임베딩 실패 (무시하고 계속 진행): {}", e.getMessage());
        }


        // 4. 퀴즈 생성
        try {
            log.info("퀴즈 생성 시작");
            quizService.generateQuiz(trainingId, savedManual.getTone());
            log.info("퀴즈 생성 완료");
        } catch (Exception e) {
            log.error("퀴즈 생성 실패 (계속 진행): {}", e.getMessage());
        }


        // 5. 카드뉴스 생성
        try {
            log.info("카드뉴스 생성 시작");
            cardnewsService.generateCardnews(trainingId);
            log.info("카드뉴스 생성 완료");
        } catch (Exception e) {
            log.error("카드뉴스 생성 실패 (계속 진행): {}", e.getMessage());
        }

        // ---------------------------------------------------------

        return responseBody;
    }


    /**
     * Training + Manual 저장만 트랜잭션으로 묶어서 즉시 커밋
     * 나머지 AI 호출은 이 트랜잭션 밖에서 진행한다.
     */
    @Transactional
    public Manual saveTrainingAndManual(
            Owner owner,
            Store store,
            ManualResponse responseBody,
            ManualRequest request
    ) {

        // Training 저장
        Training training = Training.builder()
                .title(responseBody.getTitle())
                .createdAt(LocalDateTime.now())
                .store(store)
                .build();
        trainingRepository.save(training);

        // Manual 저장
        Manual manual = Manual.builder()
                .title(responseBody.getTitle())
                .goal(responseBody.getGoal())
                .procedure(responseBody.getProcedure())
                .precaution(responseBody.getPrecaution())
                .aiRawResponse(toJsonSafe(responseBody))
                .training(training)
                .tone(request.getTone())
                .createdAt(LocalDateTime.now())
                .build();

        manualRepository.save(manual);

        training.setManual(manual);
        trainingRepository.save(training);

        return manual;
    }

    // Jackson JSON 변환 안전 처리
    private String toJsonSafe(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("JSON 변환 실패: {}", e.getMessage());
            return "{}";
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
