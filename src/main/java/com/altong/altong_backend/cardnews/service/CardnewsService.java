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
    public CardnewsResponse generateCardnews(String token, Long trainingId, String tone) {
        log.info("🎨 카드뉴스 생성 시작: trainingId={}, tone={}", trainingId, tone);

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

        // Training 확인
        Training training = trainingRepository.findById(trainingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRAINING_NOT_FOUND));

        // 본인 가게의 Training인지 확인
        Store store = training.getStore();
        if (!store.getOwner().getId().equals(owner.getId())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        // Manual 존재 확인
        if (training.getManual() == null) {
            log.error("❌ Training에 Manual이 없음: trainingId={}", trainingId);
            throw new BusinessException(ErrorCode.MANUAL_NOT_FOUND);
        }

        // Manual ID 가져오기
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
            log.debug("→ FastAPI 요청 전송 중: url={}, manualId={}", CARDNEWS_API_URL, manualId);
            
            ResponseEntity<CardnewsResponse> aiResponse = restTemplate.exchange(
                    CARDNEWS_API_URL,
                    HttpMethod.POST,
                    entity,
                    CardnewsResponse.class
            );
            
            log.debug("← FastAPI 응답 수신: status={}", aiResponse.getStatusCode());

            CardnewsResponse response = aiResponse.getBody();

            if (response == null) {
                log.error("❌ FastAPI 응답이 null: trainingId={}", trainingId);
                throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
            }

            // DB 저장
            CardNews cardNews = CardNews.builder()
                    .imageUrl(response.getSlides().get(0).getImageUrl())  // 4컷 만화 이미지
                    .training(training)
                    .build();

            cardnewsRepository.save(cardNews);
            log.debug("💾 CardNews DB 저장 완료: id={}, imageUrl={}", cardNews.getId(), cardNews.getImageUrl());

            log.info("✅ 카드뉴스 생성 완료: trainingId={}", trainingId);
            return response;

        } catch (Exception e) {
            log.error("❌ 카드뉴스 생성 실패: trainingId={}, 원인: {}", trainingId, e.getMessage());
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
        }
    }
}