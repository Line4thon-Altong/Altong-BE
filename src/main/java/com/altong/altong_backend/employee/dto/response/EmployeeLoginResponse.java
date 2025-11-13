package com.altong.altong_backend.employee.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmployeeLoginResponse {
    private Long id;
    private String username;
    private String name;
    private String displayName;
    private Long storeId;
    private String storeName;
    private String role;
    private String accessToken;
    private String refreshToken;
}
