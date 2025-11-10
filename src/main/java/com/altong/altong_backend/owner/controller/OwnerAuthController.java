package com.altong.altong_backend.owner.controller;

import com.altong.altong_backend.global.response.ApiResponse;
import com.altong.altong_backend.owner.service.OwnerAuthService;
import com.altong.altong_backend.owner.dto.request.*;
import com.altong.altong_backend.owner.dto.response.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * OwnerAuthController
 *
 * 사장(OWNER) 회원의 로그인, 비밀번호/상호명 변경, 로그아웃 관련 API
 * Swagger 문서화를 통해 제약조건 / 예외 / 테스트 방법을 상세히 기술
 */
@RestController
@RequestMapping("/api/owners")
@RequiredArgsConstructor
@Tag(name = "Owner.Auth API", description = "사장(OWNER) 인증 및 계정 관리 API")
public class OwnerAuthController {

    private final OwnerAuthService service;

    // ===============================
    // 1️. 로그인
    // ===============================
    @Operation(
        summary = "사장 로그인",
        description = """
        ### 개요  
        - 사장 계정 로그인 시 AccessToken / RefreshToken을 발급합니다.
        
        ### 제약조건  
        - `username`, `password` 필수 입력
        - 잘못된 비밀번호 시 401 Unauthorized 응답
        
        ### 예외상황  
        | 코드 | 상태 | 설명 |
        |------|------|------|
        | `A003` | 404 | 사용자 정보 없음 |
        | `A004` | 401 | 비밀번호 불일치 |
        | `G001` | 400 | 필수값 누락 |

        ### 테스트 방법  
        - Swagger / Postman 모두 가능  
        ```json
        {
          "username": "owner01",
          "password": "abcd1234!"
        }
        ```
        """
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "로그인 성공",
            content = @Content(schema = @Schema(implementation = OwnerLoginResponse.class),
                examples = @ExampleObject(value = """
                {
                  "code": "SUCCESS",
                  "message": "요청이 성공적으로 처리되었습니다.",
                  "data": {
                    "accessToken": "eyJhbGciOi...",
                    "refreshToken": "eyJhbGciOi...",
                    "username": "owner01",
                    "storeName": "알통치킨 평택점"
                  }
                }
                """))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "비밀번호 불일치",
            content = @Content(examples = @ExampleObject(value = """
                { "code": "A004", "message": "비밀번호가 올바르지 않습니다.", "data": null }
                """))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "사용자 없음",
            content = @Content(examples = @ExampleObject(value = """
                { "code": "A003", "message": "해당 사용자를 찾을 수 없습니다.", "data": null }
                """))
        )
    })
    @PostMapping("/login")
    public ApiResponse<OwnerLoginResponse> login(@RequestBody @Valid OwnerLoginRequest req) {
        return ApiResponse.success(service.login(req));
    }

    // ===============================
    // 2️. 비밀번호 변경
    // ===============================
    @Operation(
        summary = "비밀번호 변경",
        description = """
        ### 개요  
        - 사장이 자신의 비밀번호를 변경합니다.
        
        ### 제약조건  
        - 헤더: `X-OWNER-ID` 필수
        - Body: `oldPassword`, `newPassword` 필수
        
        ### 예외상황  
        | 코드 | 상태 | 설명 |
        |------|------|------|
        | `A004` | 401 | 기존 비밀번호 불일치 |
        | `A003` | 404 | 사용자 없음 |
        | `G001` | 400 | 필수값 누락 |
        
        ### 테스트 방법  
        - Swagger / Postman 모두 가능  
        ```json
        {
          "oldPassword": "abcd1234!",
          "newPassword": "newPass!123"
        }
        ```
        헤더에 `X-OWNER-ID: 1` 추가 후 요청
        """
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "비밀번호 변경 성공",
            content = @Content(examples = @ExampleObject(value = """
                { "code": "SUCCESS", "message": "비밀번호가 성공적으로 변경되었습니다.", "data": null }
                """))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "비밀번호 불일치",
            content = @Content(examples = @ExampleObject(value = """
                { "code": "A004", "message": "기존 비밀번호가 일치하지 않습니다.", "data": null }
                """))
        )
    })
    @PatchMapping("/password")
    public ApiResponse<OwnerPasswordUpdateResponse> updatePassword(
        @RequestHeader("X-OWNER-ID") Long ownerId,
        @RequestBody @Valid OwnerPasswordUpdateRequest req
    ) {
        return ApiResponse.success(service.updatePassword(ownerId, req));
    }

    // ===============================
    // 3️. 상호명 변경
    // ===============================
    @Operation(
        summary = "상호명 변경",
        description = """
        ### 개요  
        - 사장이 등록된 가게의 상호명을 변경합니다.
        
        ### 제약조건  
        - 헤더: `X-OWNER-ID` 필수
        - Body: `storeName` 필수 (공백 불가, 최대 50자)
        
        ### 예외상황  
        | 코드 | 상태 | 설명 |
        |------|------|------|
        | `A003` | 404 | 사용자 없음 |
        | `G001` | 400 | 잘못된 입력 |
        
        ### 테스트 방법  
        ```json
        { "storeName": "알통치킨 송탄점" }
        ```
        헤더에 `X-OWNER-ID: 1` 추가 후 요청
        """
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "상호명 변경 성공",
            content = @Content(examples = @ExampleObject(value = """
                { "code": "SUCCESS", "message": "상호명이 변경되었습니다.", "data": null }
                """))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "입력 오류",
            content = @Content(examples = @ExampleObject(value = """
                { "code": "G001", "message": "유효하지 않은 상호명입니다.", "data": null }
                """))
        )
    })
    @PatchMapping("/store-name")
    public ApiResponse<OwnerPasswordUpdateResponse> updateStoreName(
        @RequestHeader("X-OWNER-ID") Long ownerId,
        @RequestBody @Valid OwnerStoreNameUpdateRequest req
    ) {
        return ApiResponse.success(service.updateStoreName(ownerId, req));
    }

    // ===============================
    // 4️. 로그아웃
    // ===============================
    @Operation(
        summary = "로그아웃",
        description = """
        ### 개요  
        - Refresh Token을 만료시켜 로그아웃을 처리합니다.
        
        ### 제약조건  
        - Body: `refreshToken` 필수
        
        ### 예외상황  
        | 코드 | 상태 | 설명 |
        |------|------|------|
        | `A004` | 401 | 토큰 검증 실패 |
        | `G001` | 400 | 잘못된 요청 |
        
        ### 테스트 방법  
        ```json
        { "refreshToken": "eyJhbGciOi..." }
        ```
        """
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "로그아웃 성공",
            content = @Content(examples = @ExampleObject(value = """
                { "code": "SUCCESS", "message": "로그아웃이 완료되었습니다.", "data": null }
                """))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "토큰 오류",
            content = @Content(examples = @ExampleObject(value = """
                { "code": "A004", "message": "유효하지 않은 토큰입니다.", "data": null }
                """))
        )
    })
    @PostMapping("/logout")
    public ApiResponse<OwnerLogoutResponse> logout(@RequestBody @Valid OwnerLogoutRequest req) {
        return ApiResponse.success(service.logout(req));
    }
}