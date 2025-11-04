package com.altong.altong_backend.schedule.dto.employee.response;

import com.altong.altong_backend.schedule.entity.Schedule;
import java.time.LocalDate;
import java.time.LocalTime;

public record ScheduleResponse(Long scheduleId,Long storeId,Long employeeId,String employeeName,LocalDate workDate,
                               LocalTime startTime,LocalTime endTime,String workStatus) {

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
