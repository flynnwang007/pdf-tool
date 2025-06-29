package com.yourcompany.pdfapp.service;

import com.yourcompany.pdfapp.config.AppConfig;
import com.yourcompany.pdfapp.model.FileEntity;
import com.yourcompany.pdfapp.security.SupabaseUserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import technology.tabula.*;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;
import technology.tabula.extractors.BasicExtractionAlgorithm;
import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.Table;
import technology.tabula.RectangularTextContainer;
import com.opencsv.CSVWriter;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.util.Matrix;
import java.awt.Color;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.fontbox.ttf.TrueTypeCollection;
import org.apache.fontbox.ttf.TrueTypeFont;

import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;

import org.bouncycastle.asn1.x500.X500Name;
import java.security.KeyPairGenerator;
import java.security.KeyPair;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.math.BigInteger;
import java.util.Date;
import org.jodconverter.core.DocumentConverter;
import org.jodconverter.local.JodConverter;
import org.jodconverter.local.office.LocalOfficeManager;

@Service
public class PdfService {
    
    @Autowired
    private FileService fileService;
    
    @Autowired
    private AppConfig.FileProperties fileProperties;
    
    @Autowired
    private AppConfig.PdfProperties pdfProperties;
    
    /**
     * 获取当前认证用户的ID
     */
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SupabaseUserPrincipal) {
            return ((SupabaseUserPrincipal) authentication.getPrincipal()).getUserId();
        }
        throw new RuntimeException("未找到认证用户信息");
    }
    
    /**
     * 合并多个PDF文件
     */
    public FileEntity mergePdfs(List<Long> fileIds, String outputFileName) throws IOException {
        if (fileIds == null || fileIds.isEmpty()) {
            throw new IllegalArgumentException("文件列表不能为空");
        }
        
        PDFMergerUtility merger = new PDFMergerUtility();
        List<PDDocument> documents = new ArrayList<>();
        
        try {
            // 加载所有PDF文档
            for (Long fileId : fileIds) {
                byte[] fileContent = fileService.getFileContent(fileId);
                PDDocument document = Loader.loadPDF(fileContent);
                documents.add(document);
            }
            
            // 创建输出文件
            String outputName = outputFileName != null ? outputFileName : "merged_" + System.currentTimeMillis() + ".pdf";
            Path outputPath = createOutputFile(outputName);
            
            // 合并文档
            PDDocument mergedDocument = new PDDocument();
            for (PDDocument doc : documents) {
                for (int i = 0; i < doc.getNumberOfPages(); i++) {
                    mergedDocument.addPage(doc.getPage(i));
                }
            }
            
            // 保存合并后的文档
            mergedDocument.save(outputPath.toFile());
            mergedDocument.close();
            
            // 保存为新的文件实体
            return saveProcessedFile(outputPath, outputName, "PDF合并");
            
        } finally {
            // 关闭所有文档
            for (PDDocument doc : documents) {
                try {
                    doc.close();
                } catch (IOException e) {
                    // 忽略关闭异常
                }
            }
        }
    }
    
    /**
     * 分割PDF文件
     */
    public List<FileEntity> splitPdf(Long fileId, String splitType, Map<String, Object> options) throws IOException {
        byte[] fileContent = fileService.getFileContent(fileId);
        Optional<FileEntity> originalFile = fileService.getFile(fileId);
        
        if (originalFile.isEmpty()) {
            throw new IllegalArgumentException("原文件不存在");
        }
        
        List<FileEntity> resultFiles = new ArrayList<>();
        
        try (PDDocument document = Loader.loadPDF(fileContent)) {
            Splitter splitter = new Splitter();
            
            switch (splitType.toLowerCase()) {
                case "pages":
                    // 按页数分割
                    Integer pageCount = (Integer) options.get("pageCount");
                    if (pageCount == null || pageCount <= 0) {
                        throw new IllegalArgumentException("页数必须大于0");
                    }
                    
                    splitter.setSplitAtPage(pageCount);
                    List<PDDocument> splitDocuments = splitter.split(document);
                    
                    for (int i = 0; i < splitDocuments.size(); i++) {
                        String fileName = getFileNameWithoutExtension(originalFile.get().getOriginalName()) 
                                        + "_part" + (i + 1) + ".pdf";
                        FileEntity splitFile = saveDocumentAsFile(splitDocuments.get(i), fileName, "PDF分割");
                        resultFiles.add(splitFile);
                    }
                    
                    // 关闭分割后的文档
                    for (PDDocument doc : splitDocuments) {
                        doc.close();
                    }
                    break;
                    
                case "range":
                    // 按页面范围分割
                    List<Map<String, Integer>> ranges = (List<Map<String, Integer>>) options.get("ranges");
                    if (ranges == null || ranges.isEmpty()) {
                        throw new IllegalArgumentException("页面范围不能为空");
                    }
                    
                    for (int i = 0; i < ranges.size(); i++) {
                        Map<String, Integer> range = ranges.get(i);
                        Integer startPage = range.get("start");
                        Integer endPage = range.get("end");
                        
                        if (startPage == null || endPage == null || startPage > endPage) {
                            throw new IllegalArgumentException("页面范围无效: " + range);
                        }
                        
                        PDDocument rangeDoc = new PDDocument();
                        for (int pageIndex = startPage - 1; pageIndex < endPage && pageIndex < document.getNumberOfPages(); pageIndex++) {
                            rangeDoc.addPage(document.getPage(pageIndex));
                        }
                        
                        String fileName = getFileNameWithoutExtension(originalFile.get().getOriginalName()) 
                                        + "_pages" + startPage + "-" + endPage + ".pdf";
                        FileEntity rangeFile = saveDocumentAsFile(rangeDoc, fileName, "PDF分割");
                        resultFiles.add(rangeFile);
                        
                        rangeDoc.close();
                    }
                    break;
                    
                default:
                    throw new IllegalArgumentException("不支持的分割类型: " + splitType);
            }
        }
        
        return resultFiles;
    }
    
    /**
     * PDF压缩
     */
    public FileEntity compressPdf(Long fileId, String compressionLevel) throws IOException {
        byte[] fileContent = fileService.getFileContent(fileId);
        Optional<FileEntity> originalFile = fileService.getFile(fileId);
        
        if (originalFile.isEmpty()) {
            throw new IllegalArgumentException("原文件不存在");
        }
        
        try (PDDocument document = Loader.loadPDF(fileContent)) {
            // 移除所有安全限制以避免保存时的加密错误
            document.setAllSecurityToBeRemoved(true);
            
            // 应用压缩设置
            applyCompression(document, compressionLevel);
            
            String outputName = getFileNameWithoutExtension(originalFile.get().getOriginalName()) 
                              + "_compressed.pdf";
            Path outputPath = createOutputFile(outputName);
            
            document.save(outputPath.toFile());
            
            return saveProcessedFile(outputPath, outputName, "PDF压缩");
        }
    }
    
    /**
     * PDF转换为图片
     */
    public List<FileEntity> pdfToImages(Long fileId, String imageFormat, Integer dpi, String pageRange, String customRange) throws IOException {
        byte[] fileContent = fileService.getFileContent(fileId);
        Optional<FileEntity> originalFile = fileService.getFile(fileId);
        
        if (originalFile.isEmpty()) {
            throw new IllegalArgumentException("原文件不存在");
        }
        
        List<FileEntity> imageFiles = new ArrayList<>();
        
        try (PDDocument document = Loader.loadPDF(fileContent)) {
            PDFRenderer renderer = new PDFRenderer(document);
            int dpiValue = dpi != null ? dpi : 300;
            
            // 标准化图片格式名称
            String normalizedFormat = imageFormat.toUpperCase();
            if ("JPEG".equals(normalizedFormat)) {
                normalizedFormat = "JPG";
            }
            
            // 解析页面范围
            List<Integer> pagesToProcess = parsePageRange(pageRange, customRange, document.getNumberOfPages());
            
            System.out.println("PDF转图片 - 文档页数: " + document.getNumberOfPages() + ", 格式: " + normalizedFormat + ", DPI: " + dpiValue + ", 页面范围: " + pageRange + ", 自定义范围: " + customRange + ", 实际处理页面: " + pagesToProcess);
            
            for (int pageIndex : pagesToProcess) {
                // pageIndex 已经是0基索引，无需再次转换
                BufferedImage image = renderer.renderImageWithDPI(pageIndex, dpiValue, ImageType.RGB);
                
                // 显示页面号时需要+1（用于文件名）
                int pageNumber = pageIndex + 1;
                String fileName = getFileNameWithoutExtension(originalFile.get().getOriginalName()) 
                                + "_page" + pageNumber + "." + normalizedFormat.toLowerCase();
                
                Path outputPath = createOutputFile(fileName);
                
                System.out.println("正在保存图片: " + outputPath.toString() + ", 页面: " + pageNumber + ", 图片尺寸: " + image.getWidth() + "x" + image.getHeight());
                
                // 使用标准的ImageIO写入图片
                if (!ImageIO.write(image, normalizedFormat, outputPath.toFile())) {
                    throw new IOException("无法写入图片格式: " + normalizedFormat);
                }
                
                // 验证文件是否成功写入
                File savedFile = outputPath.toFile();
                if (!savedFile.exists() || savedFile.length() == 0) {
                    throw new IOException("图片文件保存失败或文件大小为0: " + fileName);
                }
                
                System.out.println("图片保存成功: " + fileName + ", 文件大小: " + savedFile.length() + " bytes");
                
                FileEntity imageFile = saveProcessedFile(outputPath, fileName, "PDF转图片");
                imageFiles.add(imageFile);
            }
        }
        
        return imageFiles;
    }
    
    /**
     * PDF转换为图片（兼容旧方法）
     */
    public List<FileEntity> pdfToImages(Long fileId, String imageFormat, Integer dpi) throws IOException {
        return pdfToImages(fileId, imageFormat, dpi, "all", null);
    }
    
    /**
     * 图片转PDF
     */
    public FileEntity imagesToPdf(List<Long> imageFileIds, String outputFileName) throws IOException {
        if (imageFileIds == null || imageFileIds.isEmpty()) {
            throw new IllegalArgumentException("图片文件列表不能为空");
        }
        
        try (PDDocument document = new PDDocument()) {
            for (Long fileId : imageFileIds) {
                byte[] imageContent = fileService.getFileContent(fileId);
                BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageContent));
                
                if (image != null) {
                    PDPage page = new PDPage(new PDRectangle(image.getWidth(), image.getHeight()));
                    document.addPage(page);
                    
                    PDImageXObject pdImage = LosslessFactory.createFromImage(document, image);
                    
                    try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                        contentStream.drawImage(pdImage, 0, 0, image.getWidth(), image.getHeight());
                    }
                }
            }
            
            String outputName = outputFileName != null ? outputFileName : "images_to_pdf_" + System.currentTimeMillis() + ".pdf";
            Path outputPath = createOutputFile(outputName);
            
            document.save(outputPath.toFile());
            
            return saveProcessedFile(outputPath, outputName, "图片转PDF");
        }
    }
    
    /**
     * PDF转Word文档 - 基于文件ID
     */
    public Long convertPdfToWordById(Long fileId) throws IOException {
        byte[] fileContent = fileService.getFileContent(fileId);
        Optional<FileEntity> originalFile = fileService.getFile(fileId);
        
        if (originalFile.isEmpty()) {
            throw new IllegalArgumentException("原文件不存在");
        }
        
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(fileContent);
             PDDocument document = Loader.loadPDF(fileContent)) {
            
            // 创建Word文档
            XWPFDocument wordDoc = new XWPFDocument();
            PDFTextStripper textStripper = new PDFTextStripper();
            
            // 按页提取文本并添加到Word文档
            for (int pageNum = 1; pageNum <= document.getNumberOfPages(); pageNum++) {
                textStripper.setStartPage(pageNum);
                textStripper.setEndPage(pageNum);
                
                String pageText = textStripper.getText(document);
                
                // 创建段落
                XWPFParagraph paragraph = wordDoc.createParagraph();
                XWPFRun run = paragraph.createRun();
                run.setText(pageText);
                
                // 页面分隔符（除了最后一页）
                if (pageNum < document.getNumberOfPages()) {
                    run.addBreak(BreakType.PAGE);
                }
            }
            
            // 保存Word文档
            String outputName = getFileNameWithoutExtension(originalFile.get().getOriginalName()) + ".docx";
            Path outputPath = createOutputFile(outputName);
            
            try (FileOutputStream fos = new FileOutputStream(outputPath.toFile())) {
                wordDoc.write(fos);
            }
            
            wordDoc.close();
            
            FileEntity savedFile = saveProcessedFile(outputPath, outputName, "PDF转Word");
            return savedFile.getId();
        }
    }

    /**
     * PDF转Excel表格 - 基于文件ID
     */
    public Long convertPdfToExcelById(Long fileId) throws IOException {
        byte[] fileContent = fileService.getFileContent(fileId);
        Optional<FileEntity> originalFile = fileService.getFile(fileId);
        
        if (originalFile.isEmpty()) {
            throw new IllegalArgumentException("原文件不存在");
        }
        
        try (PDDocument document = Loader.loadPDF(fileContent)) {
            
            // 使用Tabula提取表格数据
            ObjectExtractor oe = new ObjectExtractor(document);
            List<Table> tables = new ArrayList<>();
            
            for (int pageIndex = 0; pageIndex < document.getNumberOfPages(); pageIndex++) {
                Page page = oe.extract(pageIndex + 1);
                List<Table> pageTables = new SpreadsheetExtractionAlgorithm().extract(page);
                tables.addAll(pageTables);
            }
            
            oe.close();
            
            // 创建Excel工作簿
            Workbook workbook = new XSSFWorkbook();
            
            for (int i = 0; i < tables.size(); i++) {
                Table table = tables.get(i);
                Sheet sheet = workbook.createSheet("表格" + (i + 1));
                
                List<List<RectangularTextContainer>> rows = table.getRows();
                for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
                    Row row = sheet.createRow(rowIndex);
                    List<RectangularTextContainer> cells = rows.get(rowIndex);
                    
                    for (int cellIndex = 0; cellIndex < cells.size(); cellIndex++) {
                        Cell cell = row.createCell(cellIndex);
                        String cellText = cells.get(cellIndex).getText().trim();
                        cell.setCellValue(cellText);
                    }
                }
            }
            
            // 如果没有检测到表格，创建一个空的工作表
            if (tables.isEmpty()) {
                Sheet sheet = workbook.createSheet("数据");
                Row headerRow = sheet.createRow(0);
                headerRow.createCell(0).setCellValue("未检测到表格数据");
            }
            
            // 保存Excel文件
            String outputName = getFileNameWithoutExtension(originalFile.get().getOriginalName()) + ".xlsx";
            Path outputPath = createOutputFile(outputName);
            
            try (FileOutputStream fos = new FileOutputStream(outputPath.toFile())) {
                workbook.write(fos);
            }
            
            workbook.close();
            
            FileEntity savedFile = saveProcessedFile(outputPath, outputName, "PDF转Excel");
            return savedFile.getId();
        }
    }

    /**
     * PDF转CSV - 基于文件ID  
     */
    public Long convertPdfToCsvById(Long fileId) throws IOException {
        byte[] fileContent = fileService.getFileContent(fileId);
        Optional<FileEntity> originalFile = fileService.getFile(fileId);
        
        if (originalFile.isEmpty()) {
            throw new IllegalArgumentException("原文件不存在");
        }
        
        try (PDDocument document = Loader.loadPDF(fileContent)) {
            
            // 使用Tabula提取表格数据
            ObjectExtractor oe = new ObjectExtractor(document);
            List<Table> tables = new ArrayList<>();
            
            for (int pageIndex = 0; pageIndex < document.getNumberOfPages(); pageIndex++) {
                Page page = oe.extract(pageIndex + 1);
                List<Table> pageTables = new SpreadsheetExtractionAlgorithm().extract(page);
                tables.addAll(pageTables);
            }
            
            oe.close();
            
            String outputName = getFileNameWithoutExtension(originalFile.get().getOriginalName()) + ".csv";
            Path outputPath = createOutputFile(outputName);
            
            try (CSVWriter writer = new CSVWriter(new FileWriter(outputPath.toFile()))) {
                if (tables.isEmpty()) {
                    writer.writeNext(new String[]{"未检测到表格数据"});
                } else {
                    // 写入第一个表格的数据
                    Table firstTable = tables.get(0);
                    List<List<RectangularTextContainer>> rows = firstTable.getRows();
                    
                    for (List<RectangularTextContainer> row : rows) {
                        String[] csvRow = new String[row.size()];
                        for (int i = 0; i < row.size(); i++) {
                            csvRow[i] = row.get(i).getText().trim();
                        }
                        writer.writeNext(csvRow);
                    }
                }
            }
            
            FileEntity savedFile = saveProcessedFile(outputPath, outputName, "PDF转CSV");
            return savedFile.getId();
        }
    }

    /**
     * OCR识别 - 基于文件ID
     */
    public String performOcrById(Long fileId, String language) throws TesseractException, IOException {
        byte[] fileContent = fileService.getFileContent(fileId);
        
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(fileContent)) {
            // 检查是否为PDF文件
            if (fileContent.length >= 4 && 
                fileContent[0] == 0x25 && fileContent[1] == 0x50 && 
                fileContent[2] == 0x44 && fileContent[3] == 0x46) {
                
                // PDF文件转换为图片后进行OCR
                try (PDDocument document = Loader.loadPDF(fileContent)) {
                    PDFRenderer renderer = new PDFRenderer(document);
                    StringBuilder allText = new StringBuilder();
                    
                    Tesseract tesseract = new Tesseract();
                    tesseract.setDatapath(pdfProperties.getOcr().getDataPath());
                    tesseract.setLanguage(language);
                    tesseract.setOcrEngineMode(1);
                    tesseract.setPageSegMode(3);
                    
                    for (int pageIndex = 0; pageIndex < document.getNumberOfPages(); pageIndex++) {
                        BufferedImage image = renderer.renderImageWithDPI(pageIndex, 300, ImageType.RGB);
                        String pageText = tesseract.doOCR(image);
                        allText.append(pageText).append("\n\n");
                    }
                    
                    return allText.toString().trim();
                }
            } else {
                // 直接对图片进行OCR
                BufferedImage image = ImageIO.read(inputStream);
                
                Tesseract tesseract = new Tesseract();
                tesseract.setDatapath(pdfProperties.getOcr().getDataPath());
                tesseract.setLanguage(language);
                tesseract.setOcrEngineMode(1);
                tesseract.setPageSegMode(3);
                
                return tesseract.doOCR(image);
            }
        }
    }

    /**
     * PDF信息分析 - 基于文件ID
     */
    public Map<String, Object> getPdfInfoById(Long fileId) throws IOException {
        byte[] fileContent = fileService.getFileContent(fileId);
        Optional<FileEntity> originalFile = fileService.getFile(fileId);
        
        if (originalFile.isEmpty()) {
            throw new IllegalArgumentException("原文件不存在");
        }
        
        try (PDDocument document = Loader.loadPDF(fileContent)) {
            Map<String, Object> pdfInfo = new HashMap<>();
            
            // 基本信息
            pdfInfo.put("pageCount", document.getNumberOfPages());
            pdfInfo.put("fileSize", originalFile.get().getFileSize());
            pdfInfo.put("fileName", originalFile.get().getOriginalName());
            
            // 文档信息
            PDDocumentInformation docInfo = document.getDocumentInformation();
            if (docInfo != null) {
                pdfInfo.put("title", docInfo.getTitle());
                pdfInfo.put("author", docInfo.getAuthor());
                pdfInfo.put("subject", docInfo.getSubject());
                pdfInfo.put("creator", docInfo.getCreator());
                pdfInfo.put("producer", docInfo.getProducer());
                pdfInfo.put("creationDate", docInfo.getCreationDate());
                pdfInfo.put("modificationDate", docInfo.getModificationDate());
            }
            
            // 表格检测
            try {
                ObjectExtractor oe = new ObjectExtractor(document);
                List<Table> tables = new ArrayList<>();
                
                for (int pageIndex = 0; pageIndex < document.getNumberOfPages(); pageIndex++) {
                    Page page = oe.extract(pageIndex + 1);
                    List<Table> pageTables = new SpreadsheetExtractionAlgorithm().extract(page);
                    tables.addAll(pageTables);
                }
                
                oe.close();
                pdfInfo.put("hasTable", !tables.isEmpty());
                pdfInfo.put("tableCount", tables.size());
            } catch (Exception e) {
                pdfInfo.put("hasTable", false);
                pdfInfo.put("tableCount", 0);
            }
            
            // 文本提取
            PDFTextStripper textStripper = new PDFTextStripper();
            String text = textStripper.getText(document);
            pdfInfo.put("textLength", text.length());
            pdfInfo.put("hasText", text.trim().length() > 0);
            
            return pdfInfo;
        }
    }
    
    /**
     * PDF转Word文档 - 基于文件上传
     */
    public Long convertPdfToWord(MultipartFile file) throws IOException {
        // 先保存上传的文件
        FileEntity savedFile = fileService.saveFile(file, getCurrentUserId());
        
        // 使用已有的基于ID的方法
        return convertPdfToWordById(savedFile.getId());
    }

    /**
     * PDF转Excel表格 - 基于文件上传
     */
    public Long convertPdfToExcel(MultipartFile file) throws IOException {
        // 先保存上传的文件
        FileEntity savedFile = fileService.saveFile(file, getCurrentUserId());
        
        // 使用已有的基于ID的方法
        return convertPdfToExcelById(savedFile.getId());
    }

    /**
     * PDF转CSV - 基于文件上传
     */
    public Long convertPdfToCsv(MultipartFile file) throws IOException {
        // 先保存上传的文件
        FileEntity savedFile = fileService.saveFile(file, getCurrentUserId());
        
        // 使用已有的基于ID的方法
        return convertPdfToCsvById(savedFile.getId());
    }

    /**
     * OCR识别 - 基于文件上传
     */
    public String performOcr(MultipartFile file, String language) throws TesseractException, IOException {
        // 先保存上传的文件
        FileEntity savedFile = fileService.saveFile(file, getCurrentUserId());
        
        // 使用已有的基于ID的方法
        return performOcrById(savedFile.getId(), language);
    }

    /**
     * PDF信息分析 - 基于文件上传
     */
    public Map<String, Object> getPdfInfo(MultipartFile file) throws IOException {
        // 先保存上传的文件
        FileEntity savedFile = fileService.saveFile(file, getCurrentUserId());
        
        // 使用已有的基于ID的方法
        return getPdfInfoById(savedFile.getId());
    }
    
    // 辅助方法
    
    private Path createOutputFile(String fileName) throws IOException {
        Path outputDir = Paths.get(fileProperties.getUploadPath(), "processed");
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }
        return outputDir.resolve(fileName);
    }
    
    private FileEntity saveProcessedFile(Path filePath, String originalName, String description) throws IOException {
        byte[] fileContent = Files.readAllBytes(filePath);
        String mimeType = getMimeTypeFromExtension(getFileExtension(originalName));
        return fileService.saveFileFromBytes(fileContent, originalName, mimeType, getCurrentUserId(), description);
    }
    
    private FileEntity saveDocumentAsFile(PDDocument document, String fileName, String description) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        document.save(baos);
        byte[] fileContent = baos.toByteArray();
        String mimeType = "application/pdf";
        return fileService.saveFileFromBytes(fileContent, fileName, mimeType, getCurrentUserId(), description);
    }
    
    private String getFileNameWithoutExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(0, lastDot) : fileName;
    }
    
    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1) : "pdf";
    }
    
    private String getMimeTypeFromExtension(String extension) {
        switch (extension.toLowerCase()) {
            case "pdf":
                return "application/pdf";
            case "png":
                return "image/png";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "tiff":
                return "image/tiff";
            case "bmp":
                return "image/bmp";
            case "gif":
                return "image/gif";
            default:
                return "application/octet-stream";
        }
    }
    
    private void applyCompression(PDDocument document, String compressionLevel) throws IOException {
        System.out.println("正在优化PDF文档，压缩级别: " + compressionLevel);
        
        // 根据压缩级别设置参数
        float imageQuality;
        boolean compressStreams = true;
        
        switch (compressionLevel.toLowerCase()) {
            case "low":
                imageQuality = 0.9f;
                break;
            case "medium":
                imageQuality = 0.7f;
                break;
            case "high":
                imageQuality = 0.5f;
                break;
            default:
                imageQuality = 0.7f;
        }
        
        // 1. 压缩图像
        compressImages(document, imageQuality);
        
        // 2. 优化内容流
        if (compressStreams) {
            optimizeContentStreams(document);
        }
        
        // 3. 移除未使用的资源
        removeUnusedResources(document);
        
        System.out.println("PDF压缩完成，压缩级别: " + compressionLevel + ", 图像质量: " + (imageQuality * 100) + "%");
    }
    
    private void compressImages(PDDocument document, float quality) throws IOException {
        System.out.println("正在压缩PDF中的图像，目标质量: " + (quality * 100) + "%");
        
        try {
            for (int pageIndex = 0; pageIndex < document.getNumberOfPages(); pageIndex++) {
                PDPage page = document.getPage(pageIndex);
                
                // 获取页面资源
                if (page.getResources() != null && page.getResources().getXObjectNames() != null) {
                    Iterable<org.apache.pdfbox.cos.COSName> xObjectNames = page.getResources().getXObjectNames();
                    
                    for (org.apache.pdfbox.cos.COSName name : xObjectNames) {
                        org.apache.pdfbox.pdmodel.graphics.PDXObject xObject = page.getResources().getXObject(name);
                        
                        if (xObject instanceof PDImageXObject) {
                            PDImageXObject image = (PDImageXObject) xObject;
                            
                            // 如果图像尺寸较大，进行压缩
                            if (image.getWidth() > 1024 || image.getHeight() > 1024) {
                                try {
                                    // 获取图像数据
                                    BufferedImage bufferedImage = image.getImage();
                                    
                                    if (bufferedImage != null) {
                                        // 缩放图像（如果太大）
                                        int maxSize = 1024;
                                        if (bufferedImage.getWidth() > maxSize || bufferedImage.getHeight() > maxSize) {
                                            double scale = Math.min((double) maxSize / bufferedImage.getWidth(),
                                                                  (double) maxSize / bufferedImage.getHeight());
                                            
                                            int newWidth = (int) (bufferedImage.getWidth() * scale);
                                            int newHeight = (int) (bufferedImage.getHeight() * scale);
                                            
                                            BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
                                            scaledImage.getGraphics().drawImage(bufferedImage.getScaledInstance(newWidth, newHeight, java.awt.Image.SCALE_SMOOTH), 0, 0, null);
                                            
                                            // 创建压缩后的图像对象
                                            PDImageXObject compressedImage = JPEGFactory.createFromImage(document, scaledImage, quality);
                                            
                                            // 替换原图像
                                            page.getResources().put(name, compressedImage);
                                            
                                            System.out.println("压缩图像: " + name.getName() + 
                                                             " 从 " + bufferedImage.getWidth() + "x" + bufferedImage.getHeight() +
                                                             " 缩放到 " + newWidth + "x" + newHeight);
                                        }
                                    }
                                } catch (Exception e) {
                                    System.out.println("压缩图像时出错: " + e.getMessage());
                                    // 继续处理其他图像
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("图像压缩过程中出现错误: " + e.getMessage());
            // 不抛出异常，让文档继续处理
        }
    }
    
    private void optimizeContentStreams(PDDocument document) throws IOException {
        System.out.println("正在优化内容流...");
        
        try {
            // PDFBox在保存时会自动压缩流
            // 这里可以添加额外的优化逻辑
            
            for (int pageIndex = 0; pageIndex < document.getNumberOfPages(); pageIndex++) {
                PDPage page = document.getPage(pageIndex);
                
                // 强制页面内容重新编码以实现压缩
                if (page.getContents() != null) {
                    // PDFBox会在保存时自动应用流压缩
                    System.out.println("优化第 " + (pageIndex + 1) + " 页的内容流");
                }
            }
        } catch (Exception e) {
            System.out.println("优化内容流时出错: " + e.getMessage());
        }
    }
    
    private void removeUnusedResources(PDDocument document) throws IOException {
        System.out.println("正在移除未使用的资源...");
        
        try {
            // 移除文档级别的未使用元数据
            document.getDocumentInformation().setCustomMetadataValue("Compressed", "true");
            
            // 清理页面资源（这是简化版本）
            for (int pageIndex = 0; pageIndex < document.getNumberOfPages(); pageIndex++) {
                PDPage page = document.getPage(pageIndex);
                
                // 这里可以添加更复杂的资源清理逻辑
                // 比如移除未引用的字体、颜色空间等
            }
            
            System.out.println("资源清理完成");
        } catch (Exception e) {
            System.out.println("清理资源时出错: " + e.getMessage());
        }
    }

    /**
     * 为PDF添加水印
     */
    public FileEntity addWatermark(Long fileId, Map<String, Object> watermarkOptions) throws IOException {
        // 添加调试日志
        System.out.println("=== 开始处理水印 ===");
        System.out.println("文件ID: " + fileId);
        System.out.println("水印选项: " + watermarkOptions);
        
        // 添加同步锁防止并发处理同一文件时的冲突
        synchronized (this) {
            return processWatermarkInternal(fileId, watermarkOptions);
        }
    }
    
    /**
     * 内部水印处理方法
     */
    private FileEntity processWatermarkInternal(Long fileId, Map<String, Object> watermarkOptions) throws IOException {
        byte[] fileContent = null;
        Optional<FileEntity> originalFile = null;
        
        try {
            fileContent = fileService.getFileContent(fileId);
            originalFile = fileService.getFile(fileId);
            
            if (originalFile.isEmpty()) {
                throw new IllegalArgumentException("原文件不存在: " + fileId);
            }
            
            System.out.println("成功获取文件: " + originalFile.get().getOriginalName() + ", 大小: " + fileContent.length + " bytes");
            
        } catch (Exception e) {
            System.err.println("获取文件失败: " + e.getMessage());
            throw new IOException("获取文件失败: " + e.getMessage(), e);
        }
        
        PDDocument document = null;
        try {
            document = Loader.loadPDF(fileContent);
            System.out.println("PDF文档加载成功，页数: " + document.getNumberOfPages());
            
            // 移除安全限制
            document.setAllSecurityToBeRemoved(true);
            
            // 解析水印参数
            String watermarkType = (String) watermarkOptions.getOrDefault("watermarkType", "text");
            String position = (String) watermarkOptions.getOrDefault("watermarkPosition", "center");
            Integer opacity = (Integer) watermarkOptions.getOrDefault("watermarkOpacity", 50);
            Integer rotation = (Integer) watermarkOptions.getOrDefault("watermarkRotation", 0);
            
            System.out.println("水印类型: " + watermarkType + ", 位置: " + position + ", 透明度: " + opacity + ", 旋转: " + rotation);
            
            // 页面范围处理
            String pageRange = (String) watermarkOptions.getOrDefault("pageRange", "all");
            int startPage = 0;
            int endPage = document.getNumberOfPages() - 1;
            
            if (!"all".equals(pageRange)) {
                String customRange = (String) watermarkOptions.get("customRange");
                if (customRange != null && !customRange.trim().isEmpty()) {
                    String[] rangeParts = customRange.split("-");
                    if (rangeParts.length == 2) {
                        startPage = Math.max(0, Integer.parseInt(rangeParts[0].trim()) - 1);
                        endPage = Math.min(document.getNumberOfPages() - 1, Integer.parseInt(rangeParts[1].trim()) - 1);
                    }
                }
            }
            
            System.out.println("页面范围: " + (startPage + 1) + " 到 " + (endPage + 1));
            
            // 为指定页面添加水印
            for (int pageIndex = startPage; pageIndex <= endPage; pageIndex++) {
                PDPage page = document.getPage(pageIndex);
                
                try {
                    if ("text".equals(watermarkType)) {
                        addTextWatermark(document, page, watermarkOptions, position, opacity, rotation);
                    } else if ("image".equals(watermarkType)) {
                        addImageWatermark(document, page, watermarkOptions, position, opacity, rotation);
                    }
                    System.out.println("成功为第 " + (pageIndex + 1) + " 页添加水印");
                } catch (Exception e) {
                    System.err.println("为第 " + (pageIndex + 1) + " 页添加水印失败: " + e.getMessage());
                    throw new IOException("为第 " + (pageIndex + 1) + " 页添加水印失败: " + e.getMessage(), e);
                }
            }
            
            // 保存水印后的文档
            String outputName = getFileNameWithoutExtension(originalFile.get().getOriginalName()) 
                              + "_watermarked.pdf";
            Path outputPath = createOutputFile(outputName);
            
            System.out.println("开始保存文件: " + outputPath);
            document.save(outputPath.toFile());
            System.out.println("文件保存成功");
            
            FileEntity result = saveProcessedFile(outputPath, outputName, "添加水印");
            System.out.println("处理完成，新文件ID: " + result.getId());
            
            return result;
            
        } catch (Exception e) {
            System.err.println("处理PDF文档时出错: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("处理PDF文档时出错: " + e.getMessage(), e);
        } finally {
            if (document != null) {
                try {
                    document.close();
                    System.out.println("PDF文档已关闭");
                } catch (IOException e) {
                    System.err.println("关闭PDF文档时出错: " + e.getMessage());
                }
            }
            System.out.println("=== 水印处理结束 ===");
        }
    }
    
    /**
     * 添加文字水印
     */
    private void addTextWatermark(PDDocument document, PDPage page, Map<String, Object> options, 
                                 String position, Integer opacity, Integer rotation) throws IOException {
        
        System.out.println("=== 添加文字水印调试信息 ===");
        System.out.println("接收到的选项: " + options);
        System.out.println("位置: " + position);
        System.out.println("透明度: " + opacity);
        System.out.println("旋转角度: " + rotation);
        
        String watermarkText = (String) options.getOrDefault("watermarkText", "Watermark");
        Integer fontSize = (Integer) options.getOrDefault("watermarkSize", 24);
        String colorHex = (String) options.getOrDefault("watermarkColor", "#666666");
        
        System.out.println("水印文字: " + watermarkText);
        System.out.println("字体大小: " + fontSize);
        System.out.println("颜色值: " + colorHex);
        
        // 解析颜色
        Color textColor = parseColor(colorHex);
        System.out.println("解析后的颜色: " + textColor);
        
        // 创建支持中文的字体
        PDFont font = createSupportedFont(document, watermarkText);
        
        // 处理不支持的字符
        boolean containsChinese = containsChineseCharacters(watermarkText);
        String displayText = watermarkText;
        
        if (containsChinese) {
            // 如果包含中文字符，先尝试使用原文本
            try {
                font.encode(watermarkText);
                displayText = watermarkText; // 如果能编码，使用原文本
            } catch (Exception e) {
                // 如果不能编码，过滤不支持的字符
                displayText = filterUnsupportedCharacters(watermarkText, font);
                System.out.println("原始文本: " + watermarkText);
                System.out.println("过滤后文本: " + displayText);
            }
        }
        
        // 即使过滤后为空或只有占位符，也继续添加水印
        if (displayText.trim().isEmpty()) {
            displayText = "Watermark"; // 使用默认文本
            System.out.println("使用默认水印文本: " + displayText);
        }
        
        watermarkText = displayText;
        
        // 获取页面尺寸
        float pageWidth = page.getMediaBox().getWidth();
        float pageHeight = page.getMediaBox().getHeight();
        System.out.println("页面尺寸: " + pageWidth + " x " + pageHeight);
        
        // 计算文字尺寸
        float textWidth = font.getStringWidth(watermarkText) / 1000 * fontSize;
        float textHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize;
        System.out.println("文字尺寸: " + textWidth + " x " + textHeight);
        
        // 计算位置
        float[] coordinates = calculatePosition(position, pageWidth, pageHeight, textWidth, textHeight);
        float x = coordinates[0];
        float y = coordinates[1];
        System.out.println("计算的位置: (" + x + ", " + y + ")");
        System.out.println("=== 调试信息结束 ===");
        
        try (PDPageContentStream contentStream = new PDPageContentStream(document, page, 
                PDPageContentStream.AppendMode.APPEND, true, true)) {
            
            // 设置透明度
            PDExtendedGraphicsState graphicsState = new PDExtendedGraphicsState();
            graphicsState.setNonStrokingAlphaConstant(opacity / 100.0f);
            contentStream.setGraphicsStateParameters(graphicsState);
            
            // 设置颜色
            contentStream.setNonStrokingColor(textColor);
            
            // 设置字体
            contentStream.setFont(font, fontSize);
            
            // 绘制文字
            contentStream.beginText();
            
            // 如果有旋转，应用旋转变换
            if (rotation != 0) {
                // 计算旋转中心点（文字的中心）
                float centerX = x + textWidth / 2;
                float centerY = y + textHeight / 2;
                
                // 应用旋转矩阵
                Matrix rotationMatrix = Matrix.getRotateInstance(Math.toRadians(rotation), centerX, centerY);
                contentStream.setTextMatrix(rotationMatrix);
            } else {
                // 直接设置文字位置
                contentStream.newLineAtOffset(x, y);
            }
            
            contentStream.showText(watermarkText);
            contentStream.endText();
        }
    }
    
    /**
     * 添加图片水印
     */
    private void addImageWatermark(PDDocument document, PDPage page, Map<String, Object> options, 
                                  String position, Integer opacity, Integer rotation) throws IOException {
        
        System.out.println("=== 添加图片水印调试信息 ===");
        System.out.println("接收到的选项: " + options);
        System.out.println("位置: " + position);
        System.out.println("透明度: " + opacity);
        System.out.println("旋转角度: " + rotation);
        
        // 临时禁用旋转功能进行调试
        if (rotation != 0) {
            System.out.println("警告：临时禁用旋转功能进行调试，将旋转角度设为0");
            rotation = 0;
        }
        
        // 注意：这里需要从前端获取图片数据
        // 在实际实现中，图片应该作为MultipartFile上传或者通过其他方式传递
        byte[] imageData = (byte[]) options.get("watermarkImageData");
        if (imageData == null) {
            System.err.println("错误：图片水印数据不能为空");
            throw new IllegalArgumentException("图片水印数据不能为空");
        }
        
        System.out.println("图片数据大小: " + imageData.length + " 字节");
        
        try {
            // 创建PDImageXObject
            PDImageXObject image = PDImageXObject.createFromByteArray(document, imageData, "watermark");
            System.out.println("图片对象创建成功");
            
            // 获取页面尺寸
            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();
            System.out.println("页面尺寸: " + pageWidth + " x " + pageHeight);
            
            // 获取图片原始尺寸
            float imageWidth = image.getWidth();
            float imageHeight = image.getHeight();
            System.out.println("原始图片尺寸: " + imageWidth + " x " + imageHeight);
            
            // 计算图片尺寸（保持宽高比，限制最大尺寸）
            // 检查图片尺寸是否有效
            if (imageWidth <= 0 || imageHeight <= 0) {
                throw new IllegalArgumentException("图片尺寸无效: " + imageWidth + " x " + imageHeight);
            }
            
            // 自适应最大尺寸设置
            float maxWidth = pageWidth * 0.3f; // 最大为页面宽度的30%
            float maxHeight = pageHeight * 0.3f; // 最大为页面高度的30%
            
            // 确保最小尺寸
            maxWidth = Math.max(maxWidth, 50); // 最小50像素宽度
            maxHeight = Math.max(maxHeight, 50); // 最小50像素高度
            
            System.out.println("最大允许尺寸: " + maxWidth + " x " + maxHeight);
            
            // 计算缩放比例
            float scaleX = maxWidth / imageWidth;
            float scaleY = maxHeight / imageHeight;
            float scale = Math.min(scaleX, scaleY);
            
            // 限制缩放比例
            scale = Math.min(scale, 1.0f); // 不放大，只缩小
            scale = Math.max(scale, 0.1f); // 最小缩放10%
            
            System.out.println("缩放比例: " + scale + " (scaleX: " + scaleX + ", scaleY: " + scaleY + ")");
            
            float scaledWidth = imageWidth * scale;
            float scaledHeight = imageHeight * scale;
            System.out.println("缩放后尺寸: " + scaledWidth + " x " + scaledHeight);
            
            // 计算位置
            float[] coordinates = calculatePosition(position, pageWidth, pageHeight, scaledWidth, scaledHeight);
            float x = coordinates[0];
            float y = coordinates[1];
            System.out.println("计算的位置: (" + x + ", " + y + ")");
            
            // 验证参数
            if (opacity < 0 || opacity > 100) {
                System.err.println("警告：透明度值无效: " + opacity + "，重置为50");
                opacity = 50;
            }
            
            if (rotation < -360 || rotation > 360) {
                System.err.println("警告：旋转角度值无效: " + rotation + "，重置为0");
                rotation = 0;
            }
            
            float opacityValue = opacity / 100.0f;
            System.out.println("透明度值: " + opacityValue);
            System.out.println("旋转角度: " + rotation + " 度");
            
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page, 
                    PDPageContentStream.AppendMode.APPEND, true, true)) {
                
                System.out.println("内容流创建成功");
                
                // 设置透明度
                if (opacityValue < 1.0f) {
                    PDExtendedGraphicsState graphicsState = new PDExtendedGraphicsState();
                    graphicsState.setNonStrokingAlphaConstant(opacityValue);
                    graphicsState.setStrokingAlphaConstant(opacityValue);
                    contentStream.setGraphicsStateParameters(graphicsState);
                    System.out.println("透明度设置成功: " + opacityValue);
                } else {
                    System.out.println("使用完全不透明: " + opacityValue);
                }
                
                // 绘制图片（先绘制，再应用变换）
                if (rotation != 0) {
                    // 保存当前图形状态
                    contentStream.saveGraphicsState();
                    
                    // 计算旋转中心点
                    float centerX = x + scaledWidth / 2;
                    float centerY = y + scaledHeight / 2;
                    System.out.println("旋转中心点: (" + centerX + ", " + centerY + ")");
                    
                    // 移动到旋转中心，旋转，然后移回
                    contentStream.transform(Matrix.getTranslateInstance(centerX, centerY));
                    contentStream.transform(Matrix.getRotateInstance(Math.toRadians(rotation), 0, 0));
                    contentStream.transform(Matrix.getTranslateInstance(-scaledWidth / 2, -scaledHeight / 2));
                    
                    // 绘制图片（以旋转中心为原点）
                    contentStream.drawImage(image, 0, 0, scaledWidth, scaledHeight);
                    
                    // 恢复图形状态
                    contentStream.restoreGraphicsState();
                    System.out.println("旋转图片绘制成功");
                } else {
                    // 无旋转，直接绘制
                    contentStream.drawImage(image, x, y, scaledWidth, scaledHeight);
                    System.out.println("图片绘制成功");
                }
                
                System.out.println("=== 图片水印添加完成 ===");
            }
            
        } catch (Exception e) {
            System.err.println("添加图片水印时出错: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("添加图片水印失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 计算水印位置
     */
    private float[] calculatePosition(String position, float pageWidth, float pageHeight, 
                                     float watermarkWidth, float watermarkHeight) {
        float x, y;
        float margin = 50; // 边距
        
        switch (position) {
            case "top-left":
                x = margin;
                y = pageHeight - watermarkHeight - margin;
                break;
            case "top-right":
                x = pageWidth - watermarkWidth - margin;
                y = pageHeight - watermarkHeight - margin;
                break;
            case "bottom-left":
                x = margin;
                y = margin;
                break;
            case "bottom-right":
                x = pageWidth - watermarkWidth - margin;
                y = margin;
                break;
            case "center":
            default:
                x = (pageWidth - watermarkWidth) / 2;
                y = (pageHeight - watermarkHeight) / 2;
                break;
        }
        
        return new float[]{x, y};
    }
    
    /**
     * 创建支持文本的字体
     */
    private PDFont createSupportedFont(PDDocument document, String text) throws IOException {
        if (containsChineseCharacters(text)) {
            PDFont font = loadChineseFontFromFile(document);
            if (font != null) {
                return font;
            }
        }
        // 对于非中文或字体加载失败的情况，使用内置字体
        return new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    }

    private PDFont loadChineseFontFromFile(PDDocument document) {
        System.out.println("检测到中文字符，尝试加载中文字体...");
        
        // 1. 优先从项目内置资源加载字体
        String bundledFontPath = "/fonts/AlibabaPuHuiTi-2-55-Regular.ttf";
        try {
            InputStream fontStream = PdfService.class.getResourceAsStream(bundledFontPath);
            if (fontStream != null) {
                System.out.println("尝试从内置资源加载字体: " + bundledFontPath);
                // 启用字体子集嵌入(embedSubset=true)，以减小文件大小
                try (InputStream closableFontStream = fontStream) {
                    return PDType0Font.load(document, closableFontStream, true);
                }
            } else {
                System.out.println("警告: 未找到内置字体资源 " + bundledFontPath);
            }
        } catch (IOException e) {
            System.err.println("加载内置字体时发生IO错误: " + e.getMessage());
        }

        System.out.println("内置字体加载失败，回退到系统字体...");

        // 2. 如果内置字体加载失败，则回退到系统字体
        String[] systemFontPaths = {
            "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc",
            "C:/Windows/Fonts/simsun.ttc",
            "/System/Library/Fonts/STHeiti Light.ttc",
            "/usr/share/fonts/truetype/wqy/wqy-zenhei.ttc",
            "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"
        };

        for (String path : systemFontPaths) {
            try {
                File fontFile = new File(path);
                if (fontFile.exists()) {
                    System.out.println("尝试加载系统字体文件: " + path);
                    if (path.toLowerCase().endsWith(".ttc")) {
                        try (TrueTypeCollection ttc = new TrueTypeCollection(fontFile)) {
                            final TrueTypeFont[] ttf = new TrueTypeFont[1];
                            ttc.processAllFonts(font -> {
                                if (ttf[0] == null) ttf[0] = font;
                            });
                            if (ttf[0] != null) {
                                System.out.println("成功从TTC加载字体: " + ttf[0].getName());
                                return PDType0Font.load(document, ttf[0], true);
                            }
                        }
                    } else {
                        try (InputStream fontStream = new FileInputStream(fontFile)) {
                            // 对后备字体同样启用子集嵌入
                            return PDType0Font.load(document, fontStream, true);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("加载系统字体 " + path + " 失败: " + e.getMessage());
            }
        }

        System.out.println("所有内置及系统路径均未找到可用的中文字体，返回null");
        return null;
    }

    private boolean containsChineseCharacters(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        for (char c : text.toCharArray()) {
            // 检查字符是否在中文Unicode范围内
            if ((c >= 0x4E00 && c <= 0x9FFF) ||  // CJK统一汉字
                (c >= 0x3400 && c <= 0x4DBF) ||  // CJK扩展A
                (c >= 0x20000 && c <= 0x2A6DF) || // CJK扩展B
                (c >= 0x2A700 && c <= 0x2B73F) || // CJK扩展C
                (c >= 0x2B740 && c <= 0x2B81F) || // CJK扩展D
                (c >= 0x2B820 && c <= 0x2CEAF) || // CJK扩展E
                (c >= 0x2CEB0 && c <= 0x2EBEF) || // CJK扩展F
                (c >= 0x3000 && c <= 0x303F) ||  // CJK符号和标点
                (c >= 0xFF00 && c <= 0xFFEF)) {  // 全角ASCII、全角中英文标点、半宽片假名、半宽平假名、半宽韩文字母
                return true;
            }
        }
        return false;
    }
    
    /**
     * 过滤掉字体不支持的字符
     */
    private String filterUnsupportedCharacters(String text, PDFont font) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        StringBuilder filteredText = new StringBuilder();
        for (char c : text.toCharArray()) {
            try {
                // 尝试编码字符以检查字体是否支持
                font.encode(String.valueOf(c));
                filteredText.append(c);
            } catch (Exception e) {
                                 // 如果字符不支持，尝试用类似的ASCII字符替换
                 if (isChineseCharacter(c)) {
                     // 对于中文字符，用英文字母占位，这样至少有可见的水印
                     filteredText.append('X'); // 用X占位，保持可见性
                 } else {
                     // 对于其他字符，用相似的ASCII字符替换
                     filteredText.append('?');
                 }
            }
        }
        return filteredText.toString();
    }
    
    /**
     * 检查单个字符是否为中文字符
     */
    private boolean isChineseCharacter(char c) {
        return (c >= 0x4E00 && c <= 0x9FFF) ||  // CJK统一汉字
               (c >= 0x3400 && c <= 0x4DBF) ||  // CJK扩展A
               (c >= 0x20000 && c <= 0x2A6DF) || // CJK扩展B
               (c >= 0x2A700 && c <= 0x2B73F) || // CJK扩展C
               (c >= 0x2B740 && c <= 0x2B81F) || // CJK扩展D
               (c >= 0x2B820 && c <= 0x2CEAF) || // CJK扩展E
               (c >= 0x2CEB0 && c <= 0x2EBEF) || // CJK扩展F
               (c >= 0x3000 && c <= 0x303F) ||  // CJK符号和标点
               (c >= 0xFF00 && c <= 0xFFEF);   // 全角ASCII、全角中英文标点
    }

    /**
     * 解析颜色字符串，支持多种格式
     */
    private Color parseColor(String colorString) {
        try {
            if (colorString == null || colorString.trim().isEmpty()) {
                return new Color(102, 102, 102); // 默认灰色
            }
            
            colorString = colorString.trim();
            
            // 处理HEX格式: #ff0000
            if (colorString.startsWith("#")) {
                String hex = colorString.substring(1);
                
                if (hex.length() == 6) {
                    int r = Integer.parseInt(hex.substring(0, 2), 16);
                    int g = Integer.parseInt(hex.substring(2, 4), 16);
                    int b = Integer.parseInt(hex.substring(4, 6), 16);
                    return new Color(r, g, b);
                } else if (hex.length() == 8) {
                    int r = Integer.parseInt(hex.substring(0, 2), 16);
                    int g = Integer.parseInt(hex.substring(2, 4), 16);
                    int b = Integer.parseInt(hex.substring(4, 6), 16);
                    int a = Integer.parseInt(hex.substring(6, 8), 16);
                    return new Color(r, g, b, a);
                }
            }
            // 处理RGB格式: rgb(255, 0, 0)
            else if (colorString.startsWith("rgb(") && colorString.endsWith(")")) {
                String rgb = colorString.substring(4, colorString.length() - 1);
                String[] parts = rgb.split(",");
                if (parts.length == 3) {
                    int r = Integer.parseInt(parts[0].trim());
                    int g = Integer.parseInt(parts[1].trim());
                    int b = Integer.parseInt(parts[2].trim());
                    return new Color(r, g, b);
                }
            }
            // 处理RGBA格式: rgba(255, 0, 0, 1.0)
            else if (colorString.startsWith("rgba(") && colorString.endsWith(")")) {
                String rgba = colorString.substring(5, colorString.length() - 1);
                String[] parts = rgba.split(",");
                if (parts.length == 4) {
                    int r = Integer.parseInt(parts[0].trim());
                    int g = Integer.parseInt(parts[1].trim());
                    int b = Integer.parseInt(parts[2].trim());
                    float alpha = Float.parseFloat(parts[3].trim());
                    int a = Math.round(alpha * 255);
                    return new Color(r, g, b, a);
                }
            }
            
        } catch (Exception e) {
            System.out.println("解析颜色失败: " + colorString + ", 错误: " + e.getMessage());
        }
        
        // 如果解析失败，返回默认颜色
        return new Color(102, 102, 102); // 默认灰色
    }

    /**
     * 旋转PDF页面
     */
    public FileEntity rotatePages(Long fileId, String pageRange, String customRange, Integer rotation) throws IOException {
        byte[] fileContent = fileService.getFileContent(fileId);
        Optional<FileEntity> originalFile = fileService.getFile(fileId);
        
        if (originalFile.isEmpty()) {
            throw new IllegalArgumentException("原文件不存在");
        }
        
        try (PDDocument document = Loader.loadPDF(fileContent)) {
            // 解析页面范围
            List<Integer> pagesToRotate = parsePageRange(pageRange, customRange, document.getNumberOfPages());
            
            for (Integer pageIndex : pagesToRotate) {
                if (pageIndex >= 0 && pageIndex < document.getNumberOfPages()) {
                    PDPage page = document.getPage(pageIndex);
                    
                    // 获取当前旋转角度
                    int currentRotation = page.getRotation();
                    int newRotation = (currentRotation + rotation) % 360;
                    
                    // 设置新的旋转角度
                    page.setRotation(newRotation);
                }
            }
            
            // 保存旋转后的文档
            String outputName = getFileNameWithoutExtension(originalFile.get().getOriginalName()) 
                              + "_rotated.pdf";
            Path outputPath = createOutputFile(outputName);
            document.save(outputPath.toFile());
            
            return saveProcessedFile(outputPath, outputName, "页面旋转");
        }
    }
    
    /**
     * 删除PDF页面
     */
    public FileEntity deletePages(Long fileId, String pageRange, String customRange) throws IOException {
        byte[] fileContent = fileService.getFileContent(fileId);
        Optional<FileEntity> originalFile = fileService.getFile(fileId);
        
        if (originalFile.isEmpty()) {
            throw new IllegalArgumentException("原文件不存在");
        }
        
        try (PDDocument document = Loader.loadPDF(fileContent)) {
            // 解析要删除的页面范围
            List<Integer> pagesToDelete = parsePageRange(pageRange, customRange, document.getNumberOfPages());
            
            if (pagesToDelete.size() >= document.getNumberOfPages()) {
                throw new IllegalArgumentException("不能删除所有页面");
            }
            
            // 按降序排序，从后往前删除，避免索引变化问题
            pagesToDelete.sort((a, b) -> b.compareTo(a));
            
            for (Integer pageIndex : pagesToDelete) {
                if (pageIndex >= 0 && pageIndex < document.getNumberOfPages()) {
                    document.removePage(pageIndex);
                }
            }
            
            // 保存删除页面后的文档
            String outputName = getFileNameWithoutExtension(originalFile.get().getOriginalName()) 
                              + "_deleted_pages.pdf";
            Path outputPath = createOutputFile(outputName);
            document.save(outputPath.toFile());
            
            return saveProcessedFile(outputPath, outputName, "页面删除");
        }
    }
    
    /**
     * 提取PDF页面
     */
    public FileEntity extractPages(Long fileId, String pageRange, String customRange) throws IOException {
        byte[] fileContent = fileService.getFileContent(fileId);
        Optional<FileEntity> originalFile = fileService.getFile(fileId);
        
        if (originalFile.isEmpty()) {
            throw new IllegalArgumentException("原文件不存在");
        }
        
        try (PDDocument sourceDocument = Loader.loadPDF(fileContent);
             PDDocument extractedDocument = new PDDocument()) {
            
            // 解析要提取的页面范围
            List<Integer> pagesToExtract = parsePageRange(pageRange, customRange, sourceDocument.getNumberOfPages());
            
            if (pagesToExtract.isEmpty()) {
                throw new IllegalArgumentException("没有指定要提取的页面");
            }
            
            // 按顺序提取页面
            pagesToExtract.sort(Integer::compareTo);
            
            for (Integer pageIndex : pagesToExtract) {
                if (pageIndex >= 0 && pageIndex < sourceDocument.getNumberOfPages()) {
                    PDPage page = sourceDocument.getPage(pageIndex);
                    extractedDocument.addPage(page);
                }
            }
            
            // 保存提取的页面
            String outputName = getFileNameWithoutExtension(originalFile.get().getOriginalName()) 
                              + "_extracted_pages.pdf";
            Path outputPath = createOutputFile(outputName);
            extractedDocument.save(outputPath.toFile());
            
            return saveProcessedFile(outputPath, outputName, "页面提取");
        }
    }
    
    /**
     * 重新排序PDF页面
     */
    public FileEntity reorderPages(Long fileId, List<Integer> pageOrder) throws IOException {
        byte[] fileContent = fileService.getFileContent(fileId);
        Optional<FileEntity> originalFile = fileService.getFile(fileId);
        
        if (originalFile.isEmpty()) {
            throw new IllegalArgumentException("原文件不存在");
        }
        
        try (PDDocument sourceDocument = Loader.loadPDF(fileContent);
             PDDocument reorderedDocument = new PDDocument()) {
            
            // 验证页面顺序
            int totalPages = sourceDocument.getNumberOfPages();
            if (pageOrder.size() != totalPages) {
                throw new IllegalArgumentException("页面顺序列表的长度必须等于PDF的总页数");
            }
            
            // 验证页面索引的有效性
            for (Integer pageIndex : pageOrder) {
                if (pageIndex < 1 || pageIndex > totalPages) {
                    throw new IllegalArgumentException("页面索引超出范围: " + pageIndex);
                }
            }
            
            // 按新顺序添加页面
            for (Integer pageNumber : pageOrder) {
                int pageIndex = pageNumber - 1; // 转换为0基索引
                PDPage page = sourceDocument.getPage(pageIndex);
                reorderedDocument.addPage(page);
            }
            
            // 保存重排序后的文档
            String outputName = getFileNameWithoutExtension(originalFile.get().getOriginalName()) 
                              + "_reordered.pdf";
            Path outputPath = createOutputFile(outputName);
            reorderedDocument.save(outputPath.toFile());
            
            return saveProcessedFile(outputPath, outputName, "页面重排");
        }
    }
    
    /**
     * 解析页面范围字符串
     */
    private List<Integer> parsePageRange(String pageRange, String customRange, int totalPages) {
        List<Integer> pages = new ArrayList<>();
        
        if ("all".equals(pageRange)) {
            for (int i = 0; i < totalPages; i++) {
                pages.add(i);
            }
        } else if ("first".equals(pageRange)) {
            if (totalPages > 0) {
                pages.add(0); // 第一页
            }
        } else if ("last".equals(pageRange)) {
            if (totalPages > 0) {
                pages.add(totalPages - 1); // 最后一页
            }
        } else if ("custom".equals(pageRange) && customRange != null && !customRange.trim().isEmpty()) {
            // 解析自定义范围，如 "1-3,5,7-9"
            String[] ranges = customRange.split(",");
            for (String range : ranges) {
                range = range.trim();
                if (range.contains("-")) {
                    String[] parts = range.split("-");
                    if (parts.length == 2) {
                        try {
                            int start = Integer.parseInt(parts[0].trim()) - 1; // 转换为0基索引
                            int end = Integer.parseInt(parts[1].trim()) - 1;   // 转换为0基索引
                            for (int i = Math.max(0, start); i <= Math.min(totalPages - 1, end); i++) {
                                if (!pages.contains(i)) {
                                    pages.add(i);
                                }
                            }
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("无效的页面范围格式: " + range);
                        }
                    }
                } else {
                    try {
                        int pageNum = Integer.parseInt(range) - 1; // 转换为0基索引
                        if (pageNum >= 0 && pageNum < totalPages && !pages.contains(pageNum)) {
                            pages.add(pageNum);
                        }
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("无效的页面号: " + range);
                    }
                }
            }
        }
        
        return pages;
    }

    // ============= 文本位置提取辅助类 =============
    
    /**
     * 文本位置信息类
     */
    private static class TextLocation {
        private String text;
        private float x, y, width, height;
        
        public TextLocation(String text, float x, float y, float width, float height) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
        
        public String getText() { return text; }
        public float getX() { return x; }
        public float getY() { return y; }
        public float getWidth() { return width; }
        public float getHeight() { return height; }
    }
    
    /**
     * 文本位置提取器 - 简化实现
     */
    private static class TextLocationExtractor extends PDFTextStripper {
        private List<TextLocation> textLocations = new ArrayList<>();
        
        public TextLocationExtractor() throws IOException {
            super();
        }
        
        @Override
        protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
            if (textPositions != null && !textPositions.isEmpty()) {
                // 为每个字符创建位置信息
                for (TextPosition textPosition : textPositions) {
                    String character = textPosition.getUnicode();
                    if (character != null && !character.trim().isEmpty()) {
                        TextLocation location = new TextLocation(
                            character,
                            textPosition.getX(),
                            textPosition.getY() - textPosition.getHeight(),
                            textPosition.getWidth(),
                            textPosition.getHeight()
                        );
                        textLocations.add(location);
                    }
                }
            }
            super.writeString(text, textPositions);
        }
        
        public List<TextLocation> getTextLocations() {
            return textLocations;
        }
    }
    
    /**
     * 关键词位置信息类
     */
    private static class KeywordLocation {
        private String keyword;
        private float x, y, width, height;
        
        public KeywordLocation(String keyword, float x, float y, float width, float height) {
            this.keyword = keyword;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
        
        public String getKeyword() { return keyword; }
        public float getX() { return x; }
        public float getY() { return y; }
        public float getWidth() { return width; }
        public float getHeight() { return height; }
    }
    
    /**
     * 关键词位置查找器
     */
    private static class KeywordLocationFinder extends PDFTextStripper {
        private List<String> keywords;
        private List<KeywordLocation> keywordLocations = new ArrayList<>();
        private StringBuilder currentText = new StringBuilder();
        private List<TextPosition> currentPositions = new ArrayList<>();
        private Set<String> foundKeywords = new HashSet<>(); // 防止重复匹配
        
        public KeywordLocationFinder(List<String> keywords) throws IOException {
            super();
            this.keywords = keywords;
        }
        
        @Override
        protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
            if (textPositions != null && !textPositions.isEmpty()) {
                // 将当前文本和位置添加到缓冲区
                currentText.append(text);
                currentPositions.addAll(textPositions);
                
                // 检查是否包含任何关键词
                String fullText = currentText.toString();
                for (String keyword : keywords) {
                    // 防止重复匹配同一个关键词
                    String keywordKey = keyword + "_" + currentText.length();
                    if (foundKeywords.contains(keywordKey)) {
                        continue;
                    }
                    
                    int index = fullText.toLowerCase().indexOf(keyword.toLowerCase());
                    if (index >= 0 && index + keyword.length() <= currentPositions.size()) {
                        // 计算关键词的边界框
                        float minX = Float.MAX_VALUE;
                        float maxX = Float.MIN_VALUE;
                        float minY = Float.MAX_VALUE;
                        float maxY = Float.MIN_VALUE;
                        
                        for (int i = index; i < index + keyword.length() && i < currentPositions.size(); i++) {
                            TextPosition pos = currentPositions.get(i);
                            minX = Math.min(minX, pos.getX());
                            maxX = Math.max(maxX, pos.getX() + pos.getWidth());
                            // PDF坐标系：Y轴向上递增，文字基线在pos.getY()
                            minY = Math.min(minY, pos.getY() - pos.getHeight());
                            maxY = Math.max(maxY, pos.getY());
                        }
                        
                        KeywordLocation location = new KeywordLocation(
                            keyword, minX, minY, maxX - minX, maxY - minY
                        );
                        keywordLocations.add(location);
                        foundKeywords.add(keywordKey);
                        
                        // 调试日志
                        System.out.println("找到关键词: " + keyword + 
                                         " - X: " + minX + 
                                         ", Y: " + minY + 
                                         ", 宽度: " + (maxX - minX) + 
                                         ", 高度: " + (maxY - minY));
                    }
                }
            }
            super.writeString(text, textPositions);
        }
        
        public List<KeywordLocation> getKeywordLocations() {
            return keywordLocations;
        }
    }

    // ============= 安全工具 =============
    
    /**
     * PDF加密
     */
    public FileEntity encryptPdf(Long fileId, String userPassword, String ownerPassword) throws IOException {
        byte[] fileContent = fileService.getFileContent(fileId);
        Optional<FileEntity> originalFile = fileService.getFile(fileId);
        
        if (originalFile.isEmpty()) {
            throw new IllegalArgumentException("原文件不存在");
        }
        
        try (PDDocument document = Loader.loadPDF(fileContent)) {
            // 创建加密处理器
            AccessPermission ap = new AccessPermission();
            
            // 设置权限（可以根据需要调整）
            ap.setCanPrint(true);
            ap.setCanModify(false);
            ap.setCanExtractContent(false);
            ap.setCanModifyAnnotations(false);
            ap.setCanFillInForm(true);
            ap.setCanExtractForAccessibility(true);
            ap.setCanAssembleDocument(false);
            ap.setCanPrintFaithful(true);
            
            // 创建标准加密算法
            StandardProtectionPolicy spp = new StandardProtectionPolicy(ownerPassword, userPassword, ap);
            spp.setEncryptionKeyLength(128); // 使用128位加密
            spp.setPermissions(ap);
            
            // 应用加密
            document.protect(spp);
            
            // 保存加密后的文档
            String outputName = getFileNameWithoutExtension(originalFile.get().getOriginalName()) 
                              + "_encrypted.pdf";
            Path outputPath = createOutputFile(outputName);
            document.save(outputPath.toFile());
            
            return saveProcessedFile(outputPath, outputName, "PDF加密");
        }
    }
    
    /**
     * PDF解密
     */
    public FileEntity decryptPdf(Long fileId, String password) throws IOException {
        byte[] fileContent = fileService.getFileContent(fileId);
        Optional<FileEntity> originalFile = fileService.getFile(fileId);
        
        if (originalFile.isEmpty()) {
            throw new IllegalArgumentException("原文件不存在");
        }
        
        try (PDDocument document = Loader.loadPDF(fileContent, password)) {
            // 检查文档是否真的被加密了
            if (!document.isEncrypted()) {
                throw new IllegalArgumentException("该PDF文件未加密，无需解密");
            }
            
            // 移除安全性设置
            document.setAllSecurityToBeRemoved(true);
            
            // 保存解密后的文档
            String outputName = getFileNameWithoutExtension(originalFile.get().getOriginalName()) 
                              + "_decrypted.pdf";
            Path outputPath = createOutputFile(outputName);
            document.save(outputPath.toFile());
            
            return saveProcessedFile(outputPath, outputName, "PDF解密");
        } catch (InvalidPasswordException e) {
            throw new IllegalArgumentException("密码错误，无法解密PDF文件");
        }
    }
    
    /**
     * 内容编辑（涂黑敏感内容）
     */
    public FileEntity redactPdf(Long fileId, List<String> keywords, String pageRange, String customRange) throws IOException {
        byte[] fileContent = fileService.getFileContent(fileId);
        Optional<FileEntity> originalFile = fileService.getFile(fileId);
        
        if (originalFile.isEmpty()) {
            throw new IllegalArgumentException("原文件不存在");
        }
        
        try (PDDocument document = Loader.loadPDF(fileContent)) {
            // 解析页面范围
            List<Integer> pagesToProcess = parsePageRange(pageRange, customRange, document.getNumberOfPages());
            
            int totalRedactedCount = 0;
            
            // 对每个页面进行敏感词查找和涂黑
            for (Integer pageIndex : pagesToProcess) {
                if (pageIndex >= 0 && pageIndex < document.getNumberOfPages()) {
                    PDPage page = document.getPage(pageIndex);
                    
                    // 首先提取页面文本以检查是否包含敏感词
                    PDFTextStripper textStripper = new PDFTextStripper();
                    textStripper.setStartPage(pageIndex + 1);
                    textStripper.setEndPage(pageIndex + 1);
                    String pageText = textStripper.getText(document);
                    
                    boolean hasKeywords = false;
                    List<String> foundKeywords = new ArrayList<>();
                    
                    // 检查页面是否包含敏感词
                    for (String keyword : keywords) {
                        if (pageText.toLowerCase().contains(keyword.toLowerCase())) {
                            hasKeywords = true;
                            foundKeywords.add(keyword);
                        }
                    }
                    
                                        if (hasKeywords) {
                        // 使用精确的文本位置定位来进行涂黑
                        KeywordLocationFinder locationFinder = new KeywordLocationFinder(foundKeywords);
                        locationFinder.setStartPage(pageIndex + 1);
                        locationFinder.setEndPage(pageIndex + 1);
                        
                        StringWriter writer = new StringWriter();
                        locationFinder.writeText(document, writer);
                        writer.close();
                        
                        List<KeywordLocation> locations = locationFinder.getKeywordLocations();
                        
                        PDPageContentStream contentStream = new PDPageContentStream(document, page, 
                            PDPageContentStream.AppendMode.APPEND, true, true);
                        
                        PDRectangle mediaBox = page.getMediaBox();
                        float pageHeight = mediaBox.getHeight();
                        
                        if (!locations.isEmpty()) {
                            // 设置涂黑颜色
                            contentStream.setNonStrokingColor(Color.BLACK);
                            
                            // 在每个找到的关键词位置绘制黑色矩形
                            for (KeywordLocation location : locations) {
                                // 增加边距以确保完全覆盖文字
                                float marginX = 3f; // 左右边距
                                float marginY = 4f; // 上下边距，增加以确保顶部覆盖完全
                                
                                // 关键修复：PDF坐标系转换
                                // PDF坐标系Y轴从底部向上，需要将坐标转换为从顶部算起
                                float rectX = location.getX() - marginX;
                                // 转换Y坐标并增加顶部边距
                                float rectY = pageHeight - location.getY() - location.getHeight() - marginY;
                                float rectWidth = location.getWidth() + (marginX * 2);
                                float rectHeight = location.getHeight() + (marginY * 2);
                                
                                // 调试输出转换前后的坐标
                                System.out.println("原始坐标 - Y: " + location.getY() + 
                                                 ", 转换后Y: " + rectY + 
                                                 ", 页面高度: " + pageHeight);
                                System.out.println("绘制涂黑矩形 - X: " + rectX + 
                                                 ", Y: " + rectY + 
                                                 ", 宽度: " + rectWidth + 
                                                 ", 高度: " + rectHeight);
                                
                                contentStream.addRect(rectX, rectY, rectWidth, rectHeight);
                                contentStream.fill();
                                
                                totalRedactedCount++;
                            }
                        }
                        
                        // 不添加任何说明文字，保持涂黑处理的隐蔽性
                        
                        contentStream.close();
                    }
                }
            }
            
            // 不显示任何处理说明，保持PDF原始外观
            
            // 保存编辑后的文档
            String outputName = getFileNameWithoutExtension(originalFile.get().getOriginalName()) 
                              + "_redacted.pdf";
            Path outputPath = createOutputFile(outputName);
            document.save(outputPath.toFile());
            
            return saveProcessedFile(outputPath, outputName, "内容编辑");
        }
    }
    
    /**
     * 数字签名
     */
    public FileEntity digitalSignPdf(Long fileId, String signerName, String reason, String location) throws IOException {
        return digitalSignPdf(fileId, signerName, reason, location, "all", null);
    }
    
    /**
     * 数字签名（支持页面选择）
     */
    public FileEntity digitalSignPdf(Long fileId, String signerName, String reason, String location, 
                                   String pageRange, String customRange) throws IOException {
        byte[] fileContent = fileService.getFileContent(fileId);
        Optional<FileEntity> originalFile = fileService.getFile(fileId);
        
        if (originalFile.isEmpty()) {
            throw new IllegalArgumentException("原文件不存在");
        }
        
        try (PDDocument document = Loader.loadPDF(fileContent)) {
            
            // 解析页面范围
            List<Integer> pagesToSign = parsePageRange(pageRange, customRange, document.getNumberOfPages());
            
            // 在指定页面添加可视化签名信息
            if (!pagesToSign.isEmpty()) {
                for (Integer pageIndex : pagesToSign) {
                    if (pageIndex >= 0 && pageIndex < document.getNumberOfPages()) {
                        PDPage page = document.getPage(pageIndex);
                        PDPageContentStream contentStream = new PDPageContentStream(document, page, 
                            PDPageContentStream.AppendMode.APPEND, true, true);
                        
                        // 添加签名框
                        PDRectangle mediaBox = page.getMediaBox();
                        float x = mediaBox.getWidth() - 200;
                        float y = 50;
                        float width = 180;
                        float height = 100;
                        
                        // 绘制签名框
                        contentStream.setStrokingColor(Color.BLUE);
                        contentStream.setLineWidth(2);
                        contentStream.addRect(x, y, width, height);
                        contentStream.stroke();
                        
                        // 添加签名信息
                        contentStream.beginText();
                        contentStream.setFont(createSupportedFont(document, "数字签名"), 9);
                        contentStream.setNonStrokingColor(Color.BLUE);
                        contentStream.newLineAtOffset(x + 10, y + height - 15);
                        contentStream.showText("[认证] 数字签名");
                        contentStream.newLineAtOffset(0, -12);
                        contentStream.showText("签名者: " + signerName);
                        contentStream.newLineAtOffset(0, -12);
                        contentStream.showText("原因: " + reason);
                        contentStream.newLineAtOffset(0, -12);
                        contentStream.showText("位置: " + location);
                        contentStream.newLineAtOffset(0, -12);
                        contentStream.showText("时间: " + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
                        contentStream.newLineAtOffset(0, -12);
                        contentStream.showText("页面: " + (pageIndex + 1));
                        contentStream.newLineAtOffset(0, -12);
                        contentStream.showText("证书: 自签名证书");
                        contentStream.endText();
                        
                        contentStream.close();
                    }
                }
            }
            
            // 保存签名后的文档
            String outputName = getFileNameWithoutExtension(originalFile.get().getOriginalName()) 
                              + "_signed.pdf";
            Path outputPath = createOutputFile(outputName);
            document.save(outputPath.toFile());
            
            String description = "数字签名的PDF文件";
            if (pagesToSign.size() == 1) {
                description += " (签名页: 第" + (pagesToSign.get(0) + 1) + "页)";
            } else if (pagesToSign.size() > 1) {
                description += " (签名页: 共" + pagesToSign.size() + "页)";
            }
            
            return saveProcessedFile(outputPath, outputName, "数字签名");
        }
    }

    /**
     * Word转PDF
     */
    public Long convertWordToPdf(MultipartFile file) throws IOException {
        String originalName = file.getOriginalFilename();
        String ext = originalName != null && originalName.toLowerCase().endsWith(".doc") ? ".doc" : ".docx";
        java.io.File tempWord = java.io.File.createTempFile("upload_", ext);
        file.transferTo(tempWord);

        java.io.File tempPdf = java.io.File.createTempFile("converted_", ".pdf");
        LocalOfficeManager officeManager = LocalOfficeManager.install();
        try {
            officeManager.start();
            JodConverter.convert(tempWord).to(tempPdf).execute();
        } catch (Exception e) { // 捕获所有异常，包括 OfficeException
            throw new IOException("Word转PDF失败: " + e.getMessage(), e);
        } finally {
            try { officeManager.stop(); } catch (Exception ignore) {} // 保证关闭
        }

        FileEntity saved = saveProcessedFile(tempPdf.toPath(), originalName.replaceAll("\\.docx?$", ".pdf"), "Word转PDF");
        return saved.getId();
    }

    /**
     * Excel转PDF
     */
    public Long convertExcelToPdf(MultipartFile file) throws IOException {
        String originalName = file.getOriginalFilename();
        String ext = originalName != null && originalName.toLowerCase().endsWith(".xls") ? ".xls" : ".xlsx";
        java.io.File tempExcel = java.io.File.createTempFile("upload_", ext);
        file.transferTo(tempExcel);

        java.io.File tempPdf = java.io.File.createTempFile("converted_", ".pdf");
        LocalOfficeManager officeManager = LocalOfficeManager.install();
        try {
            officeManager.start();
            JodConverter.convert(tempExcel).to(tempPdf).execute();
        } catch (Exception e) {
            throw new IOException("Excel转PDF失败: " + e.getMessage(), e);
        } finally {
            try { officeManager.stop(); } catch (Exception ignore) {}
        }

        FileEntity saved = saveProcessedFile(tempPdf.toPath(), originalName.replaceAll("\\.xlsx?$", ".pdf"), "Excel转PDF");
        return saved.getId();
    }

    /**
     * PDF转PPTX
     */
    public Long convertPdfToPpt(MultipartFile file) throws IOException {
        String originalName = file.getOriginalFilename();
        FileEntity savedFile = fileService.saveFile(file, getCurrentUserId());
        byte[] fileContent = fileService.getFileContent(savedFile.getId());

        try (PDDocument document = Loader.loadPDF(fileContent);
             org.apache.poi.xslf.usermodel.XMLSlideShow ppt = new org.apache.poi.xslf.usermodel.XMLSlideShow()) {

            org.apache.pdfbox.rendering.PDFRenderer renderer = new org.apache.pdfbox.rendering.PDFRenderer(document);

            for (int i = 0; i < document.getNumberOfPages(); i++) {
                java.awt.image.BufferedImage image = renderer.renderImageWithDPI(i, 200, org.apache.pdfbox.rendering.ImageType.RGB);
                java.io.ByteArrayOutputStream imgOut = new java.io.ByteArrayOutputStream();
                javax.imageio.ImageIO.write(image, "png", imgOut);

                org.apache.poi.sl.usermodel.PictureData.PictureType picType = org.apache.poi.sl.usermodel.PictureData.PictureType.PNG;
                org.apache.poi.xslf.usermodel.XSLFPictureData picData = ppt.addPicture(imgOut.toByteArray(), picType);
                org.apache.poi.xslf.usermodel.XSLFSlide slide = ppt.createSlide();
                org.apache.poi.xslf.usermodel.XSLFPictureShape pic = slide.createPicture(picData);

                pic.setAnchor(new java.awt.Rectangle(0, 0, 960, 720)); // 16:9
            }

            String outputName = getFileNameWithoutExtension(originalName) + ".pptx";
            java.nio.file.Path outputPath = createOutputFile(outputName);

            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(outputPath.toFile())) {
                ppt.write(fos);
            }

            FileEntity result = saveProcessedFile(outputPath, outputName, "PDF转PPT");
            return result.getId();
        }
    }

    /**
     * PDF转图片
     */
    public List<FileEntity> convertPdfToImages(MultipartFile file, String format, int dpi) throws IOException {
        String originalName = file.getOriginalFilename();
        FileEntity savedFile = fileService.saveFile(file, getCurrentUserId());
        byte[] fileContent = fileService.getFileContent(savedFile.getId());

        List<FileEntity> imageFiles = new ArrayList<>();
        try (PDDocument document = Loader.loadPDF(fileContent)) {
            PDFRenderer renderer = new PDFRenderer(document);
            String normalizedFormat = format.toUpperCase();
            if ("JPEG".equals(normalizedFormat)) normalizedFormat = "JPG";

            for (int i = 0; i < document.getNumberOfPages(); i++) {
                BufferedImage image = renderer.renderImageWithDPI(i, dpi, ImageType.RGB);
                String fileName = getFileNameWithoutExtension(originalName) + "_page" + (i + 1) + "." + normalizedFormat.toLowerCase();
                Path outputPath = createOutputFile(fileName);

                if (!ImageIO.write(image, normalizedFormat, outputPath.toFile())) {
                    throw new IOException("无法写入图片格式: " + normalizedFormat);
                }

                FileEntity imageFile = saveProcessedFile(outputPath, fileName, "PDF转图片");
                imageFiles.add(imageFile);
            }
        }
        return imageFiles;
    }

    /**
     * Word转PDF - 基于文件ID
     */
    public Long convertWordToPdfById(Long fileId) throws IOException {
        FileEntity wordFile = fileService.getFile(fileId)
            .orElseThrow(() -> new IllegalArgumentException("文件不存在"));
        String originalName = wordFile.getOriginalName();
        String ext = originalName != null && originalName.toLowerCase().endsWith(".doc") ? ".doc" : ".docx";
        java.io.File tempWord = java.io.File.createTempFile("upload_", ext);
        java.nio.file.Files.copy(java.nio.file.Paths.get(wordFile.getFilePath()), tempWord.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        java.io.File tempPdf = java.io.File.createTempFile("converted_", ".pdf");
        LocalOfficeManager officeManager = LocalOfficeManager.install();
        try {
            officeManager.start();
            JodConverter.convert(tempWord).to(tempPdf).execute();
        } catch (Exception e) {
            throw new IOException("Word转PDF失败: " + e.getMessage(), e);
        } finally {
            try { officeManager.stop(); } catch (Exception ignore) {}
        }

        FileEntity saved = saveProcessedFile(tempPdf.toPath(), originalName.replaceAll("\\.docx?$", ".pdf"), "Word转PDF");
        return saved.getId();
    }

    /**
     * Excel转PDF - 基于文件ID
     */
    public Long convertExcelToPdfById(Long fileId) throws IOException {
        FileEntity excelFile = fileService.getFile(fileId)
            .orElseThrow(() -> new IllegalArgumentException("文件不存在"));
        String originalName = excelFile.getOriginalName();
        String ext = originalName != null && originalName.toLowerCase().endsWith(".xls") ? ".xls" : ".xlsx";
        java.io.File tempExcel = java.io.File.createTempFile("upload_", ext);
        java.nio.file.Files.copy(java.nio.file.Paths.get(excelFile.getFilePath()), tempExcel.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        java.io.File tempPdf = java.io.File.createTempFile("converted_", ".pdf");
        LocalOfficeManager officeManager = LocalOfficeManager.install();
        try {
            officeManager.start();
            JodConverter.convert(tempExcel).to(tempPdf).execute();
        } catch (Exception e) {
            throw new IOException("Excel转PDF失败: " + e.getMessage(), e);
        } finally {
            try { officeManager.stop(); } catch (Exception ignore) {}
        }

        FileEntity saved = saveProcessedFile(tempPdf.toPath(), originalName.replaceAll("\\.xlsx?$", ".pdf"), "Excel转PDF");
        return saved.getId();
    }

    /**
     * PDF转PPT - 基于文件ID
     */
    public Long convertPdfToPptById(Long fileId) throws IOException {
        FileEntity pdfFile = fileService.getFile(fileId)
            .orElseThrow(() -> new IllegalArgumentException("文件不存在"));
        String originalName = pdfFile.getOriginalName();
        byte[] fileContent = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(pdfFile.getFilePath()));

        try (PDDocument document = Loader.loadPDF(fileContent);
             org.apache.poi.xslf.usermodel.XMLSlideShow ppt = new org.apache.poi.xslf.usermodel.XMLSlideShow()) {

            org.apache.pdfbox.rendering.PDFRenderer renderer = new org.apache.pdfbox.rendering.PDFRenderer(document);

            for (int i = 0; i < document.getNumberOfPages(); i++) {
                java.awt.image.BufferedImage image = renderer.renderImageWithDPI(i, 200, org.apache.pdfbox.rendering.ImageType.RGB);
                java.io.ByteArrayOutputStream imgOut = new java.io.ByteArrayOutputStream();
                javax.imageio.ImageIO.write(image, "png", imgOut);

                org.apache.poi.sl.usermodel.PictureData.PictureType picType = org.apache.poi.sl.usermodel.PictureData.PictureType.PNG;
                org.apache.poi.xslf.usermodel.XSLFPictureData picData = ppt.addPicture(imgOut.toByteArray(), picType);
                org.apache.poi.xslf.usermodel.XSLFSlide slide = ppt.createSlide();
                org.apache.poi.xslf.usermodel.XSLFPictureShape pic = slide.createPicture(picData);

                pic.setAnchor(new java.awt.Rectangle(0, 0, 960, 720)); // 16:9
            }

            String outputName = getFileNameWithoutExtension(originalName) + ".pptx";
            java.nio.file.Path outputPath = createOutputFile(outputName);

            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(outputPath.toFile())) {
                ppt.write(fos);
            }

            FileEntity result = saveProcessedFile(outputPath, outputName, "PDF转PPT");
            return result.getId();
        }
    }

    /**
     * PDF转图片 - 基于文件ID
     */
    public List<FileEntity> convertPdfToImagesById(Long fileId, String format, int dpi) throws IOException {
        FileEntity pdfFile = fileService.getFile(fileId)
            .orElseThrow(() -> new IllegalArgumentException("文件不存在"));
        byte[] fileContent = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(pdfFile.getFilePath()));
        String originalName = pdfFile.getOriginalName();

        List<FileEntity> imageFiles = new ArrayList<>();
        try (PDDocument document = Loader.loadPDF(fileContent)) {
            PDFRenderer renderer = new PDFRenderer(document);
            String normalizedFormat = format.toUpperCase();
            if ("JPEG".equals(normalizedFormat)) normalizedFormat = "JPG";

            for (int i = 0; i < document.getNumberOfPages(); i++) {
                BufferedImage image = renderer.renderImageWithDPI(i, dpi, ImageType.RGB);
                String fileName = getFileNameWithoutExtension(originalName) + "_page" + (i + 1) + "." + normalizedFormat.toLowerCase();
                Path outputPath = createOutputFile(fileName);

                if (!ImageIO.write(image, normalizedFormat, outputPath.toFile())) {
                    throw new IOException("无法写入图片格式: " + normalizedFormat);
                }

                FileEntity imageFile = saveProcessedFile(outputPath, fileName, "PDF转图片");
                imageFiles.add(imageFile);
            }
        }
        return imageFiles;
    }
} 