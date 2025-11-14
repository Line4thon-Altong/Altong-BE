package com.altong.altong_backend.training.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OwnerDashboardResponse {
    private int employeeCount;
    private List<TrainingSummary> trainings;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TrainingSummary {
        private Long id;
        private String title;
        private String createdAt;
    }
}
