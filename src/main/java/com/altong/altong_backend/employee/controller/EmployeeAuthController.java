package com.altong.altong_backend.employee.controller;

import com.altong.altong_backend.global.response.ApiResponse;
import com.altong.altong_backend.employee.service.EmployeeAuthService;
import com.altong.altong_backend.employee.dto.request.*;
import com.altong.altong_backend.employee.dto.response.*;
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

/**
 * EmployeeAuthController
 *
 * 알바(EMPLOYEE) 회원의 로그인 / 비밀번호 변경 / 가게 연동 해제 / 로그아웃 관련 API
 * Swagger 문서화로 제약조건 / 예외상황 / 테스트 방법을 상세히 기술함.
 */
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Tag(name = "Employee.Auth API", description = "알바(EMPLOYEE) 인증 및 계정 관리 API")
public class EmployeeAuthController {

    private final EmployeeAuthService service;
    private final JwtTokenProvider jwt;

    // ===============================
    // 1️. 로그인
    // ===============================
    @Operation(
        summary = "알바 로그인",
        description = """
        ### 개요  
        - 알바 계정 로그인 시 AccessToken / RefreshToken을 발급합니다.

        ### 제약조건  
        - `username`, `password` 필수 입력
        - 잘못된 비밀번호 시 401 Unauthorized

        ### 예외상황  
        | 코드 | 상태 | 설명 |
        |------|------|------|
        | `A003` | 404 | 사용자 정보 없음 |
        | `A004` | 401 | 비밀번호 불일치 |
        | `G001` | 400 | 필수값 누락 |

        ### 테스트 방법  
        ```json
        {
          "username": "emp01",
          "password": "abcd1234!"
        }
        ```
        """
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "로그인 성공",
            content = @Content(schema = @Schema(implementation = EmployeeLoginResponse.class),
                examples = @ExampleObject(value = """
                {
                  "code": "SUCCESS",
                  "message": "요청이 성공적으로 처리되었습니다.",
                  "data": {
                    "accessToken": "eyJhbGciOi...",
                    "refreshToken": "eyJhbGciOi...",
                    "username": "emp01",
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
    public ApiResponse<EmployeeLoginResponse> login(@RequestBody @Valid EmployeeLoginRequest req) {
        return ApiResponse.success(service.login(req));
    }

    // ===============================
// 2️. 비밀번호 변경
// ===============================
    @Operation(
        summary = "비밀번호 변경",
        description = """
        ### 개요  
        - 알바가 자신의 비밀번호를 변경합니다.
        - JWT Access Token을 통해 로그인한 알바의 ID를 식별합니다.

        ### 제약조건  
        - 헤더: `Authorization: Bearer {accessToken}` 필수  
        - Body: `oldPassword`, `newPassword` 필수

        ### 예외상황  
        | 코드 | 상태 | 설명 |
        |------|------|------|
        | `A004` | 401 | 기존 비밀번호 불일치 |
        | `A003` | 404 | 사용자 없음 |
        | `G001` | 400 | 잘못된 요청 |

        ### 테스트 방법  
        1. **/api/employees/login** 호출 후 AccessToken 발급  
        2. Swagger 상단의 **Authorize** 버튼 클릭 →  
           `"Bearer eyJhbGciOi..."` 형식으로 토큰 입력  
        3. 아래 JSON Body 전송  
    
        ```json
        {
          "oldPassword": "abcd1234!",
          "newPassword": "newPass!123"
        }
        ```
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
    public ApiResponse<EmployeePasswordUpdateResponse> updatePassword(
        HttpServletRequest request,
        @RequestBody @Valid EmployeePasswordUpdateRequest req
    ) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("유효하지 않은 Authorization 헤더입니다.");
        }

        String token = authHeader.substring(7);  // "Bearer " 제거
        Long empId = jwt.getEmployeeIdFromToken(token);

        return ApiResponse.success(service.updatePassword(empId, req));
    }

    // ===============================
    // 3️. 가게 연동 해제
    // ===============================
    @Operation(
        summary = "가게 연동 해제",
        description = """
        ### 개요  
        - 알바가 기존에 연동된 가게(store)와의 연결을 해제합니다.

        ### 제약조건  
        - Path: `employeeId` 필수  
        - Query: `storeId` 필수  

        ### 예외상황  
        | 코드 | 상태 | 설명 |
        |------|------|------|
        | `A003` | 404 | 사용자 혹은 가게 정보 없음 |
        | `G001` | 400 | 요청 형식 오류 |

        ### 테스트 방법  
        - URL 예시  
        ```
        DELETE /api/employees/2/unlink-store?storeId=5
        ```
        """
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "가게 연동 해제 성공",
            content = @Content(examples = @ExampleObject(value = """
                { "code": "SUCCESS", "message": "가게 연동이 해제되었습니다.", "data": null }
                """))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "대상 없음",
            content = @Content(examples = @ExampleObject(value = """
                { "code": "A003", "message": "해당 알바 또는 가게를 찾을 수 없습니다.", "data": null }
                """))
        )
    })
    @DeleteMapping("/{employeeId}/unlink-store")
    public ApiResponse<EmployeeUnlinkStoreResponse> unlinkStore(
        @PathVariable Long employeeId,
        @RequestParam Long storeId
    ) {
        EmployeeUnlinkStoreRequest req = new EmployeeUnlinkStoreRequest(employeeId, storeId);
        return ApiResponse.success(service.unlinkStore(req));
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
    public ApiResponse<EmployeeLogoutResponse> logout(@RequestBody @Valid EmployeeLogoutRequest req) {
        return ApiResponse.success(service.logout(req));
    }
}
