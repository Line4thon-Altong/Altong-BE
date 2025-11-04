package com.altong.altong_backend.schedule.dto.owner.response;

import java.time.LocalDate;
import java.time.LocalTime;

public record CheckOutResponse (Long scheduleId, LocalDate workDate,
                                LocalTime checkOutTime, String status){
}
