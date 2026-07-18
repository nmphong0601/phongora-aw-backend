package com.aw.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class CommonSecurity {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                // 1. Vô hiệu hóa CSRF vì chúng ta dùng Stateless JWT Token
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                // 2. Cấu hình phân quyền các luồng request
                .authorizeExchange(exchanges -> exchanges
                        // ĐÂY LÀ CHÌA KHÓA: Cho phép tất cả mọi người truy cập endpoint login/register công khai
                        .pathMatchers("/api/v1/auth/login", "/api/v1/auth/register").permitAll()

                        // Mọi request khác đều bắt buộc phải đi qua bộ lọc xác thực (Authenticated)
                        .anyExchange().authenticated()
                );

        return http.build();
    }
}
