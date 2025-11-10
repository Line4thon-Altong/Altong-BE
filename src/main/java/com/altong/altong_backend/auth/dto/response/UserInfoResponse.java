package com.altong.altong_backend.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserInfoResponse {

    @Schema(description = "유저 이름", example = "altong_user")
    private String username;

    @Schema(description = "상호명", example = "알통치킨 평택점")
    private String storeName;

    @Schema(description = "역할", example = "OWNER or EMPLOYEE")
    private String role;
}
