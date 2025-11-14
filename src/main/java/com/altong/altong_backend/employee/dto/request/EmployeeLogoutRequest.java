package com.altong.altong_backend.employee.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class EmployeeLogoutRequest {

    @NotBlank(message = "refreshToken은 필수입니다.")
    private String refreshToken;
}