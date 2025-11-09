// CardnewsResponse.java
package com.altong.altong_backend.cardnews.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class CardnewsResponse {
    private String title;
    private List<CardSlide> slides;
}