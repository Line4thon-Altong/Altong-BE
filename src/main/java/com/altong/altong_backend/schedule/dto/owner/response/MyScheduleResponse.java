package com.altong.altong_backend.schedule.dto.owner.response;

import com.altong.altong_backend.schedule.dto.employee.response.ScheduleResponse;

import java.util.List;

public class MyScheduleResponse {
    private Long employeeId; // 알바생 ID
    private List<ScheduleResponse> schedules;
}
