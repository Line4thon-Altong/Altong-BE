package com.altong.altong_backend.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 공통(Global) 에러
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST,"G001","잘못된 입력 값입니다.");
    // 추후 도메인별 에러 추가

    private final HttpStatus status;
    private final String code;
    private final String message;
}
