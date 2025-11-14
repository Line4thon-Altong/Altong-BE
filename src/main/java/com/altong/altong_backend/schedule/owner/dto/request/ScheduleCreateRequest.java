package com.altong.altong_backend.schedule.owner.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Schema(name = "ScheduleCreateRequest", description = "사장님이 직원 근무 날짜를 등록할 때 사용하는 요청 바디")
public record ScheduleCreateRequest(
        @Schema(description = "근무 날짜 목록" , example = "[\"2025-11-04\", \"2025-11-05\", \"2025-11-07\"]")
        @NotEmpty(message="근무일자는 최소 1개 이상이어야 합니다.") List<LocalDate> workDates,

        @Schema(description = "출근 시간(직원이 체크인 시 입력, 사장님X)", example = "09:00", nullable = true)
        LocalTime startTime,

        @Schema(description = "퇴근 시간(직원이 체크아웃 시 입력, 사장님X)", example = "18:00", nullable = true)
        LocalTime endTime
) {
}
