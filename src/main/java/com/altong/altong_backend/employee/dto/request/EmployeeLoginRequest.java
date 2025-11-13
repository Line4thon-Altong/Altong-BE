package com.altong.altong_backend.employee.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class EmployeeLoginRequest {

    @NotBlank(message = "username은 필수값입니다.")
    private String username;

    @NotBlank(message = "password는 필수값입니다.")
    private String password;
}