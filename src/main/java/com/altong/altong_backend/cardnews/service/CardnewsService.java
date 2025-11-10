package com.altong.altong_backend.cardnews.service;

import com.altong.altong_backend.cardnews.dto.request.CardnewsRequest;
import com.altong.altong_backend.cardnews.dto.response.CardnewsResponse;
import com.altong.altong_backend.cardnews.model.CardNews;
import com.altong.altong_backend.cardnews.repository.CardnewsRepository;
import com.altong.altong_backend.global.exception.BusinessException;
import com.altong.altong_backend.global.exception.ErrorCode;
import com.altong.altong_backend.global.jwt.JwtTokenProvider;
import com.altong.altong_backend.owner.model.Owner;
import com.altong.altong_backend.owner.repository.OwnerRepository;
import com.altong.altong_backend.store.model.Store;
import com.altong.altong_backend.training.model.Training;
import com.altong.altong_backend.training.repository.TrainingRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardnewsService {

    private final CardnewsRepository cardnewsRepository;
    private final TrainingRepository trainingRepository;
    private final OwnerRepository ownerRepository;
    private final RestTemplate restTemplate;
    private final JwtTokenProvider jwt;

    @Value("${ai.cardnews.api-url}")
    private String CARDNEWS_API_URL;

    /**
     * 카드뉴스 생성 (Training ID 기반)
     */
    @Transactional
    public CardnewsResponse generateCardnews(Long trainingId, String tone) {
        log.info("[내부호출] 카드뉴스 생성 시작(JWT 검증 생략): trainingId={}, tone={}", trainingId, tone);

        // Training 확인
        Training training = trainingRepository.findById(trainingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRAINING_NOT_FOUND));

        if (training.getManual() == null) {
            log.error("❌ Training에 Manual이 없음: trainingId={}", trainingId);
            throw new BusinessException(ErrorCode.MANUAL_NOT_FOUND);
        }

        Long manualId = training.getManual().getId();

        // FastAPI 요청
        CardnewsRequest request = CardnewsRequest.builder()
                .manualId(manualId)
                .tone(tone)
                .numSlides(4)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CardnewsRequest> entity = new HttpEntity<>(request, headers);

        try {
            log.debug("→ [내부호출] FastAPI 요청 전송: url={}, manualId={}", CARDNEWS_API_URL, manualId);

            ResponseEntity<CardnewsResponse> aiResponse = restTemplate.exchange(
                    CARDNEWS_API_URL,
                    HttpMethod.POST,
                    entity,
                    CardnewsResponse.class
            );

            CardnewsResponse response = aiResponse.getBody();

            if (response == null) {
                log.error("❌ [내부호출] FastAPI 응답이 null: trainingId={}", trainingId);
                throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
            }

            // DB 저장
            CardNews cardNews = CardNews.builder()
                    .imageUrl(response.getSlides().get(0).getImageUrl())
                    .training(training)
                    .build();

            cardnewsRepository.save(cardNews);
            log.info("✅ [내부호출] 카드뉴스 DB 저장 완료: id={}, trainingId={}", cardNews.getId(), trainingId);
            return response;

        } catch (Exception e) {
            log.error("❌ [내부호출] 카드뉴스 생성 실패: trainingId={}, 원인: {}", trainingId, e.getMessage());
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
        }
    }
    }