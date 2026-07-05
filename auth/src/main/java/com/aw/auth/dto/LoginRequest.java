package com.aw.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Đối tượng chứa thông tin đăng nhập từ Client")
public class LoginRequest {

    @Schema(description = "Tên đăng nhập", example = "sys_admin", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @Schema(description = "Mật khẩu", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
