package com.altong.altong_backend.manual.dto.response;

import com.altong.altong_backend.manual.model.Manual;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ManualResponse {
    private String title;
    private String goal;
    private List<Manual.ProcedureStep> procedure;
    private List<String> precaution;
}
