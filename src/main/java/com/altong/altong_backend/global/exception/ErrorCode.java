package com.altong.altong_backend.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 공통(Global) 에러
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "G001", "잘못된 입력 값입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "G002", "서버 내부 오류입니다."),

    // 인증/인가(Auth)
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "A001", "아이디 또는 비밀번호가 올바르지 않습니다."),
    DUPLICATE_USERNAME(HttpStatus.CONFLICT, "A002", "이미 존재하는 사용자 이름입니다."),
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, "A003", "사용자를 찾을 수 없습니다."),

    // 직원(Employee)
    EMPLOYEE_NOT_FOUND(HttpStatus.NOT_FOUND, "E001", "직원 정보를 찾을 수 없습니다."),

    // 사장(Owner)
    OWNER_NOT_FOUND(HttpStatus.NOT_FOUND, "O001", "사장 정보를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
