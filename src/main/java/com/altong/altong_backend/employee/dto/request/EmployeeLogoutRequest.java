package com.altong.altong_backend.employee.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EmployeeLogoutRequest {
    @NotBlank private String refreshToken;
}
