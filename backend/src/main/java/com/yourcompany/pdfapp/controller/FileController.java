package com.yourcompany.pdfapp.controller;

import com.yourcompany.pdfapp.model.FileEntity;
import com.yourcompany.pdfapp.security.SupabaseUserPrincipal;
import com.yourcompany.pdfapp.service.FileService;
import com.yourcompany.pdfapp.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.io.UnsupportedEncodingException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://www.aibase123.cn:8081"})
public class FileController {
    
    @Autowired
    private FileService fileService;
    
    @Autowired
    private JwtService jwtService;
    
    // 获取当前认证用户
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SupabaseUserPrincipal) {
            return ((SupabaseUserPrincipal) authentication.getPrincipal()).getUserId();
        }
        return null;
    }
    
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "fileName", required = false) String fileName,
            @RequestParam(value = "fileType", required = false) String fileType,
            @RequestParam(value = "fileSize", required = false) Long fileSize) {
        
        Map<String, Object> response = new HashMap<>();
        String userId = getCurrentUserId();
        
        if (userId == null) {
            response.put("success", false);
            response.put("message", "用户未认证");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        try {
            // 兼容小程序：优先使用 formData 中的字段，否则使用 MultipartFile 的属性
            String finalFileName = (fileName != null && !fileName.isEmpty()) ? fileName : file.getOriginalFilename();
            String finalFileType = (fileType != null && !fileType.isEmpty()) ? fileType : file.getContentType();
            Long finalFileSize = (fileSize != null) ? fileSize : file.getSize();
            
            FileEntity savedFile = fileService.saveFile(file, userId, finalFileName, finalFileType, finalFileSize);
            
            response.put("success", true);
            response.put("message", "文件上传成功");
            response.put("data", Map.of(
                "fileId", savedFile.getId(),
                "fileName", savedFile.getOriginalName(),
                "fileSize", savedFile.getFileSize(),
                "fileType", savedFile.getFileType(),
                "uploadTime", savedFile.getCreatedAt()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "文件保存失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllFiles() {
        Map<String, Object> response = new HashMap<>();
        String userId = getCurrentUserId();
        
        if (userId == null) {
            response.put("success", false);
            response.put("message", "用户未认证");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        try {
            List<FileEntity> files = fileService.getFilesByUser(userId);
            
            response.put("success", true);
            response.put("data", files);
            response.put("total", files.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取文件列表失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/{fileId}")
    public ResponseEntity<Map<String, Object>> getFile(@PathVariable Long fileId) {
        Map<String, Object> response = new HashMap<>();
        String userId = getCurrentUserId();
        
        if (userId == null) {
            response.put("success", false);
            response.put("message", "用户未认证");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        try {
            Optional<FileEntity> file = fileService.getFileByUserAndId(userId, fileId);
            
            if (file.isPresent()) {
                response.put("success", true);
                response.put("data", file.get());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "文件不存在或无权访问");
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取文件信息失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) {
        String userId = getCurrentUserId();
        
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            Optional<FileEntity> fileEntity = fileService.getFileByUserAndId(userId, fileId);
            
            if (fileEntity.isPresent()) {
                FileEntity file = fileEntity.get();
                byte[] fileContent = fileService.getFileContent(fileId);
                
                ByteArrayResource resource = new ByteArrayResource(fileContent);
                
                // 正确处理中文文件名
                String encodedFileName;
                try {
                    encodedFileName = java.net.URLEncoder.encode(file.getOriginalName(), "UTF-8")
                        .replace("+", "%20"); // URL编码会把空格变成+，但在文件名中应该是%20
                } catch (UnsupportedEncodingException e) {
                    // UTF-8编码应该始终支持，这里作为备选方案
                    encodedFileName = file.getOriginalName().replaceAll("[^\\w.-]", "_");
                }
                
                return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(file.getMimeType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                           "attachment; filename*=UTF-8''" + encodedFileName)
                    .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @DeleteMapping("/{fileId}")
    public ResponseEntity<Map<String, Object>> deleteFile(@PathVariable Long fileId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            fileService.deleteFile(fileId);
            
            response.put("success", true);
            response.put("message", "文件删除成功");
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "文件删除失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/types/{fileType}")
    public ResponseEntity<Map<String, Object>> getFilesByType(@PathVariable String fileType) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<FileEntity> files = fileService.getFilesByType(fileType.toUpperCase());
            
            response.put("success", true);
            response.put("data", files);
            response.put("total", files.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取文件列表失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 生成带token的下载链接
     * POST /api/files/{fileId}/generate-download-url
     */
    @PostMapping("/{fileId}/generate-download-url")
    public ResponseEntity<Map<String, Object>> generateDownloadUrl(@PathVariable Long fileId) {
        Map<String, Object> response = new HashMap<>();
        String userId = getCurrentUserId();
        if (userId == null) {
            response.put("success", false);
            response.put("message", "用户未认证");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        // 检查文件归属
        Optional<FileEntity> fileOpt = fileService.getFileByUserAndId(userId, fileId);
        if (fileOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "文件不存在或无权访问");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        // 生成token（10分钟有效）
        String token = jwtService.generateDownloadToken(fileId.toString(), 600);
        String url = "/api/files/download/" + fileId + "?token=" + token;
        response.put("success", true);
        response.put("url", url);
        return ResponseEntity.ok(response);
    }

    /**
     * 支持token校验的下载接口
     * GET /api/files/download/{fileId}?token=xxx
     * 无需登录态，仅校验token
     */
    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFileWithToken(
            @PathVariable Long fileId,
            @RequestParam String token) {
        // 校验token
        boolean valid = jwtService.validateDownloadToken(token, fileId.toString());
        if (!valid) {
            System.out.println("下载token校验失败: fileId=" + fileId + ", token=" + token);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            Optional<FileEntity> fileOpt = fileService.getFileById(fileId);
            if (fileOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            FileEntity file = fileOpt.get();
            byte[] fileContent = fileService.getFileContent(fileId);
            ByteArrayResource resource = new ByteArrayResource(fileContent);
            String encodedFileName;
            try {
                encodedFileName = java.net.URLEncoder.encode(file.getOriginalName(), "UTF-8").replace("+", "%20");
            } catch (UnsupportedEncodingException e) {
                encodedFileName = file.getOriginalName().replaceAll("[^\\w.-]", "_");
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(file.getMimeType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 