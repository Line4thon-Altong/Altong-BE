package com.altong.altong_backend.global.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenProvider provider;

    public JwtAuthFilter(JwtTokenProvider provider) {
        this.provider = provider;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest req,
            HttpServletResponse res,
            FilterChain chain
    ) throws ServletException, IOException {

        String auth = req.getHeader("Authorization");

        if (StringUtils.hasText(auth) && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            try {
                // 토큰 검증 + 인증 객체 생성 + 시큐리티 컨텍스트 등록
                Authentication authentication = provider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (ExpiredJwtException e) {
                // 만료된 토큰이면 인증 정보만 비우고, 요청은 그대로 진행
                SecurityContextHolder.clearContext();
            } catch (JwtException | IllegalArgumentException e) {
                // 위조 / 파싱 실패 등도 마찬가지로 인증만 비우고 진행
                SecurityContextHolder.clearContext();
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
            }
        }

        // 필터 체인 계속 진행 (에러가 있어도 컨트롤러까지 도달하도록)
        chain.doFilter(req, res);
    }
}