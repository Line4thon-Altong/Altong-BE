// CardSlide.java
package com.altong.altong_backend.cardnews.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class CardSlide {
    private String title;
    
    private List<String> content;
    
    @JsonProperty("image_prompt")
    private String imagePrompt;
    
    @JsonProperty("image_url")
    private String imageUrl;
}