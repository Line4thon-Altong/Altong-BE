package com.altong.altong_backend.schedule.owner.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Schema(name = "ScheduleListResponse", description = "알바생 출퇴근 시간 목록 응답")
public class ScheduleListResponse {
    @Schema(description = "스케줄 목록")
    private List<ScheduleResponse> schedules;
}
