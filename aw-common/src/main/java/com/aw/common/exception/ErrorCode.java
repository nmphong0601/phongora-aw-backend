package com.aw.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    // Các lỗi chung
    UNCATEGORIZED_EXCEPTION(500, "Lỗi hệ thống không xác định", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(400, "Dữ liệu đầu vào không hợp lệ", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(401, "Chưa xác thực hoặc Token hết hạn", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(403, "Không có quyền truy cập", HttpStatus.FORBIDDEN),

    // Các lỗi nghiệp vụ (Business Logic)
    USER_NOT_FOUND(404, "Không tìm thấy người dùng", HttpStatus.NOT_FOUND),
    EMPLOYEE_ALREADY_EXISTS(400, "Nhân viên đã tồn tại", HttpStatus.BAD_REQUEST),

    EMPLOYEE_NEW_CODE_NOT_GENERATED(40401, "Hệ thống chưa cấu hình Sequence cho công ty: %s", HttpStatus.EXPECTATION_FAILED),;

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public int getCode() { return code; }
    public String getMessage() { return message; }
    public HttpStatus getHttpStatus() { return httpStatus; }
}
