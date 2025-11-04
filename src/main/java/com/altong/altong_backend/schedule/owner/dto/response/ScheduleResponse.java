package com.altong.altong_backend.schedule.owner.dto.response;

import com.altong.altong_backend.schedule.entity.Schedule;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalTime;

@Schema(name = "ScheduleResponse", description = "스케줄 등록 응답")
public record ScheduleResponse(
        @Schema(description = "스케줄 ID", example = "1") Long scheduleId,
        @Schema(description = "매장 ID", example = "1") Long storeId,
        @Schema(description = "직원 ID", example = "1") Long employeeId,
        @Schema(description = "직원 이름", example = "조효동") String employeeName,
        @Schema(description = "근무 날짜", example = "2025-11-04") LocalDate workDate,
        @Schema(description = "출근 시간", example = "09:00", nullable = true) LocalTime startTime,
        @Schema(description = "퇴근 시간", example = "18:00", nullable = true) LocalTime endTime,
        @Schema(description = "근무 상태", example = "SCHEDULED") String workStatus
) {

    // DTO 변환 정적 팩토리 메서드
    public static ScheduleResponse from(Schedule schedule) {
        return new ScheduleResponse(
                schedule.getId(),
                schedule.getStore() != null ? schedule.getStore().getId() : null,
                schedule.getEmployee() != null ? schedule.getEmployee().getId() : null,
                schedule.getEmployee() != null ? schedule.getEmployee().getName() : null,
                schedule.getWorkDate(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getWorkStatus() != null ? schedule.getWorkStatus().name() : null
        );
    }


}
