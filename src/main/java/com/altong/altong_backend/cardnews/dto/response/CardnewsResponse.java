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
public class CardnewsResponse {
    private String title;
    @JsonProperty("image_url")
    private String imageUrl;

    public String getImageUrl() {
        return this.imageUrl;
    }
}