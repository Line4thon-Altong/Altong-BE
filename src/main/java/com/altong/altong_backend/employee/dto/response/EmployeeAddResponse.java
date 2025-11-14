package com.altong.altong_backend.employee.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class EmployeeAddResponse {
    private Long employeeId;
    private Long storeId;
    private String message;
}

