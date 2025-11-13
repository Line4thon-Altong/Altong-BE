package com.altong.altong_backend.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInfoResponse {

    private Long id;            // 숫자 PK
    private String username;    // 계정 ID
    private String displayName; // 사장 = 가게명, 알바 = 본명
    private Long storeId;
    private String storeName;
    private String role;
}