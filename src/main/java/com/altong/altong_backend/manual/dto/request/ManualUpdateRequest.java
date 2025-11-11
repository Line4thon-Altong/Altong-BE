package com.altong.altong_backend.manual.dto.request;

import lombok.Getter;
import java.util.List;
import java.util.Map;

@Getter
public class ManualUpdateRequest {
    private String title;
    private String goal;
    private List<Map<String, Object>> procedure;
    private List<String> precaution;
}