package com.altong.altong_backend.schedule.dto.employee.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record ScheduleCreateRequest(@NotNull(message="근무일자는 필수입니다.") LocalDate workDate,
                                    LocalTime startTime,
                                    LocalTime endTime) {
}
