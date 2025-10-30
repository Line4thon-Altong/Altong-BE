package com.altong.altong_backend.auth.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupRequest {
    @NotBlank
    private String role; // OWNER | EMPLOYEE

    @NotBlank
    @Size(min = 4, max = 50)
    private String username;

    @NotBlank
    @Size(min = 8, max = 200)
    private String password;

    private String name;       // EMPLOYEE 용
    private String storeName;  // OWNER 용
}
