package com.altong.altong_backend.schedule.dto.employee.response;

import java.time.LocalDate;
import java.time.LocalTime;

public record ScheduleResponse(Long scheduleId,Long storeId,Long employeeId,String employeeName,LocalDate workDate,
                               LocalTime startTime,LocalTime endTime,String workStatus) {// 상태

}
