package com.altong.altong_backend.owner.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Builder;

@Getter
@AllArgsConstructor
@Builder
public class OwnerLoginResponse {
    private String accessToken;
    private String refreshToken;

    private String username;
    private String storeName;
}
