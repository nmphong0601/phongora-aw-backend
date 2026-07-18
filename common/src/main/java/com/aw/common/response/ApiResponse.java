package com.aw.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL) // Ẩn các field null (ví dụ: lỗi thì không trả về field 'data')
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;

    // Constructor cho trường hợp thành công
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.code = 200; // Hoặc code thành công quy ước của bạn
        response.message = "Success";
        response.data = data;
        return response;
    }

    // Constructor cho trường hợp báo lỗi
    public static <T> ApiResponse<T> error(int code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.code = code;
        response.message = message;
        return response;
    }

    // Getters and Setters...
    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}
