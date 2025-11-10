package com.altong.altong_backend.employee.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Builder;

@Getter
@AllArgsConstructor
@Builder
public class EmployeeLoginResponse {
    private String accessToken;
    private String refreshToken;

    private String username;
    private String storeName;
}
