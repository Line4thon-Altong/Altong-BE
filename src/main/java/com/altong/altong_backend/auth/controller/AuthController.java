package com.altong.altong_backend.auth.controller;

import com.altong.altong_backend.auth.dto.request.SignupRequest;
import com.altong.altong_backend.auth.dto.response.SignupResponse;
import com.altong.altong_backend.auth.dto.response.UserInfoResponse;
import com.altong.altong_backend.auth.service.AuthService;
import com.altong.altong_backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AuthController
 *
 * 회원가입 / JWT 재발급 / 내 정보 조회 기능을 담당하는 API 컨트롤러.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Auth API", description = "회원가입 / JWT 재발급 / 내 정보 조회 API")
public class AuthController {

    private final AuthService authService;

    // ===============================
    // 1️. 회원가입 (OWNER / EMPLOYEE)
    // ===============================
    @Operation(
        summary = "회원가입 (사장/알바)",
        description = """
        ### 개요  
        - 역할(`role`)에 따라 **사장(OWNER)** 또는 **알바(EMPLOYEE)** 회원을 생성합니다.
        - 회원가입 성공 시, 기본 사용자 정보가 반환됩니다.
        
        ### 제약조건  
        - `role`: OWNER | EMPLOYEE (대소문자 무관)  
        - `username`: 4~50자, 중복 불가  
        - `password`: 8~200자  
        - `OWNER`: `storeName` 필수  
        - `EMPLOYEE`: `name` 필수  

        ### 예외 상황  
        | 코드 | 상태 | 설명 |
        |------|------|------|
        | `A002` | 409 | 중복된 사용자 이름 |
        | `G001` | 400 | 필수값 누락/형식 오류 |

        ### 테스트 방법  
        - Swagger / Postman 모두 가능  
        - 예시 요청 (사장)
        ```json
        {
          "role": "OWNER",
          "username": "owner01",
          "password": "abcd1234!",
          "storeName": "알통치킨 평택점"
        }
        ```
        - 예시 요청 (알바)
        ```json
        {
          "role": "EMPLOYEE",
          "username": "emp01",
          "password": "abcd1234!",
          "name": "홍길동"
        }
        ```
        """
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "회원가입 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SignupResponse.class),
                examples = @ExampleObject(value = """
                {
                  "code": "SUCCESS",
                  "message": "요청이 성공적으로 처리되었습니다.",
                  "data": {
                    "id": 1,
                    "username": "owner01",
                    "role": "OWNER",
                    "createdAt": "2025-11-10T10:00:00"
                  }
                }
                """)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "중복 username",
            content = @Content(examples = @ExampleObject(value = """
                { "code": "A002", "message": "이미 존재하는 사용자 이름입니다.", "data": null }
                """))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "입력값 오류",
            content = @Content(examples = @ExampleObject(value = """
                { "code": "G001", "message": "잘못된 입력 값입니다.", "data": null }
                """))
        )
    })
    @PostMapping("/signup")
    public ApiResponse<SignupResponse> signup(@RequestBody @Valid SignupRequest req) {
        return ApiResponse.success(authService.signup(req));
    }

    // ===============================
    // 2️. JWT AccessToken 재발급
    // ===============================
    @Operation(
        summary = "Access Token 재발급",
        description = """
        ### 개요  
        - Refresh Token을 사용하여 새로운 Access Token을 발급합니다.
        
        ### 제약조건  
        - Body JSON에 `refreshToken` 필수
        
        ### 예외 상황  
        | 코드 | 상태 | 설명 |
        |------|------|------|
        | `A004` | 401 | 토큰이 유효하지 않음 |
        | `A001` | 401 | 토큰 파싱 실패 |
        
        ### 테스트 방법  
        ```json
        { "refreshToken": "eyJhbGciOi..." }
        ```
        """
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "재발급 성공",
            content = @Content(
                schema = @Schema(implementation = Map.class),
                examples = @ExampleObject(value = """
                {
                  "code": "SUCCESS",
                  "message": "요청이 성공적으로 처리되었습니다.",
                  "data": { "accessToken": "eyJhbGciOi..." }
                }
                """)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "토큰 오류",
            content = @Content(examples = @ExampleObject(value = """
                { "code": "A004", "message": "토큰이 유효하지 않습니다.", "data": null }
                """))
        )
    })
    @PostMapping("/refresh")
    public ApiResponse<Map<String, String>> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        String newAccess = authService.refreshAccessToken(refreshToken);
        return ApiResponse.success(Map.of("accessToken", newAccess));
    }

    // ===============================
    // 3️. 내 정보 조회
    // ===============================
    @Operation(
        summary = "내 정보 조회",
        description = """
        ### 개요  
        - Authorization 헤더의 Bearer AccessToken을 검증하여 현재 로그인한 사용자 정보를 반환합니다.
        
        ### 제약조건  
        - 헤더: `Authorization: Bearer {accessToken}`
        
        ### 예외 상황  
        | 코드 | 상태 | 설명 |
        |------|------|------|
        | `A004` | 401 | 토큰 검증 실패 |
        | `A003` | 404 | 유저 데이터 없음 |

        ### 테스트 방법  
        - Swagger 상단 **Authorize** 버튼 클릭 후 Bearer 토큰 입력  
        """
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(
                schema = @Schema(implementation = UserInfoResponse.class),
                examples = @ExampleObject(value = """
                {
                  "code": "SUCCESS",
                  "message": "요청이 성공적으로 처리되었습니다.",
                  "data": {
                    "username": "owner01",
                    "storeName": "알통치킨 평택점",
                    "role": "OWNER"
                  }
                }
                """)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(examples = @ExampleObject(value = """
                { "code": "A004", "message": "토큰이 유효하지 않습니다.", "data": null }
                """))
        )
    })
    @GetMapping("/me")
    public ApiResponse<UserInfoResponse> getMyInfo(Authentication auth) {
        return ApiResponse.success(authService.getCurrentUserInfo(auth));
    }
}