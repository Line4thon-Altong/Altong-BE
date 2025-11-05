package com.altong.altong_backend.schedule.employee.controller;

import com.altong.altong_backend.global.response.ApiResponse;
import com.altong.altong_backend.schedule.employee.dto.response.CheckInResponse;
import com.altong.altong_backend.schedule.employee.dto.response.CheckOutResponse;
import com.altong.altong_backend.schedule.employee.service.EmployeeScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Schedule(Employee)", description = "스케줄 관리(알바생) API")
public class EmployeeScheduleController {

    private final EmployeeScheduleService employeeScheduleService;

    @PatchMapping("/api/employees/{employeeId}/schedules/check-in")
    @Operation(
            summary = "출근하기",
            description = "알바생이 현재 시간으로 출근 처리",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "출근 성공",
            content = @Content(schema = @Schema(implementation = CheckInResponse.class)))
    public ResponseEntity<ApiResponse<CheckInResponse>> checkIn(@PathVariable Long employeeId) {
        CheckInResponse response = employeeScheduleService.checkIn(employeeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/api/employees/{employeeId}/schedules/check-out")
    @Operation(
            summary = "퇴근하기",
            description = "알바생이 현재 시각으로 퇴근 처리",
            security = { @SecurityRequirement(name = "bearerAuth")}
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200",description = "퇴근 성공",
            content = @Content(schema = @Schema(implementation = CheckOutResponse.class)))
    public ResponseEntity<ApiResponse<CheckOutResponse>> checkOut(@PathVariable Long employeeId) {
        CheckOutResponse response = employeeScheduleService.checkOut(employeeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
