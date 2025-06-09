package com.yourcompany.pdfapp.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class SupabaseAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        String userIdHeader = request.getHeader("X-User-ID"); // 临时方案：从前端传递用户ID
        
        if (authHeader != null && authHeader.startsWith("Bearer ") && userIdHeader != null) {
            String token = authHeader.substring(7);
            
            // 临时简化验证：只要有token和用户ID就认为有效
            // TODO: 后续需要实现真正的JWT验证
            if (!token.isEmpty() && !userIdHeader.isEmpty()) {
                // 创建认证对象
                SupabaseUserPrincipal principal = new SupabaseUserPrincipal(userIdHeader, null);
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        principal, 
                        null, 
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                    );
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        // 跳过静态资源和公开API
        return path.startsWith("/h2-console") || 
               path.startsWith("/actuator") ||
               path.equals("/") ||
               path.startsWith("/public");
    }
} 