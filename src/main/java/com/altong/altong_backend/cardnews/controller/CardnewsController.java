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

    /**
     * 카드뉴스 생성 API
     * FastAPI 서버와 연동하여 AI 기반 4컷 만화 형태의 카드뉴스를 생성합니다.
     */
    @PostMapping("/{trainingId}/cardnews")
    @Operation(
            summary = "카드뉴스 생성",
            description = """
                    Training ID를 기반으로 FastAPI 서버와 연동하여 AI 카드뉴스를 생성합니다.
                    
                    - 4컷 만화 형태의 카드뉴스 생성
                    - 각 슬라이드별로 제목, 설명, 이미지 URL 제공
                    - tone 파라미터로 말투 커스터마이징 가능
                    
                    **tone 옵션:**
                    - friendly: 친근한 말투 (기본값)
                    - professional: 전문적인 말투
                    - casual: 편안한 말투
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
            @PathVariable Long trainingId,
            
            @Parameter(
                    description = "카드뉴스 말투 (friendly/professional/casual)",
                    example = "friendly"
            )
            @RequestParam(defaultValue = "friendly") String tone
    ) {
        CardnewsResponse response = cardnewsService.generateCardnews(trainingId, tone);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
