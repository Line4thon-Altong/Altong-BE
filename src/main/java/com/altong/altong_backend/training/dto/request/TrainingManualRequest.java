package com.altong.altong_backend.training.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TrainingManualRequest {
    private String businessType;
    private String title;
    private String goal;
    private String procedure;
    private String precaution;
    private String tone;
}
