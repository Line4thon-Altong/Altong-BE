package com.altong.altong_backend.cardnews.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardSlide {
    @JsonProperty("slide_id")
    private Integer slideId;

    private String title;

    private String content;
}