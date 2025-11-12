package com.altong.altong_backend.schedule.owner.controller;

import com.altong.altong_backend.global.response.ApiResponse;
import com.altong.altong_backend.schedule.owner.dto.request.ScheduleCreateRequest;
import com.altong.altong_backend.schedule.owner.dto.response.ScheduleListResponse;
import com.altong.altong_backend.schedule.owner.dto.response.ScheduleResponse;
import com.altong.altong_backend.schedule.owner.service.OwnerScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@AllArgsConstructor
@Tag(name = "Schedule(Owner)", description = "스케줄 관리(사장님) API")
public class OwnerScheduleController {

    private final OwnerScheduleService ownerScheduleService;

    @PostMapping("/api/stores/{storeId}/employees/{employeeId}/schedules")
    @Operation(
            summary = "알바 스케줄 등록",
            description = "사장님이 특정 매장의 직원 근무 날짜를 등록. startTime/endTime은 직원이 입력",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "생성 성공",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ScheduleResponse.class))))
    public ResponseEntity<ApiResponse<List<ScheduleResponse>>> createSchedule(@PathVariable Long storeId,
                                                                        @PathVariable Long employeeId,
                                                                        @Valid @RequestBody ScheduleCreateRequest request) {
        List<ScheduleResponse> response = ownerScheduleService.createSchedule(storeId,employeeId,request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/api/stores/{storeId}/employees/{employeeId}/schedules")
    @Operation(
            summary = "특정 직원 출퇴근 기록 조회",
            description = "사장님이 매장의 특정 직원 출퇴근 기록을 조회",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = ScheduleListResponse.class)))
    public ResponseEntity<ApiResponse<ScheduleListResponse>> getEmployeeSchedules(
            @PathVariable Long storeId,
            @PathVariable Long employeeId) {
        
        ScheduleListResponse response = ownerScheduleService.getEmployeeSchedules(storeId, employeeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/api/stores/{storeId}/schedules")
    @Operation(
            summary = "매장 전체 스케줄 조회",
            description = "사장님이 매장의 전체 스케줄을 조회. workDate로 특정 날짜만 조회 가능",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = ScheduleListResponse.class)))
    public ResponseEntity<ApiResponse<ScheduleListResponse>> getStoreSchedules(
            @PathVariable Long storeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate workDate) {
        
        ScheduleListResponse response = ownerScheduleService.getStoreSchedules(storeId, workDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/api/stores/{storeId}/schedules/{scheduleId}")
    @Operation(
            summary = "알바 스케줄 삭제",
            description = "사장님이 특정 스케줄을 삭제",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공")
    public ResponseEntity<ApiResponse<Void>> deleteSchedule(
            @PathVariable Long storeId,
            @PathVariable Long scheduleId) {
        
        ownerScheduleService.deleteSchedule(storeId, scheduleId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
