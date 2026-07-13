package com.aw.common.exception;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException {
    private final ErrorCode errorCode;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public AppException(ErrorCode errorCode, Object... args) {
        // Trộn param vào message gốc của Enum
        super(String.format(errorCode.getMessage(), args));
        this.errorCode = errorCode;
    }
}
