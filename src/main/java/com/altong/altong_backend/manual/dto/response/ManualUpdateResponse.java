package com.altong.altong_backend.manual.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ManualUpdateResponse {
    private Long manualId;
    private String message;
}
