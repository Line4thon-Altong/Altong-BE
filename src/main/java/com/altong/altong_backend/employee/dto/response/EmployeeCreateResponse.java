package com.altong.altong_backend.employee.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmployeeCreateResponse {
    private Long employeeId;
    private Long storeId;
    private String name;
    private String username;
    private String message;
}

