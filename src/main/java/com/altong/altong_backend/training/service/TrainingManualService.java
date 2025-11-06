package com.altong.altong_backend.training.service;


import com.altong.altong_backend.global.exception.BusinessException;
import com.altong.altong_backend.global.exception.ErrorCode;
import com.altong.altong_backend.store.model.Store;
import com.altong.altong_backend.store.repository.StoreRepository;
import com.altong.altong_backend.training.dto.request.TrainingManualRequest;
import com.altong.altong_backend.training.dto.response.TrainingManualResponse;
import com.altong.altong_backend.training.model.Training;
import com.altong.altong_backend.training.repository.TrainingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TrainingManualService {

    private final TrainingRepository trainingRepository;
    private final StoreRepository storeRepository;
    private final RestTemplate restTemplate;

    @Value("${ai.manual.api-url}")
    private String MANUAL_API_URL;

    @Transactional
    public TrainingManualResponse generateManual(Long ownerId, TrainingManualRequest request) {

        // 사장님의 가게 찾기
        Store store = storeRepository.findByOwner_Id(ownerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        // FastAPI로 요청 보내기
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<TrainingManualRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<TrainingManualResponse> aiResponse =
                restTemplate.exchange(
                        MANUAL_API_URL + "/generate",
                        HttpMethod.POST,
                        entity,
                        TrainingManualResponse.class
                );

        TrainingManualResponse responseBody = aiResponse.getBody();

        if (responseBody == null) {
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
        }

        // DB 저장
        Training training = Training.builder()
                .title(responseBody.getTitle())
                .category(request.getBusinessType())
                .inputText(String.join("\n", request.getGoal()) + "\n" + String.join("\n", request.getProcedure()))
                .aiResponse(responseBody.getGoal())
                .createdAt(LocalDateTime.now())
                .store(store)
                .build();

        trainingRepository.save(training);
        return responseBody;
    }
}
