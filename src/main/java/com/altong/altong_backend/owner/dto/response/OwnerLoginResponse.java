package com.altong.altong_backend.owner.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OwnerLoginResponse {
    private String accessToken;
    private String refreshToken;
}
