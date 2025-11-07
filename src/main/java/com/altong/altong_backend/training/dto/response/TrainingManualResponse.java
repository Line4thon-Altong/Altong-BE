package com.altong.altong_backend.training.dto.response;

import com.altong.altong_backend.training.model.Training;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class TrainingManualResponse {
    private String title;
    private String goal;
    private List<Training.ProcedureStep> procedure;
    private List<String> precaution;
}
