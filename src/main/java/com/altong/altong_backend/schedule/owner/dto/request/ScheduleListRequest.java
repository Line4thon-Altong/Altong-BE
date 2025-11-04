package com.altong.altong_backend.schedule.owner.dto.request;

import java.time.LocalDate;

public record ScheduleListRequest (LocalDate startDate, LocalDate endDate, Long employeeId){
}
