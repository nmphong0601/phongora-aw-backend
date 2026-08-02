package com.aw.hr.config;

import com.aw.common.security.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Configuration
public class RestClientConfig {

    @Bean
    public RestTemplate restTemplate(JwtService jwtService) {
        RestTemplate restTemplate = new RestTemplate();

        // Định nghĩa Interceptor để chèn Token tự động
        ClientHttpRequestInterceptor bearerTokenInterceptor = (
                request,
                body,
                execution) -> {
            // 1. Tự động lấy systemToken mỗi khi có request gửi đi
            String systemToken = jwtService.generateSystemToken();

            // 2. Thêm Authorization: Bearer <token> vào Header
            request.getHeaders().setBearerAuth(systemToken);

            // 3. Cho phép request tiếp tục dòng chảy
            return execution.execute(request, body);
        };

        // Đăng ký interceptor vào RestTemplate
        restTemplate.setInterceptors(Collections.singletonList(bearerTokenInterceptor));

        return restTemplate;
    }
}
