package com.altong.altong_backend.owner.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OwnerLoginResponse {

    @Schema(description = "사장 ID", example = "1")
    private Long id;

    @Schema(description = "로그인 ID", example = "owner01")
    private String username;

    @Schema(description = "마이페이지에서 표시할 이름 (사장 = 가게명)", example = "알통치킨 평택점")
    private String displayName;

    @Schema(description = "가게 ID", example = "10")
    private Long storeId;

    @Schema(description = "가게명", example = "알통치킨 평택점")
    private String storeName;

    @Schema(description = "역할", example = "OWNER")
    private String role;

    @Schema(description = "Access Token")
    private String accessToken;

    @Schema(description = "Refresh Token")
    private String refreshToken;
}