package com.altong.altong_backend.owner.controller;

import com.altong.altong_backend.global.response.ApiResponse;
import com.altong.altong_backend.owner.dto.request.*;
import com.altong.altong_backend.owner.dto.response.*;
import com.altong.altong_backend.owner.service.OwnerAuthService;
import com.altong.altong_backend.global.jwt.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/owners")
@RequiredArgsConstructor
@Tag(name = "Owner.Auth API", description = "사장(OWNER) 인증 및 계정 관리 API")
public class OwnerAuthController {

    private final OwnerAuthService service;
    private final JwtTokenProvider jwt;

    // =========================================================================
    // 1. 사장 로그인
    // =========================================================================
    @Operation(
        summary = "사장 로그인",
        description = """
            # 개요
            사장(OWNER) 로그인 시 **AccessToken / RefreshToken을 발급**합니다.

            ---

            # 요청 형식
            |필드|설명|
            |----|----|
            |username|사장 로그인 계정명|
            |password|비밀번호|

            DTO 유효성 실패 → `G001 INVALID_INPUT_VALUE`

            ---

            # 예시 요청
            ```json
            {
              "username": "owner01",
              "password": "abcd1234!"
            }
            ```

            ---

            # 성공 응답 예시
            ```json
            {
              "code": "SUCCESS",
              "data": {
                "id": 1,
                "username": "owner01",
                "storeId": 10,
                "storeName": "알통치킨 평택점",
                "role": "OWNER",
                "accessToken": "eyJhbGciOi...",
                "refreshToken": "eyJhbGciOi..."
              }
            }
            ```

            ---

            # 에러 상황 / 코드

            |코드|HTTP|설명|
            |----|----|----|
            |A003 NOT_FOUND_USER|404|username 존재하지 않음|
            |A004 INVALID_CREDENTIALS|401|비밀번호 불일치|
            |G001 INVALID_INPUT_VALUE|400|필드 누락|

            ---

            # 테스트 방법
            1) username / password 정상 입력 → 토큰 발급  
            2) password 틀리게 입력 → INVALID_CREDENTIALS  
            3) username 없음 → NOT_FOUND_USER  
            """
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "로그인 성공",
            content = @Content(schema = @Schema(implementation = OwnerLoginResponse.class))
        )
    })
    @PostMapping("/login")
    public ApiResponse<OwnerLoginResponse> login(@RequestBody @Valid OwnerLoginRequest req) {
        return ApiResponse.success(service.login(req));
    }

    // =========================================================================
    // 2. 비밀번호 변경
    // =========================================================================
    @Operation(
        summary = "비밀번호 변경",
        description = """
            # 개요
            로그인한 사장(OWNER)이 자신의 비밀번호를 변경합니다.  
            AccessToken을 통해 ownerId를 추출하여 본인 인증합니다.

            ---

            # 요청 형식
            |필드|설명|
            |----|----|
            |oldPassword|기존 비밀번호|
            |newPassword|새 비밀번호|

            ---

            # 예시 요청
            ```json
            {
              "oldPassword": "abcd1234!",
              "newPassword": "newPass!123"
            }
            ```

            ---

            # 성공 응답 예시
            ```json
            { "code": "SUCCESS", "data": { "message": "비밀번호 변경 완료" } }
            ```

            ---

            # 에러 상황 / 코드

            |코드|HTTP|설명|
            |----|----|----|
            |A004 INVALID_CREDENTIALS|401|기존 비밀번호 불일치|
            |A003 NOT_FOUND_USER|404|owner 없음|
            |INVALID_TOKEN|401|AccessToken 잘못됨|
            |G001 INVALID_INPUT_VALUE|400|필드 누락|

            ---

            # 테스트 방법
            1) 로그인 → AccessToken 준비  
            2) Authorize에 AccessToken 입력  
            3) 기존 비번/새 비번 보내서 정상 변경 확인  
            """
    )
    @PatchMapping("/password")
    public ApiResponse<OwnerPasswordUpdateResponse> updatePassword(
        HttpServletRequest request,
        @RequestBody @Valid OwnerPasswordUpdateRequest req
    ) {
        String token = extractToken(request);
        Long ownerId = jwt.getOwnerIdFromToken(token);
        return ApiResponse.success(service.updatePassword(ownerId, req));
    }

    // =========================================================================
    // 3. 상호명 변경
    // =========================================================================
    @Operation(
        summary = "상호명 변경",
        description = """
            # 개요
            사장이 운영 중인 가게의 **상호명을 변경**합니다.  
            AccessToken으로 ownerId를 확인합니다.

            ---

            # 요청 형식
            |필드|설명|
            |----|----|
            |storeName|새 상호명|

            ---

            # 예시 요청
            ```json
            { "storeName": "알통치킨 송탄점" }
            ```

            ---

            # 성공 응답 예시
            ```json
            {
              "code": "SUCCESS",
              "data": { "message": "상호명이 변경되었습니다." }
            }
            ```

            ---

            # 에러 상황 / 코드
            |코드|HTTP|설명|
            |----|----|----|
            |A003 OWNER_NOT_FOUND|404|사장 정보 없음|
            |A002 STORE_NOT_FOUND|404|store 없음|
            |INVALID_TOKEN|401|AccessToken 오류|
            |G001|400|필수값 누락|

            ---

            # 테스트 방법
            1) 로그인 후 AccessToken 준비  
            2) storeName 바꾸고 실행  
            3) 실제 DB 반영 여부 확인  
            """
    )
    @PatchMapping("/store-name")
    public ApiResponse<OwnerStoreNameUpdateResponse> updateStoreName(
        HttpServletRequest request,
        @RequestBody @Valid OwnerStoreNameUpdateRequest req
    ) {
        String token = extractToken(request);
        Long ownerId = jwt.getOwnerIdFromToken(token);
        return ApiResponse.success(service.updateStoreName(ownerId, req));
    }

    // =========================================================================
    // 4. 로그아웃
    // =========================================================================
    @Operation(
        summary = "로그아웃",
        description = """
            # 개요
            사장의 Refresh Token을 삭제하여 로그아웃 처리합니다.

            ---

            # 요청 형식
            ```json
            { "refreshToken": "eyJhbGciOi..." }
            ```

            ---

            # 성공 응답
            ```json
            { "code": "SUCCESS", "data": { "message": "로그아웃 완료" } }
            ```

            ---

            # 에러 상황 / 코드
            |코드|HTTP|설명|
            |----|----|----|
            |INVALID_TOKEN|401|RefreshToken 파싱 실패|
            |A003 OWNER_NOT_FOUND|404|owner 없음|
            |G001|400|필드 누락|

            ---

            # 테스트 방법
            1) sign-in → refreshToken 확보  
            2) refreshToken body에 넣고 요청  
            3) DB에서 refreshToken null로 변경되었는지 확인  
            """
    )
    @PostMapping("/logout")
    public ApiResponse<OwnerLogoutResponse> logout(@RequestBody @Valid OwnerLogoutRequest req) {
        return ApiResponse.success(service.logout(req));
    }

    // =========================================================================
    // 공통 토큰 추출
    // =========================================================================
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new IllegalArgumentException("유효하지 않은 Authorization 헤더입니다.");
        }
        return header.substring(7);
    }
}