package com.altong.altong_backend.schedule.employee.dto.request;

import java.time.LocalDate;

public record ScheduleUpdateRequest (Long id, LocalDate workDate){
}
