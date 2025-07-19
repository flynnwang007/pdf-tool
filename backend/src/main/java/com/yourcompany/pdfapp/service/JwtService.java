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

    @Value("${MEMFIRE_JWT_SECRET:0d37e31b-3452-4949-8e19-04bc619c78c9}")
    private String memfireJwtSecret;
    
    public String extractUserId(String token) {
        return extractClaim(token, claims -> {
            String sub = claims.getSubject();
            if (sub != null && !sub.trim().isEmpty()) return sub;
            Object uid = claims.get("uid");
            return uid != null ? uid.toString() : null;
        });
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
        // 先用supabase.jwt.secret验，失败再用memfireJwtSecret
        Exception lastException = null;
        for (String secret : new String[]{jwtSecret, memfireJwtSecret}) {
            try {
                SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
                return Jwts.parser()
                        .verifyWith(key)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();
            } catch (JwtException e) {
                lastException = e;
                logger.warn("JWT解析失败，尝试下一个秘钥: {}", e.getMessage());
            }
        }
        logger.error("所有JWT秘钥均无法解析token");
        throw new IllegalArgumentException("无效的JWT令牌", lastException);
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
            Claims claims = extractAllClaims(token);
            if (isTokenExpired(token)) {
                logger.warn("JWT令牌已过期");
                return false;
            }
            String role = (String) claims.get("role");
            String userId = extractUserId(token);
            if ("anon".equals(role)) {
                logger.debug("临时允许anon角色访问");
                return true;
            }
            if (userId == null || userId.trim().isEmpty()) {
                logger.warn("JWT令牌缺少用户ID");
                return false;
            }
            String issuer = claims.getIssuer();
            if (issuer != null && !issuer.equals("supabase") && !issuer.equals("memfiredb")) {
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