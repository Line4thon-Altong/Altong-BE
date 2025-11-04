package com.altong.altong_backend.schedule.owner.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;

public record CheckOutResponse (Long scheduleId, LocalDate workDate,
                                LocalTime checkOutTime, String status){
}
