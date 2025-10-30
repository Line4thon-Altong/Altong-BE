package com.altong.altong_backend.owner.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OwnerLogoutRequest {
    @NotBlank private String refreshToken;
}
