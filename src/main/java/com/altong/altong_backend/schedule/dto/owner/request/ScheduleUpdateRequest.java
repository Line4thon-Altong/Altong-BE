package com.altong.altong_backend.schedule.dto.owner.request;

import java.time.LocalDate;

public record ScheduleUpdateRequest (Long id, LocalDate workDate){
}
