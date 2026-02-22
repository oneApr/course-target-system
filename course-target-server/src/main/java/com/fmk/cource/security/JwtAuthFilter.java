package com.fmk.cource.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT 认证过滤器
 * 请求头格式：Authorization: Bearer <token>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    @Value("${jwt.header}")
    private String header;           // Authorization

    @Value("${jwt.prefix}")
    private String prefix;           // "Bearer "

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractToken(request);
        log.debug("[JwtFilter] URI={}, token={}", request.getRequestURI(),
                token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "null");

        if (StringUtils.hasText(token)) {
            boolean valid = jwtUtil.validateToken(token);
            log.debug("[JwtFilter] token有效={}", valid);

            if (valid) {
                try {
                    String username = jwtUtil.getUsernameFromToken(token);
                    String redisKey = "login:token:" + username;
                    String storedToken = redisTemplate.opsForValue().get(redisKey);
                    log.debug("[JwtFilter] username={}, Redis中有token={}", username, storedToken != null);

                    if (token.equals(storedToken)) {
                        Long userId = jwtUtil.getUserIdFromToken(token);
                        UserDetails userDetails = User.builder()
                                .username(username).password("")
                                .authorities(Collections.emptyList()).build();
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        auth.setDetails(userId);
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        log.debug("[JwtFilter] 认证成功, userId={}", userId);
                    } else {
                        log.warn("[JwtFilter] token与Redis不一致，请重新登录");
                    }
                } catch (Exception e) {
                    log.error("[JwtFilter] 认证异常: {}", e.getMessage());
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头提取 Token，严格要求 "Bearer " 前缀
     * 在 doc.html 使用🔒按钮填 token，Knife4j 会自动加 Bearer 前缀
     */
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader(header);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith(prefix)) {
            return authHeader.substring(prefix.length()).trim();
        }
        return null;
    }
}
