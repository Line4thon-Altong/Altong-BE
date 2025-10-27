package com.altong.altong_backend.global.exception.domain.employee;

import com.altong.altong_backend.global.exception.BusinessException;
import com.altong.altong_backend.global.exception.ErrorCode;

public class EmployeeException extends BusinessException {
    public EmployeeException(ErrorCode errorCode) {
        super(errorCode);
    }
}
