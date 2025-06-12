package com.yourcompany.pdfapp.security;

import com.yourcompany.pdfapp.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class SupabaseAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(SupabaseAuthenticationFilter.class);
    
    @Autowired
    private JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            try {
                // 使用真正的JWT验证
                if (jwtService.validateToken(token)) {
                    Claims claims = jwtService.parseToken(token);
                    String userId = claims.getSubject();
                    String email = (String) claims.get("email");
                    
                // 创建认证对象
                    SupabaseUserPrincipal principal = new SupabaseUserPrincipal(userId, email);
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        principal, 
                            token, 
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                    );
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.debug("用户认证成功: {}", userId);
                } else {
                    logger.warn("JWT令牌验证失败");
                }
            } catch (Exception e) {
                logger.error("JWT处理异常: {}", e.getMessage());
                // 不设置认证，让请求继续，后续会被Spring Security拦截
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