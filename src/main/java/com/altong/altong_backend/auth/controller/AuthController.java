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
 * 사장/알바 공통: 회원가입 / JWT 재발급 / 내 정보 조회
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Auth API", description = "회원가입 / JWT 재발급 / 내 정보 조회 API")
public class AuthController {

    private final AuthService authService;

    // =========================================================================
    // 1. 회원가입
    // =========================================================================
    @Operation(
        summary = "회원가입 (사장 / 알바)",
        description = """
        # 개요
        역할(role)에 따라 **사장(OWNER)** 또는 **알바(EMPLOYEE)** 계정을 생성합니다.  
        생성된 사용자 정보는 `SignupResponse` 형태로 응답됩니다.

        ---

        # 요청 제약조건

        |필드|설명|
        |----|----|
        |`role`|OWNER / EMPLOYEE (**필수**)|
        |`username`|4~50자, 고유해야 함(**필수**)|
        |`password`|8~200자(**필수**)|
        |`storeName`|OWNER일 때 필수|
        |`name`|EMPLOYEE일 때 필수|

        DTO 유효성 검증 실패 시 `G001 (INVALID_INPUT_VALUE)` 발생

        ---

        # 예시 요청 (사장)
        ```json
        {
          "role": "OWNER",
          "username": "owner01",
          "password": "abcd1234!",
          "storeName": "알통치킨 평택점"
        }
        ```

        # 예시 요청 (알바)
        ```json
        {
          "role": "EMPLOYEE",
          "username": "emp01",
          "password": "abcd1234!",
          "name": "홍길동"
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
            "role": "OWNER",
            "storeId": 10,
            "storeName": "알통치킨 평택점",
            "createdAt": "2025-11-14T10:00:00"
          }
        }
        ```

        ---

        # 에러 코드 / 예외 상황

        |코드|HTTP|설명|
        |----|----|----|
        |`G001`|400|필수값 누락 / role 잘못됨 / name 또는 storeName 미입력 / 형식 오류|
        |`A002 (DUPLICATE_USERNAME)`|409|username 중복|
        |`A003`|404|내부 로직 사용자 조회 실패|

        ---

        # 테스트 방법
        1. OWNER 요청 전송 → 가게 생성 + owner 생성 정상 확인  
        2. EMPLOYEE 요청 전송 → name 필수 확인  
        3. username 중복으로 요청 → 409 오류 확인  
        4. role 값을 잘못 보내보기(MANAGER 등) → 400 오류 확인  
        """
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "회원가입 성공",
            content = @Content(schema = @Schema(implementation = SignupResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "아이디 중복",
            content = @Content(examples = @ExampleObject("""
                { "code":"A002", "message":"이미 사용 중인 사용자명입니다.", "data":null }
            """))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "입력 오류",
            content = @Content(examples = @ExampleObject("""
                { "code":"G001", "message":"유효하지 않은 입력값입니다.", "data":null }
            """))
        )
    })
    @PostMapping("/signup")
    public ApiResponse<SignupResponse> signup(@RequestBody @Valid SignupRequest req) {
        return ApiResponse.success(authService.signup(req));
    }

    // =========================================================================
    // 2. AccessToken 재발급
    // =========================================================================
    @Operation(
        summary = "Access Token 재발급",
        description = """
        # 개요
        만료된 AccessToken을 RefreshToken을 사용해 재발급합니다.

        ---

        # 요청 형식
        ```json
        { "refreshToken": "eyJh..." }
        ```

        refreshToken 필드 누락 시 → `400 INVALID_INPUT_VALUE`

        ---

        # 성공 응답
        ```json
        {
          "code": "SUCCESS",
          "data": { "accessToken": "새로운 AccessToken..." }
        }
        ```

        ---

        # 에러 코드 / 예외 상황

        |코드|HTTP|설명|
        |----|----|----|
        |`INVALID_CREDENTIALS`|401|RefreshToken 만료 / 위조 / 파싱 실패|
        |`G001`|400|refreshToken 필드 누락|

        ---

        # 테스트 방법
        1. 로그인 후 받은 RefreshToken 전달  
        2. 토큰 일부 삭제 후 재요청 → INVALID_CREDENTIALS 유도  
        3. refreshToken을 빈 문자열로 보내기 → INVALID_INPUT_VALUE  
        """
    )
    @PostMapping("/refresh")
    public ApiResponse<Map<String, String>> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        String newAccess = authService.refreshAccessToken(refreshToken);
        return ApiResponse.success(Map.of("accessToken", newAccess));
    }

    // =========================================================================
    // 3. 내 정보 조회
    // =========================================================================
    @Operation(
        summary = "내 정보 조회",
        description = """
        # 개요
        AccessToken 기반으로 현재 로그인한 사용자 정보를 조회합니다.  
        OWNER/EMPLOYEE에 따라 displayName이 다르게 설정됩니다.

        ---

        # 응답 스키마

        |필드|설명|
        |----|----|
        |id|사용자 PK|
        |username|로그인 ID|
        |displayName|OWNER=가게명, EMPLOYEE=본명|
        |storeId|소속 가게 PK|
        |storeName|가게명|
        |role|OWNER or EMPLOYEE|

        ---

        # 성공 예시
        ```json
        {
          "code": "SUCCESS",
          "data": {
            "id": 3,
            "username": "owner01",
            "displayName": "알통치킨 평택점",
            "storeId": 10,
            "storeName": "알통치킨 평택점",
            "role": "OWNER"
          }
        }
        ```

        ---

        # 에러 코드 / 예외 상황

        |코드|HTTP|설명|
        |----|----|----|
        |`A003 (OWNER_NOT_FOUND)`|404|OWNER 조회 실패|
        |`A003 (EMPLOYEE_NOT_FOUND)`|404|EMPLOYEE 조회 실패|
        |`INVALID_CREDENTIALS`|401|AccessToken 만료 / 위조|
        |`G001`|400|Authorization 헤더 없음 or 형식 불일치|

        ---

        # 테스트 방법
        1. 로그인 → AccessToken 획득  
        2. Swagger Authorize 버튼 → Bearer {token} 입력  
        3. `/me` 호출 → 정상 응답 확인  
        4. 토큰 일부 조작해서 401 오류 확인  
        5. 헤더 제거 후 호출 → 403 또는 401 확인  
        """
    )
    @GetMapping("/me")
    public ApiResponse<UserInfoResponse> getMyInfo(Authentication auth) {
        return ApiResponse.success(authService.getCurrentUserInfo(auth));
    }
}