package com.altong.altong_backend.employee.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class EmployeeListResponse {
    private Long id;
    private String username;
    private String name;
    private LocalDateTime addedAt;
}
