package com.altong.altong_backend.global.exception.domain.owner;

import com.altong.altong_backend.global.exception.BusinessException;
import com.altong.altong_backend.global.exception.ErrorCode;

public class OwnerException extends BusinessException {
    public OwnerException(ErrorCode errorCode) {
        super(errorCode);
    }
}
