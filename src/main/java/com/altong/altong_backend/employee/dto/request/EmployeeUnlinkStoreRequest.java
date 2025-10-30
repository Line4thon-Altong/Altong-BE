package com.altong.altong_backend.employee.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeUnlinkStoreRequest {
    private Long employeeId;
    private Long storeId;
}
