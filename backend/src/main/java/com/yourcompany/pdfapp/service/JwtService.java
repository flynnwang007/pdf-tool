package com.yourcompany.pdfapp.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);
    
    @Value("${supabase.jwt.secret:your-supabase-jwt-secret-here}")
    private String jwtSecret;
    
    public String extractUserId(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    public String extractEmail(String token) {
        return extractClaim(token, claims -> (String) claims.get("email"));
    }
    
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    private Claims extractAllClaims(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            logger.error("JWT解析失败: {}", e.getMessage());
            throw new IllegalArgumentException("无效的JWT令牌", e);
        }
    }
    
    private Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            logger.error("检查JWT过期时间失败: {}", e.getMessage());
            return true;
        }
    }
    
    public Boolean validateToken(String token) {
        try {
            logger.debug("开始验证JWT令牌，长度: {}", token.length());
            
            // 检查令牌格式和签名
            Claims claims = extractAllClaims(token);
            
            // 检查过期时间
            if (isTokenExpired(token)) {
                logger.warn("JWT令牌已过期");
                return false;
            }
            
            // 检查角色和用户ID
            String role = (String) claims.get("role");
            String userId = claims.getSubject();
            
            // 临时允许anon角色访问（用于测试）
            if ("anon".equals(role)) {
                logger.debug("临时允许anon角色访问");
                return true;
            }
            
            // 检查必要字段
            if (userId == null || userId.trim().isEmpty()) {
                logger.warn("JWT令牌缺少用户ID");
                return false;
            }
            
            // 检查发行者（可选）
            String issuer = claims.getIssuer();
            if (issuer != null && !issuer.equals("supabase")) {
                logger.debug("JWT发行者: {}", issuer);
            }
            
            logger.debug("JWT令牌验证成功，用户ID: {}, 角色: {}", userId, role);
            return true;
            
        } catch (Exception e) {
            logger.error("JWT令牌验证失败: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            return false;
        }
    }
    
    public Claims parseToken(String token) {
        if (!validateToken(token)) {
            throw new IllegalArgumentException("无效的JWT令牌");
        }
        return extractAllClaims(token);
    }
    
    /**
     * 生成仅用于文件下载的 JWT token
     * @param fileId 文件ID
     * @param expiresInSeconds 有效期（秒）
     * @return token字符串
     */
    public String generateDownloadToken(String fileId, long expiresInSeconds) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiresInSeconds * 1000);
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        return Jwts.builder()
                .setSubject(fileId)
                .setExpiration(expiry)
                .claim("type", "download")
                .signWith(key)
                .compact();
    }

    /**
     * 校验下载token是否有效
     * @param token token字符串
     * @param fileId 文件ID
     * @return 是否有效
     */
    public boolean validateDownloadToken(String token, String fileId) {
        try {
            Claims claims = extractAllClaims(token);
            boolean expired = claims.getExpiration().before(new Date());
            boolean match = fileId.equals(claims.getSubject());
            boolean isDownload = "download".equals(claims.get("type"));
            return !expired && match && isDownload;
        } catch (Exception e) {
            logger.error("下载token校验失败: {}", e.getMessage());
            return false;
        }
    }
} 