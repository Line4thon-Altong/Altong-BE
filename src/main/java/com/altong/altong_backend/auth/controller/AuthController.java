package com.altong.altong_backend.auth.controller;

import com.altong.altong_backend.auth.dto.request.SignupRequest;
import com.altong.altong_backend.auth.dto.response.SignupResponse;
import com.altong.altong_backend.auth.service.AuthService;
import com.altong.altong_backend.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** 회원가입 */
    @PostMapping("/signup")
    public ApiResponse<SignupResponse> signup(@RequestBody @Valid SignupRequest req) {
        return ApiResponse.success(authService.signup(req));
    }

    /** JWT 재발급 */
    @PostMapping("/refresh")
    public ApiResponse<Map<String, String>> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        String newAccess = authService.refreshAccessToken(refreshToken);
        return ApiResponse.success(Map.of("accessToken", newAccess));
    }
}
