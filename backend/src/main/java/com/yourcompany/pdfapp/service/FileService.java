package com.yourcompany.pdfapp.service;

import com.yourcompany.pdfapp.config.AppConfig;
import com.yourcompany.pdfapp.model.FileEntity;
import com.yourcompany.pdfapp.repository.FileRepository;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FileService {
    
    private static final Logger logger = LoggerFactory.getLogger(FileService.class);

    @Autowired
    private FileRepository fileRepository;
    
    @Autowired
    private AppConfig.FileProperties fileProperties;
    
    public FileEntity saveFile(MultipartFile file, String userId) throws IOException {
        // 验证文件
        validateFile(file);
        
        // 生成唯一文件名
        String originalName = file.getOriginalFilename();
        String extension = FilenameUtils.getExtension(originalName);
        String storedName = UUID.randomUUID().toString() + "." + extension;
        
        // 确保上传目录存在
        Path uploadPath = Paths.get(fileProperties.getUploadPath());
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // 保存文件到磁盘
        Path filePath = uploadPath.resolve(storedName);
        Files.copy(file.getInputStream(), filePath);
        
        // 计算文件校验和
        String checksum = calculateChecksum(filePath);
        
        // 创建文件实体
        FileEntity fileEntity = new FileEntity(
            userId,
            originalName,
            storedName,
            filePath.toString(),
            file.getSize(),
            getFileType(extension),
            file.getContentType(),
            "UPLOAD"
        );
        fileEntity.setChecksum(checksum);
        
        // 保存到数据库
        FileEntity savedEntity = fileRepository.save(fileEntity);
        logger.info("文件已保存: id={}, name={}, source={}", savedEntity.getId(), savedEntity.getOriginalName(), savedEntity.getSource());
        return savedEntity;
    }
    
    /**
     * 兼容小程序上传：支持自定义文件名、类型和大小
     */
    public FileEntity saveFile(MultipartFile file, String userId, String customFileName, String customFileType, Long customFileSize) throws IOException {
        // 验证文件
        validateFile(file);
        
        // 使用自定义文件名或 MultipartFile 的文件名
        String originalName = (customFileName != null && !customFileName.isEmpty()) ? customFileName : file.getOriginalFilename();
        String extension = FilenameUtils.getExtension(originalName);
        String storedName = UUID.randomUUID().toString() + "." + extension;
        
        // 确保上传目录存在
        Path uploadPath = Paths.get(fileProperties.getUploadPath());
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // 保存文件到磁盘
        Path filePath = uploadPath.resolve(storedName);
        Files.copy(file.getInputStream(), filePath);
        
        // 计算文件校验和
        String checksum = calculateChecksum(filePath);
        
        // 使用自定义文件类型或 MultipartFile 的文件类型
        String mimeType = (customFileType != null && !customFileType.isEmpty()) ? customFileType : file.getContentType();
        
        // 使用自定义文件大小或 MultipartFile 的文件大小
        Long fileSize = (customFileSize != null) ? customFileSize : file.getSize();
        
        // 创建文件实体
        FileEntity fileEntity = new FileEntity(
            userId,
            originalName,
            storedName,
            filePath.toString(),
            fileSize,
            getFileType(extension),
            mimeType,
            "UPLOAD"
        );
        fileEntity.setChecksum(checksum);
        
        // 保存到数据库
        FileEntity savedEntity = fileRepository.save(fileEntity);
        logger.info("文件已保存 (兼容模式): id={}, name={}, source={}", savedEntity.getId(), savedEntity.getOriginalName(), savedEntity.getSource());
        return savedEntity;
    }
    
    public Optional<FileEntity> getFile(Long fileId) {
        return fileRepository.findById(fileId);
    }
    
    public List<FileEntity> getAllFiles() {
        return fileRepository.findAll();
    }
    
    public List<FileEntity> getFilesByUser(String userId) {
        return fileRepository.findByUserId(userId);
    }
    
    public List<FileEntity> getFilesByType(String fileType) {
        return fileRepository.findByFileType(fileType);
    }
    
    public List<FileEntity> getFilesByUserAndType(String userId, String fileType) {
        return fileRepository.findByUserIdAndFileType(userId, fileType);
    }
    
    public Optional<FileEntity> getFileByUserAndId(String userId, Long fileId) {
        return fileRepository.findByIdAndUserId(fileId, userId);
    }
    
    public void deleteFile(Long fileId) throws IOException {
        Optional<FileEntity> fileEntity = fileRepository.findById(fileId);
        if (fileEntity.isPresent()) {
            // 删除磁盘文件
            Path filePath = Paths.get(fileEntity.get().getFilePath());
            Files.deleteIfExists(filePath);
            
            // 删除数据库记录
            fileRepository.deleteById(fileId);
        }
    }
    
    public byte[] getFileContent(Long fileId) throws IOException {
        Optional<FileEntity> fileEntity = fileRepository.findById(fileId);
        if (fileEntity.isPresent()) {
            Path filePath = Paths.get(fileEntity.get().getFilePath());
            return Files.readAllBytes(filePath);
        }
        throw new RuntimeException("文件不存在: " + fileId);
    }
    
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        
        if (file.getSize() > fileProperties.getMaxFileSize()) {
            throw new IllegalArgumentException("文件大小超过限制: " + fileProperties.getMaxFileSize());
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !fileProperties.getAllowedTypes().contains(contentType)) {
            throw new IllegalArgumentException("不支持的文件类型: " + contentType);
        }
    }
    
    private String getFileType(String extension) {
        if (extension == null) return "unknown";
        
        switch (extension.toLowerCase()) {
            case "pdf":
                return "PDF";
            case "doc":
            case "docx":
                return "WORD";
            case "ppt":
            case "pptx":
                return "POWERPOINT";
            case "xls":
            case "xlsx":
                return "EXCEL";
            case "jpg":
            case "jpeg":
            case "png":
            case "tiff":
                return "IMAGE";
            default:
                return "OTHER";
        }
    }
    
    private String calculateChecksum(Path filePath) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] fileBytes = Files.readAllBytes(filePath);
            byte[] hashBytes = digest.digest(fileBytes);
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            return null;
        }
    }
    
    /**
     * 直接保存字节数组为文件
     */
    public FileEntity saveFileFromBytes(byte[] fileContent, String originalName, String mimeType, String userId, String source) throws IOException {
        if (fileContent == null || fileContent.length == 0) {
            throw new IllegalArgumentException("文件内容不能为空");
        }
        
        // 生成唯一文件名
        String extension = getExtensionFromName(originalName);
        String storedName = UUID.randomUUID().toString() + "." + extension;
        
        // 确保上传目录存在
        Path uploadPath = Paths.get(fileProperties.getUploadPath());
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // 保存文件到磁盘
        Path filePath = uploadPath.resolve(storedName);
        Files.write(filePath, fileContent);
        
        // 计算文件校验和
        String checksum = calculateChecksum(filePath);
        
        // 创建文件实体
        FileEntity fileEntity = new FileEntity(
            userId,
            originalName,
            storedName,
            filePath.toString(),
            (long) fileContent.length,
            getFileType(extension),
            mimeType != null ? mimeType : "application/octet-stream",
            source
        );
        fileEntity.setChecksum(checksum);
        
        // 保存到数据库
        FileEntity savedEntity = fileRepository.save(fileEntity);
        logger.info("文件已保存 (from bytes): id={}, name={}, source={}", savedEntity.getId(), savedEntity.getOriginalName(), savedEntity.getSource());
        return savedEntity;
    }
    
    /**
     * 从文件名中提取扩展名
     */
    private String getExtensionFromName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "tmp";
        }
        
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0 && lastDot < fileName.length() - 1) {
            return fileName.substring(lastDot + 1);
        }
        
        return "tmp";
    }
    
    /**
     * 根据文件ID获取文件实体（不校验用户）
     */
    public Optional<FileEntity> getFileById(Long fileId) {
        return fileRepository.findById(fileId);
    }
} 