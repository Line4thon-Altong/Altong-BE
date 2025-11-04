package com.altong.altong_backend.schedule.employee.controller;

import com.altong.altong_backend.global.response.ApiResponse;
import com.altong.altong_backend.schedule.employee.dto.request.ScheduleCreateRequest;
import com.altong.altong_backend.schedule.employee.dto.response.ScheduleResponse;
import com.altong.altong_backend.schedule.employee.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@Tag(name = "Schedule", description = "스케줄 관리(사장님) API")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PostMapping("/api/stores/{storeId}/employees/{employeeId}/schedules")
    @Operation(
            summary = "알바 스케줄 등록",
            description = "사장님이 특정 매장의 직원 근무 날짜를 등록. startTime/endTime은 직원이 입력",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "생성 성공",
            content = @Content(schema = @Schema(implementation = ScheduleResponse.class)))
    public ResponseEntity<ApiResponse<ScheduleResponse>> createSchedule(@PathVariable Long storeId,
                                                                        @PathVariable Long employeeId,
                                                                        @RequestBody ScheduleCreateRequest scheduleCreateRequest) {
        ScheduleResponse response = scheduleService.createSchedule(storeId,employeeId,scheduleCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }
}
