package com.altong.altong_backend.global.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {
    private final String code;
    private final String message;
    private final T data;

    public static <T> ApiResponse<T> of(String code,String message,T data) {
        return new ApiResponse<>(code,message,data);
    }

    public static <T> ApiResponse<T> success(T data){
        return of("SUCCESS","요청이 성공적으로 처리되었습니다.",data);
    }

    public static <T> ApiResponse<T> fail(String code,String message){
        return of(code,message,null);
    }


}
