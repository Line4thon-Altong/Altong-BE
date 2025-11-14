package com.altong.altong_backend.global.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-exp}")
    private long accessExp;

    private Key key;

    @PostConstruct
    void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /** 액세스 토큰 생성 */
    public String createAccessToken(String subject, String role) {
        return Jwts.builder()
            .setSubject(subject)
            .claim("role", role)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + accessExp))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
    }

    /** 리프레시 토큰 생성 */
    public String createRefreshToken(String subject) {
        return Jwts.builder()
            .setSubject(subject)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + accessExp * 24))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
    }

    /** 파싱/검증 */
    public Jws<Claims> parse(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token);
    }

    /** 인증 객체 생성 */
    public Authentication getAuthentication(String token) {
        Jws<Claims> claimsJws = parse(token);
        Claims claims = claimsJws.getBody();

        String subject = claims.getSubject();
        String role = claims.get("role", String.class);

        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
        return new UsernamePasswordAuthenticationToken(subject, null, authorities);
    }

    /** 토큰에서 알바 ID 추출 */
    public Long getEmployeeIdFromToken(String token) {
        try {
            Claims claims = parse(token).getBody();
            String subject = claims.getSubject(); // 예: "EMPLOYEE:3"

            if (subject == null || !subject.startsWith("EMPLOYEE:")) {
                throw new IllegalArgumentException("EMPLOYEE 토큰이 아닙니다.");
            }

            String idPart = subject.substring("EMPLOYEE:".length());
            return Long.parseLong(idPart);
        } catch (Exception e) {
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }
    }

    /** 토큰에서 사장 ID 추출 */
    public Long getOwnerIdFromToken(String token) {
        try {
            Claims claims = parse(token).getBody();
            String subject = claims.getSubject(); // 예: "OWNER:1"

            if (subject == null || !subject.startsWith("OWNER:")) {
                throw new IllegalArgumentException("OWNER 토큰이 아닙니다.");
            }

            String idPart = subject.substring("OWNER:".length());
            return Long.parseLong(idPart);
        } catch (Exception e) {
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }
    }
}