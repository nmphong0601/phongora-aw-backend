package com.aw.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // Class tiện ích xử lý giải mã JWT (bạn sẽ tự viết hoặc đã có sẵn trong dự án)
    private final JwtService jwtService;

    private final StringRedisTemplate redisTemplate;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Skip JWT validation for Camunda and engine-rest endpoints
        return path.startsWith("/camunda")
                || path.startsWith("/camunda-welcome")
                || path.startsWith("/engine-rest");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            final String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
            }

            // 1. Lấy JWT từ Header của request
            String jwt = getJwtFromRequest(request);

            // KIỂM TRA TRONG REDIS
            Boolean isBlocked = redisTemplate.hasKey("blocklist:" + jwt);
            if (isBlocked) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token has been revoked");
                return; // Chặn request ngay lập tức
            }

            // 2. Nếu có token và token hợp lệ
            if (StringUtils.hasText(jwt) && jwtService.validateToken(jwt)) {

                // 3. Rút trích thông tin từ Claims của Token
                UUID userId = jwtService.getUserIdFromToken(jwt);
                String username = jwtService.getUsernameFromToken(jwt);
                String employeeCode = jwtService.getEmployeeCodeFromToken(jwt);
                String orgUnitCode = jwtService.getOrgUnitCodeFromToken(jwt);
                List<String> roles = jwtService.getRolesFromToken(jwt); // VD: ["ROLE_CEO", "ROLE_USER"]

                // 4. Chuyển đổi roles thành định dạng mà Spring Security hiểu
                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                // 5. Khởi tạo đối tượng UserPrincipal
                UserPrincipal principal = UserPrincipal.builder()
                        .userId(userId)
                        .username(username)
                        .employeeCode(employeeCode)
                        .orgUnitCode(orgUnitCode)
                        .authorities(authorities)
                        .build();

                // 6. Tạo thẻ xác thực (Authentication Token)
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(principal, null, authorities);

                // Cung cấp thêm chi tiết về request (IP, Session...) cho Spring
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 7. Đẩy thẻ xác thực vào Context của Thread hiện tại
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Đã xác thực thành công user: {}", username);
            }
        } catch (Exception ex) {
            log.error("Không thể xác thực user bằng JWT", ex);
            // Không ném Exception ra ngoài để tránh crash request, cứ để luồng chạy tiếp.
            // Nếu API yêu cầu quyền, Spring Security sẽ tự chặn lại ở bước sau và trả về 401/403.
        }

        // Chuyển request cho các filter tiếp theo
        filterChain.doFilter(request, response);
    }

    /**
     * Hàm phụ trợ lấy token từ Header
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        // Kiểm tra xem header có chứa chuỗi "Bearer " không
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Cắt bỏ chữ "Bearer " (7 ký tự) để lấy phần token nguyên chất
        }
        return null;
    }
}
