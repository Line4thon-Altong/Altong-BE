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
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A004", "토큰이 유효하지 않습니다."),
    UNAUTHORIZED_ROLE(HttpStatus.FORBIDDEN, "A005", "현재 계정은 이 기능을 사용할 수 없습니다."),
    UNAUTHORIZED_ACTION(HttpStatus.FORBIDDEN, "A006", "해당 리소스에 접근할 권한이 없습니다."),

    // 직원(Employee)
    EMPLOYEE_NOT_FOUND(HttpStatus.NOT_FOUND, "E001", "직원 정보를 찾을 수 없습니다."),
    EMPLOYEE_NOT_BELONG_TO_STORE(HttpStatus.FORBIDDEN, "E002", "해당 매장의 직원이 아닙니다."),

    // 사장(Owner)
    OWNER_NOT_FOUND(HttpStatus.NOT_FOUND, "O001", "사장 정보를 찾을 수 없습니다."),

    // 가게
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "S001", "가게 정보를 찾을 수 없습니다."),

    // 스케줄
    SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "SC001", "스케줄 정보를 찾을 수 없습니다."),
    SCHEDULE_NOT_BELONG_TO_STORE(HttpStatus.FORBIDDEN, "SC002", "해당 매장의 스케줄이 아닙니다."),
    SCHEDULE_NOT_FOUND_TODAY(HttpStatus.NOT_FOUND, "SC003", "오늘 근무 예정이 없습니다."),
    ALREADY_CHECKED_IN(HttpStatus.BAD_REQUEST, "SC004", "이미 출근 처리되었습니다."),
    ALREADY_CHECKED_OUT(HttpStatus.BAD_REQUEST,"SC005","이미 퇴근 처리되었습니다."),

    // 메뉴얼
    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "T001","DB 저장 중 오류가 발생했습니다."),
    MANUAL_NOT_FOUND(HttpStatus.NOT_FOUND, "T002", "메뉴얼을 찾을 수 없습니다."),
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "T003", "해당 메뉴얼에 접근할 권한이 없습니다."),

    // 퀴즈
    QUIZ_NOT_FOUND(HttpStatus.NOT_FOUND, "Q001", "해당 퀴즈를 찾을 수 없습니다."),

    // AI
    EXTERNAL_API_ERROR(HttpStatus.BAD_GATEWAY, "AI001", "AI 서버 호출 중 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
