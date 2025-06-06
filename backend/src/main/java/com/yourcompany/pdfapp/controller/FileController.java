package com.yourcompany.pdfapp.controller;

import com.yourcompany.pdfapp.model.FileEntity;
import com.yourcompany.pdfapp.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class FileController {
    
    @Autowired
    private FileService fileService;
    
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            FileEntity savedFile = fileService.saveFile(file);
            
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
        
        try {
            List<FileEntity> files = fileService.getAllFiles();
            
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
        
        try {
            Optional<FileEntity> file = fileService.getFile(fileId);
            
            if (file.isPresent()) {
                response.put("success", true);
                response.put("data", file.get());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "文件不存在");
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
        try {
            Optional<FileEntity> fileEntity = fileService.getFile(fileId);
            
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
} 