package com.yourcompany.pdfapp.repository;

import com.yourcompany.pdfapp.model.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long> {
    
    // 根据用户ID查找所有文件
    List<FileEntity> findByUserId(String userId);
    
    // 根据用户ID和文件ID查找
    Optional<FileEntity> findByIdAndUserId(Long id, String userId);
    
    // 根据用户ID和文件类型查找
    List<FileEntity> findByUserIdAndFileType(String userId, String fileType);
    
    // 根据文件名查找（保留，但应该按用户过滤）
    Optional<FileEntity> findByOriginalName(String originalName);
    
    // 根据存储名查找
    Optional<FileEntity> findByStoredName(String storedName);
    
    // 根据文件类型查找
    List<FileEntity> findByFileType(String fileType);
    
    // 根据创建时间范围查找
    List<FileEntity> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // 根据文件大小范围查找
    @Query("SELECT f FROM FileEntity f WHERE f.fileSize BETWEEN :minSize AND :maxSize")
    List<FileEntity> findByFileSizeBetween(@Param("minSize") Long minSize, @Param("maxSize") Long maxSize);
    
    // 统计用户文件总数
    @Query("SELECT COUNT(f) FROM FileEntity f WHERE f.userId = :userId")
    Long countFilesByUserId(@Param("userId") String userId);
    
    // 统计用户文件总大小
    @Query("SELECT SUM(f.fileSize) FROM FileEntity f WHERE f.userId = :userId")
    Long sumFileSizeByUserId(@Param("userId") String userId);
    
    // 统计文件总数（管理员功能）
    @Query("SELECT COUNT(f) FROM FileEntity f")
    Long countAllFiles();
    
    // 统计文件总大小（管理员功能）
    @Query("SELECT SUM(f.fileSize) FROM FileEntity f")
    Long sumFileSize();
} 