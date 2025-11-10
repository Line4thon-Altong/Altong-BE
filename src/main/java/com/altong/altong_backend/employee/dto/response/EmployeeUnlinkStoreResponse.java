package com.altong.altong_backend.employee.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EmployeeUnlinkStoreResponse {
    private Long employeeId;
    private Long storeId;
    private String message;
}
