package com.altong.altong_backend.owner.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OwnerLoginResponse {
    private Long id;
    private String username;
    private Long storeId;
    private String storeName;
    private String role;
    private String accessToken;
    private String refreshToken;
}
