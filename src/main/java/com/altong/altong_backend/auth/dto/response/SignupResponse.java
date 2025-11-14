package com.altong.altong_backend.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class SignupResponse {

    @Schema(description = "사용자 PK", example = "1")
    private Long id;

    @Schema(description = "로그인 ID", example = "owner01")
    private String username;

    @Schema(description = "역할 (OWNER / EMPLOYEE)", example = "OWNER")
    private String role;

    @Schema(description = "소속 가게 ID (OWNER는 생성된 storeId)", example = "10")
    private Long storeId;

    @Schema(description = "소속 가게 이름", example = "알통치킨 평택점")
    private String storeName;

    @Schema(description = "회원가입 시각", example = "2025-11-14T01:23:45")
    private LocalDateTime createdAt;
}
