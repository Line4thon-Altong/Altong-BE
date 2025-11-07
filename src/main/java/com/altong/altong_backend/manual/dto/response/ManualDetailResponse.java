package com.altong.altong_backend.manual.dto.response;

import com.altong.altong_backend.manual.model.Manual;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManualDetailResponse {
    private String title;
    private String goal;
    private List<Manual.ProcedureStep> procedure;
    private List<String> precaution;
    private LocalDateTime createdAt;

    public static ManualDetailResponse from(Manual manual) {
        return ManualDetailResponse.builder()
                .title(manual.getTitle())
                .goal(manual.getGoal())
                .procedure(manual.getProcedure())
                .precaution(manual.getPrecaution())
                .createdAt(manual.getCreatedAt())
                .build();
    }
}