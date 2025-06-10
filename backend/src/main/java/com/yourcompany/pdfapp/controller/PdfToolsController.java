package com.yourcompany.pdfapp.controller;

import com.yourcompany.pdfapp.model.FileEntity;
import com.yourcompany.pdfapp.service.PdfService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.tess4j.TesseractException;

@Slf4j
@RestController
@RequestMapping("/api/pdf-tools")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:3006", "http://14.103.200.105:8081"})
public class PdfToolsController {
    
    @Autowired
    private PdfService pdfService;
    
    /**
     * 合并PDF文件
     */
    @PostMapping("/merge")
    public ResponseEntity<Map<String, Object>> mergePdfs(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            @SuppressWarnings("unchecked")
            List<Object> rawFileIds = (List<Object>) request.get("fileIds");
            String outputFileName = (String) request.get("outputFileName");
            
            if (rawFileIds == null || rawFileIds.isEmpty()) {
                response.put("success", false);
                response.put("message", "文件列表不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 将Integer转换为Long
            List<Long> fileIds = rawFileIds.stream()
                .map(id -> Long.valueOf(id.toString()))
                .collect(java.util.stream.Collectors.toList());
            
            FileEntity mergedFile = pdfService.mergePdfs(fileIds, outputFileName);
            
            response.put("success", true);
            response.put("message", "PDF合并成功");
            response.put("data", Map.of(
                "fileId", mergedFile.getId(),
                "fileName", mergedFile.getOriginalName(),
                "fileSize", mergedFile.getFileSize()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "PDF合并失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 分割PDF文件
     */
    @PostMapping("/split")
    public ResponseEntity<Map<String, Object>> splitPdf(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long fileId = Long.valueOf(request.get("fileId").toString());
            String splitType = (String) request.get("splitType");
            @SuppressWarnings("unchecked")
            Map<String, Object> options = (Map<String, Object>) request.get("options");
            
            if (splitType == null || splitType.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "分割类型不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            List<FileEntity> splitFiles = pdfService.splitPdf(fileId, splitType, options);
            
            response.put("success", true);
            response.put("message", "PDF分割成功");
            response.put("data", splitFiles.stream().map(file -> Map.of(
                "fileId", file.getId(),
                "fileName", file.getOriginalName(),
                "fileSize", file.getFileSize()
            )).toArray());
            response.put("count", splitFiles.size());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "PDF分割失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 压缩PDF文件
     */
    @PostMapping("/compress")
    public ResponseEntity<Map<String, Object>> compressPdf(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long fileId = Long.valueOf(request.get("fileId").toString());
            String compressionLevel = (String) request.getOrDefault("compressionLevel", "medium");
            
            FileEntity compressedFile = pdfService.compressPdf(fileId, compressionLevel);
            
            response.put("success", true);
            response.put("message", "PDF压缩成功");
            response.put("data", Map.of(
                "fileId", compressedFile.getId(),
                "fileName", compressedFile.getOriginalName(),
                "fileSize", compressedFile.getFileSize(),
                "compressionLevel", compressionLevel
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "PDF压缩失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * PDF转图片
     */
    @PostMapping("/to-images")
    public ResponseEntity<Map<String, Object>> pdfToImages(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long fileId = Long.valueOf(request.get("fileId").toString());
            String imageFormat = (String) request.getOrDefault("imageFormat", "PNG");
            Integer dpi = request.get("dpi") != null ? Integer.valueOf(request.get("dpi").toString()) : 300;
            String pageRange = (String) request.getOrDefault("pageRange", "all");
            String customRange = (String) request.get("customRange");
            
            // 验证图片格式
            if (!isValidImageFormat(imageFormat)) {
                response.put("success", false);
                response.put("message", "不支持的图片格式: " + imageFormat);
                return ResponseEntity.badRequest().body(response);
            }
            
            List<FileEntity> imageFiles = pdfService.pdfToImages(fileId, imageFormat, dpi, pageRange, customRange);
            
            response.put("success", true);
            response.put("message", "PDF转图片成功");
            response.put("data", imageFiles.stream().map(file -> Map.of(
                "fileId", file.getId(),
                "fileName", file.getOriginalName(),
                "fileSize", file.getFileSize(),
                "fileType", "IMAGE"
            )).collect(java.util.stream.Collectors.toList()));
            response.put("count", imageFiles.size());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "PDF转图片失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 图片转PDF
     */
    @PostMapping("/from-images")
    public ResponseEntity<Map<String, Object>> imagesToPdf(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            @SuppressWarnings("unchecked")
            List<Object> rawImageFileIds = (List<Object>) request.get("imageFileIds");
            String outputFileName = (String) request.get("outputFileName");
            
            if (rawImageFileIds == null || rawImageFileIds.isEmpty()) {
                response.put("success", false);
                response.put("message", "图片文件列表不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 将Integer转换为Long
            List<Long> imageFileIds = rawImageFileIds.stream()
                .map(id -> Long.valueOf(id.toString()))
                .collect(java.util.stream.Collectors.toList());
            
            FileEntity pdfFile = pdfService.imagesToPdf(imageFileIds, outputFileName);
            
            response.put("success", true);
            response.put("message", "图片转PDF成功");
            response.put("data", Map.of(
                "fileId", pdfFile.getId(),
                "fileName", pdfFile.getOriginalName(),
                "fileSize", pdfFile.getFileSize()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "图片转PDF失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 获取PDF信息
     */
    @GetMapping("/info/{fileId}")
    public ResponseEntity<Map<String, Object>> getPdfInfo(@PathVariable Long fileId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 这里可以添加获取PDF信息的逻辑
            // 如页数、大小、创建日期等
            
            response.put("success", true);
            response.put("message", "获取PDF信息成功");
            response.put("data", Map.of(
                "fileId", fileId,
                "pages", 0, // 实际需要解析PDF获取
                "title", "",
                "author", "",
                "subject", ""
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取PDF信息失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // 辅助方法
    
    private boolean isValidImageFormat(String format) {
        String[] validFormats = {"PNG", "JPG", "JPEG", "TIFF", "BMP", "GIF"};
        for (String validFormat : validFormats) {
            if (validFormat.equalsIgnoreCase(format)) {
                return true;
            }
        }
        return false;
    }

    /**
     * PDF转Word文档
     */
    @PostMapping("/to-word")
    public ResponseEntity<Map<String, Object>> convertPdfToWord(
            @RequestParam("file") MultipartFile file) {
        
        try {
            Long fileId = pdfService.convertPdfToWord(file);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "PDF转Word成功");
            response.put("fileId", fileId);
            response.put("algorithm", "文本提取 + Apache POI");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("PDF转Word失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "PDF转Word失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * PDF转Excel表格
     */
    @PostMapping("/to-excel")
    public ResponseEntity<Map<String, Object>> convertPdfToExcel(
            @RequestParam("file") MultipartFile file) {
        
        try {
            Long fileId = pdfService.convertPdfToExcel(file);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "PDF转Excel成功");
            response.put("fileId", fileId);
            response.put("algorithm", "Tabula表格识别 + Apache POI");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("PDF转Excel失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "PDF转Excel失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * PDF转CSV
     */
    @PostMapping("/to-csv")
    public ResponseEntity<Map<String, Object>> convertPdfToCsv(
            @RequestParam("file") MultipartFile file) {
        
        try {
            Long fileId = pdfService.convertPdfToCsv(file);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "PDF转CSV成功");
            response.put("fileId", fileId);
            response.put("algorithm", "Tabula表格识别 + OpenCSV");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("PDF转CSV失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "PDF转CSV失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * OCR文字识别
     */
    @PostMapping("/ocr")
    public ResponseEntity<Map<String, Object>> performOcr(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "language", defaultValue = "eng") String language) {
        
        try {
            String recognizedText = pdfService.performOcr(file, language);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "OCR识别成功");
            response.put("recognizedText", recognizedText);
            response.put("textLength", recognizedText.length());
            response.put("language", language);
            response.put("algorithm", "Tesseract OCR引擎");
            
            return ResponseEntity.ok(response);
        } catch (TesseractException e) {
            log.error("OCR识别失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "OCR识别失败: " + e.getMessage());
            response.put("error", "Tesseract引擎错误，请检查是否正确安装Tesseract");
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("OCR识别失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "OCR识别失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取PDF详细信息
     */
    @PostMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyzePdf(
            @RequestParam("file") MultipartFile file) {
        
        try {
            Map<String, Object> pdfInfo = pdfService.getPdfInfo(file);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "PDF分析完成");
            response.put("pdfInfo", pdfInfo);
            response.put("algorithms", Map.of(
                "textExtraction", "PDFBox文本提取",
                "tableDetection", "Tabula表格识别",
                "imageRendering", "PDFBox渲染引擎"
            ));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("PDF分析失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "PDF分析失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取支持的功能列表
     */
    @GetMapping("/features")
    public ResponseEntity<Map<String, Object>> getSupportedFeatures() {
        Map<String, Object> features = new HashMap<>();
        
        // 基础功能
        features.put("basic", Map.of(
            "merge", "PDF合并 - PDFBox PDFMergerUtility",
            "split", "PDF分割 - PDFBox Splitter",
            "compress", "PDF压缩 - PDFBox优化",
            "toImages", "PDF转图片 - PDFBox渲染引擎",
            "fromImages", "图片转PDF - PDFBox图像处理"
        ));
        
        // 转换功能 (参照Stirling-PDF)
        features.put("conversion", Map.of(
            "toWord", "PDF转Word - 文本提取 + Apache POI",
            "toExcel", "PDF转Excel - Tabula表格识别 + Apache POI",
            "toCsv", "PDF转CSV - Tabula表格识别 + OpenCSV"
        ));
        
        // OCR功能
        features.put("ocr", Map.of(
            "textRecognition", "OCR文字识别 - Tesseract OCR引擎",
            "supportedLanguages", List.of("eng", "chi_sim", "chi_tra", "jpn", "kor", "fra", "deu", "spa")
        ));
        
        // 分析功能
        features.put("analysis", Map.of(
            "pdfInfo", "PDF信息提取 - PDFBox文档信息",
            "tableDetection", "表格检测 - Tabula算法",
            "textExtraction", "文本提取 - PDFBox文本提取器"
        ));
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "功能列表获取成功");
        response.put("features", features);
        response.put("referenceProject", "Stirling-PDF");
        response.put("algorithms", Map.of(
            "pdfProcessing", "Apache PDFBox 3.0.5",
            "tableExtraction", "Tabula 1.0.5",
            "ocrEngine", "Tesseract 5.8.0",
            "officeDocuments", "Apache POI 5.2.5",
            "imageProcessing", "TwelveMonkeys ImageIO 3.12.0"
        ));
        
        return ResponseEntity.ok(response);
    }

    /**
     * PDF转Word文档 - 基于文件ID
     */
    @PostMapping("/to-word/{fileId}")
    public ResponseEntity<Map<String, Object>> convertPdfToWordById(@PathVariable Long fileId) {
        
        try {
            Long resultFileId = pdfService.convertPdfToWordById(fileId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "PDF转Word成功");
            response.put("fileId", resultFileId);
            response.put("algorithm", "文本提取 + Apache POI");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("PDF转Word失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "PDF转Word失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * PDF转Excel表格 - 基于文件ID
     */
    @PostMapping("/to-excel/{fileId}")
    public ResponseEntity<Map<String, Object>> convertPdfToExcelById(@PathVariable Long fileId) {
        
        try {
            Long resultFileId = pdfService.convertPdfToExcelById(fileId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "PDF转Excel成功");
            response.put("fileId", resultFileId);
            response.put("algorithm", "Tabula表格识别 + Apache POI");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("PDF转Excel失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "PDF转Excel失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * PDF转CSV - 基于文件ID
     */
    @PostMapping("/to-csv/{fileId}")
    public ResponseEntity<Map<String, Object>> convertPdfToCsvById(@PathVariable Long fileId) {
        
        try {
            Long resultFileId = pdfService.convertPdfToCsvById(fileId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "PDF转CSV成功");
            response.put("fileId", resultFileId);
            response.put("algorithm", "Tabula表格识别 + OpenCSV");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("PDF转CSV失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "PDF转CSV失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * OCR文字识别 - 基于文件ID
     */
    @PostMapping("/ocr/{fileId}")
    public ResponseEntity<Map<String, Object>> performOcrById(
            @PathVariable Long fileId,
            @RequestParam(value = "language", defaultValue = "eng") String language) {
        
        try {
            String recognizedText = pdfService.performOcrById(fileId, language);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "OCR识别成功");
            response.put("recognizedText", recognizedText);
            response.put("textLength", recognizedText.length());
            response.put("language", language);
            response.put("algorithm", "Tesseract OCR引擎");
            
            return ResponseEntity.ok(response);
        } catch (TesseractException e) {
            log.error("OCR识别失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "OCR识别失败: " + e.getMessage());
            response.put("error", "Tesseract引擎错误，请检查是否正确安装Tesseract");
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("OCR识别失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "OCR识别失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取PDF详细信息 - 基于文件ID
     */
    @PostMapping("/analyze/{fileId}")
    public ResponseEntity<Map<String, Object>> analyzePdfById(@PathVariable Long fileId) {
        
        try {
            Map<String, Object> pdfInfo = pdfService.getPdfInfoById(fileId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "PDF分析完成");
            response.put("pdfInfo", pdfInfo);
            response.put("algorithms", Map.of(
                "textExtraction", "PDFBox文本提取",
                "tableDetection", "Tabula表格识别",
                "imageRendering", "PDFBox渲染引擎"
            ));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("PDF分析失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "PDF分析失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 为PDF添加水印
     */
    @PostMapping("/watermark")
    public ResponseEntity<Map<String, Object>> addWatermark(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long fileId = Long.valueOf(request.get("fileId").toString());
            @SuppressWarnings("unchecked")
            Map<String, Object> watermarkOptions = (Map<String, Object>) request.get("watermarkOptions");
            
            if (watermarkOptions == null) {
                response.put("success", false);
                response.put("message", "水印选项不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 验证水印类型
            String watermarkType = (String) watermarkOptions.get("watermarkType");
            if (watermarkType == null || (!watermarkType.equals("text") && !watermarkType.equals("image"))) {
                response.put("success", false);
                response.put("message", "水印类型必须是 'text' 或 'image'");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 验证文字水印参数
            if ("text".equals(watermarkType)) {
                String watermarkText = (String) watermarkOptions.get("watermarkText");
                if (watermarkText == null || watermarkText.trim().isEmpty()) {
                    response.put("success", false);
                    response.put("message", "文字水印内容不能为空");
                    return ResponseEntity.badRequest().body(response);
                }
            }
            
            // 验证图片水印参数
            if ("image".equals(watermarkType)) {
                byte[] imageData = (byte[]) watermarkOptions.get("watermarkImageData");
                if (imageData == null || imageData.length == 0) {
                    response.put("success", false);
                    response.put("message", "图片水印数据不能为空");
                    return ResponseEntity.badRequest().body(response);
                }
            }
            
            FileEntity watermarkedFile = pdfService.addWatermark(fileId, watermarkOptions);
            
            response.put("success", true);
            response.put("message", "PDF水印添加成功");
            response.put("data", Map.of(
                "fileId", watermarkedFile.getId(),
                "fileName", watermarkedFile.getOriginalName(),
                "fileSize", watermarkedFile.getFileSize(),
                "watermarkType", watermarkType
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "PDF水印添加失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 为PDF添加水印 - 支持图片上传
     */
    @PostMapping("/watermark-with-image")
    public ResponseEntity<Map<String, Object>> addWatermarkWithImage(
            @RequestParam("fileId") Long fileId,
            @RequestParam("watermarkType") String watermarkType,
            @RequestParam(value = "watermarkText", required = false) String watermarkText,
            @RequestParam(value = "watermarkImage", required = false) MultipartFile watermarkImage,
            @RequestParam(value = "watermarkPosition", defaultValue = "center") String position,
            @RequestParam(value = "watermarkOpacity", defaultValue = "50") Integer opacity,
            @RequestParam(value = "watermarkSize", defaultValue = "24") Integer fontSize,
            @RequestParam(value = "watermarkColor", defaultValue = "#666666") String color,
            @RequestParam(value = "watermarkRotation", defaultValue = "0") Integer rotation,
            @RequestParam(value = "pageRange", defaultValue = "all") String pageRange,
            @RequestParam(value = "customRange", required = false) String customRange) {
        
        Map<String, Object> response = new HashMap<>();
        
        System.out.println("=== 接收到图片水印请求 ===");
        System.out.println("文件ID: " + fileId);
        System.out.println("水印类型: " + watermarkType);
        System.out.println("位置: " + position);
        System.out.println("透明度: " + opacity);
        System.out.println("旋转角度: " + rotation);
        System.out.println("页面范围: " + pageRange);
        if (watermarkImage != null) {
            System.out.println("图片文件名: " + watermarkImage.getOriginalFilename());
            System.out.println("图片大小: " + watermarkImage.getSize() + " 字节");
            System.out.println("图片类型: " + watermarkImage.getContentType());
        } else {
            System.out.println("警告：图片文件为空！");
        }
        System.out.println("=== 参数接收完成 ===");
        
        try {
            // 构建水印选项
            Map<String, Object> watermarkOptions = new HashMap<>();
            watermarkOptions.put("watermarkType", watermarkType);
            watermarkOptions.put("watermarkPosition", position);
            watermarkOptions.put("watermarkOpacity", opacity);
            watermarkOptions.put("watermarkRotation", rotation);
            watermarkOptions.put("pageRange", pageRange);
            watermarkOptions.put("customRange", customRange);
            
            if ("text".equals(watermarkType)) {
                if (watermarkText == null || watermarkText.trim().isEmpty()) {
                    response.put("success", false);
                    response.put("message", "文字水印内容不能为空");
                    return ResponseEntity.badRequest().body(response);
                }
                watermarkOptions.put("watermarkText", watermarkText);
                watermarkOptions.put("watermarkSize", fontSize);
                watermarkOptions.put("watermarkColor", color);
                
            } else if ("image".equals(watermarkType)) {
                System.out.println("处理图片水印类型...");
                
                if (watermarkImage == null || watermarkImage.isEmpty()) {
                    System.err.println("错误：图片水印文件为空");
                    response.put("success", false);
                    response.put("message", "图片水印文件不能为空");
                    return ResponseEntity.badRequest().body(response);
                }
                
                // 验证图片格式
                String contentType = watermarkImage.getContentType();
                System.out.println("图片内容类型: " + contentType);
                
                if (contentType == null || !contentType.startsWith("image/")) {
                    System.err.println("错误：无效的图片格式 - " + contentType);
                    response.put("success", false);
                    response.put("message", "请上传有效的图片文件");
                    return ResponseEntity.badRequest().body(response);
                }
                
                // 将图片转换为字节数组
                byte[] imageData = watermarkImage.getBytes();
                System.out.println("图片数据转换成功，大小: " + imageData.length + " 字节");
                watermarkOptions.put("watermarkImageData", imageData);
                System.out.println("图片数据已添加到水印选项中");
                
            } else {
                response.put("success", false);
                response.put("message", "水印类型必须是 'text' 或 'image'");
                return ResponseEntity.badRequest().body(response);
            }
            
            FileEntity watermarkedFile = pdfService.addWatermark(fileId, watermarkOptions);
            
            response.put("success", true);
            response.put("message", "PDF水印添加成功");
            response.put("data", Map.of(
                "fileId", watermarkedFile.getId(),
                "fileName", watermarkedFile.getOriginalName(),
                "fileSize", watermarkedFile.getFileSize(),
                "watermarkType", watermarkType
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "PDF水印添加失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 旋转PDF页面
     */
    @PostMapping("/rotate")
    public ResponseEntity<Map<String, Object>> rotatePages(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long fileId = Long.valueOf(request.get("fileId").toString());
            String pageRange = (String) request.getOrDefault("pageRange", "all");
            String customRange = (String) request.get("customRange");
            Integer rotation = (Integer) request.getOrDefault("rotation", 90);
            
            FileEntity rotatedFile = pdfService.rotatePages(fileId, pageRange, customRange, rotation);
            
            response.put("success", true);
            response.put("message", "PDF页面旋转成功");
            response.put("data", Map.of(
                "fileId", rotatedFile.getId(),
                "fileName", rotatedFile.getOriginalName(),
                "fileSize", rotatedFile.getFileSize(),
                "rotation", rotation
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            log.error("PDF页面旋转失败", e);
            response.put("success", false);
            response.put("message", "PDF页面旋转失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 删除PDF页面
     */
    @PostMapping("/delete-pages")
    public ResponseEntity<Map<String, Object>> deletePages(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long fileId = Long.valueOf(request.get("fileId").toString());
            String pageRange = (String) request.getOrDefault("pageRange", "custom");
            String customRange = (String) request.get("customRange");
            
            if ("all".equals(pageRange)) {
                response.put("success", false);
                response.put("message", "删除页面不能选择全部页面");
                return ResponseEntity.badRequest().body(response);
            }
            
            if ("custom".equals(pageRange) && (customRange == null || customRange.trim().isEmpty())) {
                response.put("success", false);
                response.put("message", "请指定要删除的页面范围");
                return ResponseEntity.badRequest().body(response);
            }
            
            FileEntity resultFile = pdfService.deletePages(fileId, pageRange, customRange);
            
            response.put("success", true);
            response.put("message", "PDF页面删除成功");
            response.put("data", Map.of(
                "fileId", resultFile.getId(),
                "fileName", resultFile.getOriginalName(),
                "fileSize", resultFile.getFileSize()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            log.error("PDF页面删除失败", e);
            response.put("success", false);
            response.put("message", "PDF页面删除失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 提取PDF页面
     */
    @PostMapping("/extract-pages")
    public ResponseEntity<Map<String, Object>> extractPages(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long fileId = Long.valueOf(request.get("fileId").toString());
            String pageRange = (String) request.getOrDefault("pageRange", "custom");
            String customRange = (String) request.get("customRange");
            
            if ("custom".equals(pageRange) && (customRange == null || customRange.trim().isEmpty())) {
                response.put("success", false);
                response.put("message", "请指定要提取的页面范围");
                return ResponseEntity.badRequest().body(response);
            }
            
            FileEntity extractedFile = pdfService.extractPages(fileId, pageRange, customRange);
            
            response.put("success", true);
            response.put("message", "PDF页面提取成功");
            response.put("data", Map.of(
                "fileId", extractedFile.getId(),
                "fileName", extractedFile.getOriginalName(),
                "fileSize", extractedFile.getFileSize()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            log.error("PDF页面提取失败", e);
            response.put("success", false);
            response.put("message", "PDF页面提取失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 重新排序PDF页面
     */
    @PostMapping("/reorder-pages")
    public ResponseEntity<Map<String, Object>> reorderPages(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long fileId = Long.valueOf(request.get("fileId").toString());
            @SuppressWarnings("unchecked")
            List<Object> rawPageOrder = (List<Object>) request.get("pageOrder");
            
            if (rawPageOrder == null || rawPageOrder.isEmpty()) {
                response.put("success", false);
                response.put("message", "页面顺序不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 将Object转换为Integer
            List<Integer> pageOrder = rawPageOrder.stream()
                .map(page -> Integer.valueOf(page.toString()))
                .collect(java.util.stream.Collectors.toList());
            
            FileEntity reorderedFile = pdfService.reorderPages(fileId, pageOrder);
            
            response.put("success", true);
            response.put("message", "页面重排序成功");
            response.put("data", Map.of(
                "fileId", reorderedFile.getId(),
                "fileName", reorderedFile.getOriginalName(),
                "fileSize", reorderedFile.getFileSize()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "页面重排序失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ============= 安全工具 =============
    
    /**
     * PDF加密
     */
    @PostMapping("/encrypt")
    public ResponseEntity<Map<String, Object>> encryptPdf(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long fileId = Long.valueOf(request.get("fileId").toString());
            String userPassword = (String) request.get("userPassword"); // 打开密码
            String ownerPassword = (String) request.get("ownerPassword"); // 权限密码
            
            if (userPassword == null || userPassword.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "用户密码不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 如果没有设置权限密码，则使用用户密码
            if (ownerPassword == null || ownerPassword.trim().isEmpty()) {
                ownerPassword = userPassword;
            }
            
            FileEntity encryptedFile = pdfService.encryptPdf(fileId, userPassword, ownerPassword);
            
            response.put("success", true);
            response.put("message", "PDF加密成功");
            response.put("data", Map.of(
                "fileId", encryptedFile.getId(),
                "fileName", encryptedFile.getOriginalName(),
                "fileSize", encryptedFile.getFileSize()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "PDF加密失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * PDF解密
     */
    @PostMapping("/decrypt")
    public ResponseEntity<Map<String, Object>> decryptPdf(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long fileId = Long.valueOf(request.get("fileId").toString());
            String password = (String) request.get("password");
            
            if (password == null || password.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "解密密码不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            FileEntity decryptedFile = pdfService.decryptPdf(fileId, password);
            
            response.put("success", true);
            response.put("message", "PDF解密成功");
            response.put("data", Map.of(
                "fileId", decryptedFile.getId(),
                "fileName", decryptedFile.getOriginalName(),
                "fileSize", decryptedFile.getFileSize()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "PDF解密失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 内容编辑（涂黑敏感内容）
     */
    @PostMapping("/redact")
    public ResponseEntity<Map<String, Object>> redactPdf(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long fileId = Long.valueOf(request.get("fileId").toString());
            Object keywordsObj = request.get("keywords");
            List<String> keywords = new ArrayList<>();
            
            // 处理关键词：可能是List<String>或单个String
            if (keywordsObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> keywordList = (List<String>) keywordsObj;
                for (String keyword : keywordList) {
                    // 每个元素还需要按逗号分割
                    if (keyword != null && !keyword.trim().isEmpty()) {
                        String[] parts = keyword.split("[,，]"); // 支持中英文逗号
                        for (String part : parts) {
                            String trimmed = part.trim();
                            if (!trimmed.isEmpty()) {
                                keywords.add(trimmed);
                            }
                        }
                    }
                }
            } else if (keywordsObj instanceof String) {
                String keywordString = (String) keywordsObj;
                // 按逗号分割关键词
                String[] parts = keywordString.split("[,，]"); // 支持中英文逗号
                for (String part : parts) {
                    String trimmed = part.trim();
                    if (!trimmed.isEmpty()) {
                        keywords.add(trimmed);
                    }
                }
            }
            
            String pageRange = (String) request.getOrDefault("pageRange", "all");
            String customRange = (String) request.get("customRange");
            
            if (keywords.isEmpty()) {
                response.put("success", false);
                response.put("message", "请指定要涂黑的关键词");
                return ResponseEntity.badRequest().body(response);
            }
            
            log.info("处理后的关键词列表: {}", keywords);
            
            FileEntity redactedFile = pdfService.redactPdf(fileId, keywords, pageRange, customRange);
            
            response.put("success", true);
            response.put("message", "内容编辑成功");
            response.put("data", Map.of(
                "fileId", redactedFile.getId(),
                "fileName", redactedFile.getOriginalName(),
                "fileSize", redactedFile.getFileSize()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "内容编辑失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 数字签名
     */
    @PostMapping("/digital-sign")
    public ResponseEntity<Map<String, Object>> digitalSignPdf(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long fileId = Long.valueOf(request.get("fileId").toString());
            String signerName = (String) request.get("signerName");
            String reason = (String) request.getOrDefault("reason", "数字签名");
            String location = (String) request.getOrDefault("location", "中国");
            String pageRange = (String) request.getOrDefault("pageRange", "all");
            String customRange = (String) request.get("customRange");
            
            if (signerName == null || signerName.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "签名者姓名不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            FileEntity signedFile = pdfService.digitalSignPdf(fileId, signerName, reason, location, pageRange, customRange);
            
            response.put("success", true);
            response.put("message", "数字签名成功");
            response.put("data", Map.of(
                "fileId", signedFile.getId(),
                "fileName", signedFile.getOriginalName(),
                "fileSize", signedFile.getFileSize(),
                "signerName", signerName,
                "reason", reason,
                "location", location,
                "pageRange", pageRange
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "数字签名失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
} 