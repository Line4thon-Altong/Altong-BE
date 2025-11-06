package com.altong.altong_backend.training.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class TrainingManualResponse {
    private String title;
    private String goal;
    private List<ProcedureItem> procedure;
    private List<String> precaution;

    @Getter
    @NoArgsConstructor
    public static class ProcedureItem {
        private String step;
        private List<String> details;
    }
}
