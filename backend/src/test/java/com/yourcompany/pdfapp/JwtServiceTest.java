package com.yourcompany.pdfapp;

import com.yourcompany.pdfapp.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {
    
    private JwtService jwtService;
    private String testSecret = "test-secret-key-for-jwt-validation-must-be-long-enough-for-hmac-sha256";
    
    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", testSecret);
    }
    
    @Test
    void testValidateValidToken() {
        // 创建有效的JWT
        String validToken = createTestToken("test-user", "test@example.com", false);
        
        // 测试验证
        Boolean isValid = jwtService.validateToken(validToken);
        assertTrue(isValid, "有效的JWT应该通过验证");
        
        // 测试提取用户信息
        String userId = jwtService.extractUserId(validToken);
        assertEquals("test-user", userId);
        
        String email = jwtService.extractEmail(validToken);
        assertEquals("test@example.com", email);
    }
    
    @Test
    void testValidateExpiredToken() {
        // 创建过期的JWT
        String expiredToken = createTestToken("test-user", "test@example.com", true);
        
        // 测试验证
        Boolean isValid = jwtService.validateToken(expiredToken);
        assertFalse(isValid, "过期的JWT应该验证失败");
    }
    
    @Test
    void testValidateInvalidToken() {
        // 测试无效的JWT
        Boolean isValid = jwtService.validateToken("invalid.jwt.token");
        assertFalse(isValid, "无效的JWT应该验证失败");
    }
    
    @Test
    void testParseValidToken() {
        // 创建有效的JWT
        String validToken = createTestToken("test-user", "test@example.com", false);
        
        // 解析JWT
        Claims claims = jwtService.parseToken(validToken);
        
        assertNotNull(claims);
        assertEquals("test-user", claims.getSubject());
        assertEquals("test@example.com", claims.get("email"));
        assertEquals("supabase", claims.getIssuer());
    }
    
    private String createTestToken(String userId, String email, boolean expired) {
        SecretKey key = Keys.hmacShaKeyFor(testSecret.getBytes());
        
        Date now = new Date();
        Date expiration = expired ? 
            new Date(now.getTime() - 3600000) :  // 1小时前（过期）
            new Date(now.getTime() + 3600000);   // 1小时后（有效）
        
        return Jwts.builder()
                .subject(userId)
                .claim("email", email)
                .issuer("supabase")
                .audience().add("authenticated").and()
                .issuedAt(now)
                .expiration(expiration)
                .signWith(key)
                .compact();
    }
} 