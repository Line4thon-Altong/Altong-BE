package com.altong.altong_backend.manual.service;


import com.altong.altong_backend.global.exception.BusinessException;
import com.altong.altong_backend.global.exception.ErrorCode;
import com.altong.altong_backend.global.jwt.JwtTokenProvider;
import com.altong.altong_backend.owner.model.Owner;
import com.altong.altong_backend.owner.repository.OwnerRepository;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

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

    @Value("${ai.manual.api-url}")
    private String MANUAL_API_URL;

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

        if (responseBody == null) {
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
        }

        // Training & Manual 저장
        try {
            // Training 먼저 생성
            Training training = Training.builder()
                    .title(responseBody.getTitle())
                    .createdAt(LocalDateTime.now())
                    .store(store)
                    .build();

            trainingRepository.save(training);

            // Manual 생성 후 training과 연결
            Manual manual = Manual.builder()
                    .title(responseBody.getTitle())
                    .goal(responseBody.getGoal())
                    .procedure(responseBody.getProcedure())
                    .precaution(responseBody.getPrecaution())
                    .aiRawResponse(objectMapper.writeValueAsString(responseBody))
                    .training(training)
                    .createdAt(LocalDateTime.now())
                    .build();

            manualRepository.save(manual);

            return responseBody;

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR);
        }
    }
}
