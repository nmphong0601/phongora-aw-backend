package com.aw.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;
import java.util.function.Function;

@Component
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    // ==================== 1. CÁC HÀM RÚT TRÍCH THÔNG TIN (EXTRACTION) ====================

    public String getUsernameFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Rút trích UserId dạng UUID từ Token
     */
    public UUID getUserIdFromToken(String token) {
        String userIdStr = extractClaim(token, claims -> claims.get("userId", String.class));
        return userIdStr != null ? UUID.fromString(userIdStr) : null;
    }

    /**
     * Rút trích EmployeeId dạng UUID từ Token
     */
    public UUID getEmployeeIdFromToken(String token) {
        String employeeIdStr = extractClaim(token, claims -> claims.get("employeeId", String.class));
        return employeeIdStr != null ? UUID.fromString(employeeIdStr) : null;
    }

    /**
     * Rút trích danh sách Roles từ Token
     */
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        return extractClaim(token, claims -> claims.get("roles", List.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }


    // ==================== 2. CÁC HÀM SINH TOKEN (GENERATION) ====================

    /**
     * Hàm sinh Access Token
     */
    public String generateAccessToken(String username, UUID userId, UUID employeeId, String departmentCode, Set<String> roles) {
        Map<String, Object> claims = new HashMap<>();

        // Chuyển UUID thành String để tránh lỗi rườm rà khi Jackson serialize JSON
        claims.put("userId", userId != null ? userId.toString() : null);
        claims.put("employeeId", employeeId != null ? employeeId.toString() : null);
        claims.put("orgUnitCode", departmentCode != null && !departmentCode.isEmpty() ? departmentCode : null);
        claims.put("roles", roles);

        return generateToken(username, claims, jwtExpiration);
    }

    public String generateToken(String username, Map<String, Object> extraClaims, long expirationTime) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    // ==================== 3. CÁC HÀM KIỂM TRA (VALIDATION) ====================

    /**
     * Hàm validate token tổng quát cho JwtAuthenticationFilter
     */
    public boolean validateToken(String token) {
        try {
            // Nếu parse thành công và không bị expired, token hợp lệ
            return !isTokenExpired(token);
        } catch (Exception e) {
            // Bắt các lỗi như MalformedJwtException, SignatureException, ExpiredJwtException...
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
