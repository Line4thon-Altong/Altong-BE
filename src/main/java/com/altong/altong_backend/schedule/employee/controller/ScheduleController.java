package com.altong.altong_backend.schedule.employee.controller;

import com.altong.altong_backend.global.response.ApiResponse;
import com.altong.altong_backend.schedule.dto.employee.request.ScheduleCreateRequest;
import com.altong.altong_backend.schedule.dto.employee.response.ScheduleResponse;
import com.altong.altong_backend.schedule.employee.service.ScheduleService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PostMapping("/api/stores/{storeId}/employees/{employeeId}/schedules")
    public ResponseEntity<ApiResponse<ScheduleResponse>> createSchedule(@PathVariable Long storeId,
                                                                        @PathVariable Long employeeId,
                                                                        @RequestBody ScheduleCreateRequest scheduleCreateRequest) {
        ScheduleResponse response = scheduleService.createSchedule(storeId,employeeId,scheduleCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }
}
