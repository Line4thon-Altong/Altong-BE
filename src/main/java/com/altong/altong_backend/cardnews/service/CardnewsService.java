package com.altong.altong_backend.cardnews.service;

import com.altong.altong_backend.cardnews.dto.response.CardnewsResponse;
import com.altong.altong_backend.cardnews.model.CardNews;
import com.altong.altong_backend.cardnews.repository.CardnewsRepository;
import com.altong.altong_backend.global.exception.BusinessException;
import com.altong.altong_backend.global.exception.ErrorCode;
import com.altong.altong_backend.training.model.Training;
import com.altong.altong_backend.training.repository.TrainingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ai.cardnews.api-url}")
    private String CARDNEWS_API_URL;

    @Transactional
    public CardnewsResponse generateCardnews(Long trainingId) {
        log.info("[내부호출] 카드뉴스 생성 시작: trainingId={}", trainingId);

        Training training = trainingRepository.findById(trainingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRAINING_NOT_FOUND));

        if (training.getManual() == null) {
            log.error("Training에 Manual이 없음: trainingId={}", trainingId);
            throw new BusinessException(ErrorCode.MANUAL_NOT_FOUND);
        }

        Long manualId = training.getManual().getId();
        String url = CARDNEWS_API_URL + "?manual_id=" + manualId;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            log.debug("→ FastAPI 요청: url={}, manualId={}", url, manualId);

            ResponseEntity<CardnewsResponse> aiResponse = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    CardnewsResponse.class
            );

            CardnewsResponse response = aiResponse.getBody();

            // 디버깅 로그
            log.info("FastAPI 전체 응답: {}", response);

            if (response == null) {
                log.error("FastAPI 응답이 null");
                throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
            }

            // imageUrl 직접 추출 및 검증
            String imageUrl = response.getImageUrl();
            log.info("추출된 Image URL: '{}'", imageUrl);
            log.info("Image URL 타입: {}", imageUrl != null ? imageUrl.getClass().getName() : "null");

            if (imageUrl == null || imageUrl.isEmpty()) {
                log.error("image_url이 비어있음!");
                throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
            }

            if (!imageUrl.startsWith("http")) {
                log.error("image_url이 URL 형식이 아님: {}", imageUrl);
                throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
            }

            // DB 저장
            CardNews cardNews = CardNews.builder()
                    .imageUrl(imageUrl)
                    .training(training)
                    .build();

            cardnewsRepository.save(cardNews);

            log.info("카드뉴스 DB 저장 완료: id={}, trainingId={}, imageUrl={}",
                    cardNews.getId(), trainingId, cardNews.getImageUrl());

            return response;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("카드뉴스 생성 실패: trainingId={}, 원인: {}", trainingId, e.getMessage(), e);
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
        }
    }
}