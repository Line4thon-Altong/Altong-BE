package com.altong.altong_backend.owner.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class OwnerLogoutRequest {

    @NotBlank
    private String refreshToken;
}