package com.aw.gateway.filter;

import com.aw.gateway.util.JwtUtils;
import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Predicate;

@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtils jwtUtils;

    // Danh sách các endpoints công khai không cần kiểm tra token
    private final List<String> openEndpoints = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh-token",
            "/api/v1/auth/api-docs"
    );

    public AuthenticationFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

//    private final Predicate<ServerHttpRequest> isSecured = request -> openEndpoints.stream()
//            .noneMatch(uri -> request.getURI().getPath().contains(uri));
    private final Predicate<ServerHttpRequest> isSecured = request -> {
        String path = request.getURI().getPath();

        // Nếu bắt đầu bằng /camunda -> Bỏ qua kiểm tra (trả về false vì nó KHÔNG PHẢI là secured)
        if (path.startsWith("/camunda")) {
            return false;
        }

        // Nếu khớp với các endpoint công khai khác -> Bỏ qua kiểm tra (trả về false)
        boolean isOpenEndpoint = openEndpoints.stream().anyMatch(path::contains);

        // Nếu không nằm trong 2 trường hợp trên -> Phải kiểm tra bảo mật (trả về true)
        return !isOpenEndpoint;
    };

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // 1. Kiểm tra xem endpoint có cần bảo mật hay không
        if (isSecured.test(request)) {
            // 2. Kiểm tra sự tồn tại của Header Authorization
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            // 3. Kiểm tra tính hợp lệ và hạn dùng của Token
            if (jwtUtils.isTokenExpired(token)) {
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            // 4. Trích xuất thông tin và chuyển tiếp qua Header nội bộ (Mutate Request)
            Claims claims = jwtUtils.getClaims(token);
            request = exchange.getRequest().mutate()
                    .header("X-User-Id", claims.getSubject())
                    .header("X-User-Roles", String.valueOf(claims.get("roles")))
                    .build();

            return chain.filter(exchange.mutate().request(request).build());
        }

        return chain.filter(exchange);
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return -1; // Đảm bảo bộ lọc này luôn chạy đầu tiên trong chuỗi
    }
}
