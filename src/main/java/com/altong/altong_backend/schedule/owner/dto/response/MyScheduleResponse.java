package com.altong.altong_backend.schedule.owner.dto.response;

import com.altong.altong_backend.schedule.employee.dto.response.ScheduleResponse;

import java.util.List;

public class MyScheduleResponse {
    private Long employeeId; // 알바생 ID
    private List<ScheduleResponse> schedules;
}
