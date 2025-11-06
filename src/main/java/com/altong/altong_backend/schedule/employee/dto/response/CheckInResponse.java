package com.altong.altong_backend.schedule.employee.dto.response;

import com.altong.altong_backend.schedule.entity.Schedule;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalTime;

@Schema(name = "CheckInResponse", description = "출근 처리 응답")
public record CheckInResponse(
        @Schema(description = "스케줄 ID", example = "1") Long scheduleId,
        @Schema(description = "직원 ID", example = "1") Long employeeId,
        @Schema(description = "직원 이름", example = "조효동") String employeeName,
        @Schema(description = "근무 날짜", example = "2025-11-05") LocalDate workDate,
        @Schema(description = "출근 시각", example = "13:00:00") LocalTime startTime,
        @Schema(description = "근무 상태", example = "WORKING") String workStatus
) {
    public static CheckInResponse from(Schedule schedule) {
        return new CheckInResponse(
                schedule.getId(),
                schedule.getEmployee() != null ? schedule.getEmployee().getId() : null,
                schedule.getEmployee() != null ? schedule.getEmployee().getName() : null,
                schedule.getWorkDate(),
                schedule.getStartTime(),
                schedule.getWorkStatus() != null ? schedule.getWorkStatus().name() : null
        );
    }
}
