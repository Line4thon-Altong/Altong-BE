package com.altong.altong_backend.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class SignupResponse {
    private Long id;
    private String username;
    private String role;
    private LocalDateTime createdAt;
}
