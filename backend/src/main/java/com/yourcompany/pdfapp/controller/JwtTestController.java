package com.yourcompany.pdfapp.controller;

import com.yourcompany.pdfapp.service.JwtService;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/public/jwt")
public class JwtTestController {
    
    @Autowired
    private JwtService jwtService;
    
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateJwt(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String token = request.get("token");
            if (token == null || token.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "JWT令牌不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 验证JWT
            boolean isValid = jwtService.validateToken(token);
            response.put("isValid", isValid);
            
            if (isValid) {
                // 解析JWT内容
                Claims claims = jwtService.parseToken(token);
                response.put("success", true);
                response.put("message", "JWT验证成功");
                response.put("userId", claims.getSubject());
                response.put("email", claims.get("email"));
                response.put("issuer", claims.getIssuer());
                response.put("audience", claims.getAudience());
                response.put("issuedAt", claims.getIssuedAt());
                response.put("expiration", claims.getExpiration());
            } else {
                response.put("success", false);
                response.put("message", "JWT验证失败");
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "JWT验证异常: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
        }
        
        return ResponseEntity.ok(response);
    }
} 