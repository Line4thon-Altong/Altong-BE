package com.altong.altong_backend.cardnews.controller;

import com.altong.altong_backend.cardnews.dto.response.CardnewsResponse;
import com.altong.altong_backend.cardnews.service.CardnewsService;
import com.altong.altong_backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trainings")
@RequiredArgsConstructor
@Tag(name = "CardNews", description = "카드뉴스 생성 API (FastAPI 연동)")
public class CardnewsController {

    private final CardnewsService cardnewsService;

    @PostMapping("/{trainingId}/cardnews")
    @Operation(
            summary = "카드뉴스 생성",
            description = """
                    Training ID를 기반으로 FastAPI 서버와 연동하여 AI 카드뉴스를 생성합니다.
                    
                    - 4컷 만화 형태의 카드뉴스 생성
                    - 각 슬라이드별로 제목, 한 줄 설명 제공
                    - 매뉴얼 생성 시 설정한 tone이 자동으로 적용됩니다.
                    - 4컷 이미지 1장 생성
                    """,
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "카드뉴스 생성 성공",
            content = @Content(schema = @Schema(implementation = CardnewsResponse.class))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Training을 찾을 수 없음"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "502",
            description = "FastAPI 서버 연동 실패"
    )
    public ResponseEntity<ApiResponse<CardnewsResponse>> generateCardnews(
            @RequestHeader("Authorization") String token,
            @Parameter(description = "교육 ID", required = true)
            @PathVariable Long trainingId
    ) {
        CardnewsResponse response = cardnewsService.generateCardnews(trainingId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}