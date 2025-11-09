// CardnewsRequest.java
package com.altong.altong_backend.cardnews.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardnewsRequest {
    @JsonProperty("manual_id")
    private Long manualId;
    
    private String tone;
    
    @JsonProperty("num_slides")
    @Builder.Default
    private Integer numSlides = 4;
}