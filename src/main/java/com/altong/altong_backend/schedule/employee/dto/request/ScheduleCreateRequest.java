package com.altong.altong_backend.schedule.employee.dto.request;

import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalTime;

@Schema(name = "ScheduleCreateRequest", description = "사장님이 직원 근무 날짜를 등록할 때 사용하는 요청 바디")
public record ScheduleCreateRequest(
        @Schema(description = "근무 날짜", example = "2025-11-04")
        @NotNull(message="근무일자는 필수입니다.") LocalDate workDate,

        @Schema(description = "출근 시간(직원이 체크인 시 입력, 사장님X)", example = "09:00", nullable = true)
        LocalTime startTime,

        @Schema(description = "퇴근 시간(직원이 체크아웃 시 입력, 사장님X)", example = "18:00", nullable = true)
        LocalTime endTime
) {
}
