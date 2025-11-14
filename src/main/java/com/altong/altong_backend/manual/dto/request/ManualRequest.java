package com.altong.altong_backend.manual.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ManualRequest {
    private String businessType;
    private String title;
    private List<String> goal;
    private List<String> procedure;
    private List<String> precaution;
    private String tone;
}
