package com.altong.altong_backend.schedule.employee.controller;

import com.altong.altong_backend.global.exception.BusinessException;
import com.altong.altong_backend.global.exception.ErrorCode;
import com.altong.altong_backend.global.jwt.JwtTokenProvider;
import com.altong.altong_backend.global.response.ApiResponse;
import com.altong.altong_backend.schedule.employee.dto.response.CheckInResponse;
import com.altong.altong_backend.schedule.employee.dto.response.CheckOutResponse;
import com.altong.altong_backend.schedule.employee.service.EmployeeScheduleService;
import com.altong.altong_backend.schedule.owner.dto.response.ScheduleListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@Tag(name = "Schedule(Employee)", description = "스케줄 관리(알바생) API")
public class EmployeeScheduleController {

    private final EmployeeScheduleService employeeScheduleService;
    private final JwtTokenProvider jwtTokenProvider;

    @PatchMapping("/api/employees/{employeeId}/schedules/check-in")
    @Operation(
            summary = "출근하기",
            description = "알바생이 현재 시간으로 출근 처리",
            security = {@SecurityRequirement(name = "bearerAuth")}
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
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "퇴근 성공",
            content = @Content(schema = @Schema(implementation = CheckOutResponse.class)))
    public ResponseEntity<ApiResponse<CheckOutResponse>> checkOut(@PathVariable Long employeeId) {
        CheckOutResponse response = employeeScheduleService.checkOut(employeeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/api/employees/me/schedules")
    @Operation(
            summary = "내 스케줄 조회 (달력/출퇴근기록/내스케줄)",
            description = """
                    로그인된 알바생의 스케줄을 JWT 토큰 기반으로 조회합니다.
                    
                    1. 달력 전체 보기: 전체 데이터 사용
                    2. 내 스케줄만 보기: 토큰에서 employeeId 자동 식별
                    3. 출퇴근 기록 확인: startTime/endTime만 사용
                    4. 내 출퇴근 기록 확인: employeeId + startTime/endTime 조합 필터링
                    
                    [파라미터(workDate) 설정 시]
                    - 특정 날짜만 조회
                    - 없으면 전체 조회
                    """,
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = ScheduleListResponse.class))
    )
    public ResponseEntity<ApiResponse<ScheduleListResponse>> getMySchedules(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate workDate
    ) {
        String jwt = resolveToken(authorization);
        Long employeeId = jwtTokenProvider.getEmployeeIdFromToken(jwt);
        ScheduleListResponse response = employeeScheduleService.getEmployeeSchedules(employeeId, workDate);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private String resolveToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
        return authorizationHeader.substring(7);
    }
}
