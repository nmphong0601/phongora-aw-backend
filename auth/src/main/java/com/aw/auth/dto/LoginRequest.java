package com.aw.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Đối tượng chứa thông tin đăng nhập từ Client")
public class LoginRequest {

    @Schema(description = "Tên đăng nhập", example = "sys_admin", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @Schema(description = "Mật khẩu", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @Schema(description = "Ghi nhớ?", example = "true", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private boolean remember;
}
