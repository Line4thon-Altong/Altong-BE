package com.altong.altong_backend.employee.controller;

import com.altong.altong_backend.global.jwt.JwtTokenProvider;
import com.altong.altong_backend.global.response.ApiResponse;
import com.altong.altong_backend.employee.dto.request.*;
import com.altong.altong_backend.employee.dto.response.*;
import com.altong.altong_backend.employee.service.EmployeeAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Tag(name = "Employee.Auth API", description = "알바(EMPLOYEE) 인증 및 계정 관리 API")
public class EmployeeAuthController {

    private final EmployeeAuthService service;
    private final JwtTokenProvider jwt;

    // =========================================================================
    // 1. 알바 로그인
    // =========================================================================
    @Operation(
        summary = "알바 로그인",
        description = """
            # 개요
            알바(EMPLOYEE) 계정 로그인 시 **AccessToken / RefreshToken을 발급**합니다.

            ---

            # 요청 형식
            |필드|설명|
            |----|----|
            |username|알바 로그인 ID (필수)|
            |password|비밀번호 (필수)|

            DTO 유효성 검증 실패 시 → `G001 INVALID_INPUT_VALUE`

            ---

            # 예시 요청
            ```json
            {
              "username": "emp01",
              "password": "abcd1234!"
            }
            ```

            ---

            # 성공 응답 예시
            ```json
            {
              "code": "SUCCESS",
              "data": {
                "id": 3,
                "username": "emp01",
                "name": "홍길동",
                "displayName": "홍길동",
                "storeId": 10,
                "storeName": "알통치킨 평택점",
                "role": "EMPLOYEE",
                "accessToken": "eyJhbGciOi...",
                "refreshToken": "eyJhbGciOi..."
              }
            }
            ```

            ---

            # 에러 상황 / 코드 표

            |코드|HTTP|설명|
            |----|----|----|
            |A003 (NOT_FOUND_USER)|404|username 존재하지 않음|
            |A004 (INVALID_CREDENTIALS)|401|비밀번호 불일치|
            |G001 (INVALID_INPUT_VALUE)|400|username 또는 password 필드 누락|

            ---

            # 테스트 방법
            1. 정상 username + password로 로그인 → 토큰 발급 확인  
            2. password 틀리게 입력 → INVALID_CREDENTIALS 확인  
            3. username 누락 → INVALID_INPUT_VALUE 확인  
            """
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "로그인 성공",
            content = @Content(schema = @Schema(implementation = EmployeeLoginResponse.class))
        )
    })
    @PostMapping("/login")
    public ApiResponse<EmployeeLoginResponse> login(@RequestBody @Valid EmployeeLoginRequest req) {
        return ApiResponse.success(service.login(req));
    }

    // =========================================================================
    // 2. 비밀번호 변경
    // =========================================================================
    @Operation(
        summary = "비밀번호 변경",
        description = """
            # 개요
            로그인한 알바가 **본인 비밀번호를 변경**합니다.  
            AccessToken에서 employeeId를 추출하여 본인 인증을 수행합니다.

            ---

            # 요청 형식
            |필드|설명|
            |----|----|
            |oldPassword|기존 비밀번호 (필수)|
            |newPassword|새 비밀번호 (필수)|

            ---

            # 예시 요청
            ```json
            {
              "oldPassword": "abcd1234!",
              "newPassword": "newPass!123"
            }
            ```

            ---

            # 성공 응답
            ```json
            { "code": "SUCCESS", "data": { "message": "비밀번호 변경 완료" } }
            ```

            ---

            # 에러 상황 / 코드 표

            |코드|HTTP|설명|
            |----|----|----|
            |A004 (INVALID_CREDENTIALS)|401|기존 비번 불일치|
            |A003 (NOT_FOUND_USER)|404|사용자 없음|
            |INVALID_TOKEN|401|토큰 만료 / 변조|
            |G001|400|필수값 누락|

            ---

            # 테스트 방법
            1. 로그인 후 발급된 AccessToken을 Swagger Authorize에 입력  
            2. 기존 비번/새 비번 전송 → 성공 확인  
            3. 기존 비번 틀리게 입력 → INVALID_CREDENTIALS 확인  
            """
    )
    @PatchMapping("/password")
    public ApiResponse<EmployeePasswordUpdateResponse> updatePassword(
        HttpServletRequest request,
        @RequestBody @Valid EmployeePasswordUpdateRequest req
    ) {
        String token = extractToken(request);
        Long empId = jwt.getEmployeeIdFromToken(token);
        return ApiResponse.success(service.updatePassword(empId, req));
    }

    // =========================================================================
    // 3. 가게 연동 해제
    // =========================================================================
    @Operation(
        summary = "가게 연동 해제",
        description = """
            # 개요
            employeeId 또는 storeId를 전달하지 않고,  
            **AccessToken 기반으로 자동 식별하여 가게 연동을 해제**합니다.

            ---

            # 성공 응답 예시
            ```json
            {
              "code": "SUCCESS",
              "data": {
                "employeeId": 5,
                "storeId": null,
                "message": "가게 연동이 해제되었습니다."
              }
            }
            ```

            ---

            # 에러 상황 / 코드

            |코드|HTTP|설명|
            |----|----|----|
            |INVALID_TOKEN|401|유효하지 않은 AccessToken|
            |A003 (NOT_FOUND_USER)|404|알바 정보 없음|

            ---

            # 테스트 방법
            1. 로그인 → AccessToken 준비  
            2. Authorize에 입력 후 /unlink-store 호출  
            3. employee.store = null 되는지 확인  
            4. 토큰 변조 후 재요청 → INVALID_TOKEN 확인  
            """
    )
    @DeleteMapping("/unlink-store")
    public ApiResponse<EmployeeUnlinkStoreResponse> unlinkStore(HttpServletRequest request) {
        String token = extractToken(request);
        Long empId = jwt.getEmployeeIdFromToken(token);
        return ApiResponse.success(service.unlinkStore(empId));
    }

    // =========================================================================
    // 4. 로그아웃
    // =========================================================================
    @Operation(
        summary = "로그아웃",
        description = """
            # 개요
            로그인한 알바의 **Refresh Token을 삭제하여 로그아웃** 처리합니다.

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
            |A003|404|사용자 없음|
            |G001|400|필드 누락|

            ---

            # 테스트 방법
            1. 로그인 후 발급된 refreshToken을 body에 전송  
            2. Logout 후 해당 refreshToken이 DB에서 삭제되는지 확인  
            3. 잘못된 토큰 보내기 → INVALID_TOKEN 확인  
            """
    )
    @PostMapping("/logout")
    public ApiResponse<EmployeeLogoutResponse> logout(@RequestBody @Valid EmployeeLogoutRequest req) {
        return ApiResponse.success(service.logout(req));
    }

    // =========================================================================
    // 공통 메서드
    // =========================================================================
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new IllegalArgumentException("유효하지 않은 Authorization 헤더입니다.");
        }
        return header.substring(7);
    }
}