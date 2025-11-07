package com.altong.altong_backend.training.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class TrainingManualRequest {
    private String businessType;
    private String title;
    private List<String> goal;
    private List<String> procedure;
    private List<String> precaution;
    private String tone;
}
