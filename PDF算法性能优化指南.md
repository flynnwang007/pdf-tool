# PDF算法性能优化指南

## 算法性能基准

### 1. PDF转Word算法性能

**基准测试环境**:
- 测试文件: 10页标准PDF文档 (~2MB)
- 平均处理时间: 2-5秒
- 内存占用: 50-100MB

**性能特点**:
- ✅ 纯文本PDF处理速度快
- ⚠️ 复杂布局PDF可能丢失格式
- ⚠️ 图片和表格转换效果有限

**优化建议**:
```java
// 1. 批量处理优化
PDFTextStripper textStripper = new PDFTextStripper();
textStripper.setSortByPosition(true); // 保持文本位置顺序

// 2. 内存优化
try (PDDocument document = Loader.loadPDF(fileBytes)) {
    // 及时关闭资源
}
```

### 2. PDF转Excel算法性能

**基准测试环境**:
- 测试文件: 包含5个表格的PDF (~1MB)
- 平均处理时间: 3-8秒
- 内存占用: 80-150MB

**算法性能对比**:
```
SpreadsheetExtractionAlgorithm (电子表格算法):
- 准确度: 85-95%
- 速度: 快
- 适用: 规整表格

BasicExtractionAlgorithm (基础算法):
- 准确度: 70-85%
- 速度: 中等
- 适用: 复杂布局表格
```

**性能优化策略**:
```java
// 1. 智能算法选择
SpreadsheetExtractionAlgorithm primaryAlgorithm = new SpreadsheetExtractionAlgorithm();
List<Table> tables = primaryAlgorithm.extract(page);

if (tables.isEmpty()) {
    // 回退到基础算法
    BasicExtractionAlgorithm fallbackAlgorithm = new BasicExtractionAlgorithm();
    tables = fallbackAlgorithm.extract(page);
}

// 2. 表格质量评估
for (Table table : tables) {
    if (table.getRows().size() < 2) {
        // 跳过可能的误检测
        continue;
    }
}
```

### 3. OCR算法性能

**基准测试环境**:
- 测试文件: 10页扫描PDF (~5MB)
- 300 DPI渲染
- 平均处理时间: 15-30秒
- 内存占用: 200-400MB

**语言识别性能**:
```
英语 (eng):        准确度: 95%+, 速度: 最快
简体中文 (chi_sim): 准确度: 90%+, 速度: 中等
繁体中文 (chi_tra): 准确度: 88%+, 速度: 中等
日语 (jpn):        准确度: 85%+, 速度: 较慢
韩语 (kor):        准确度: 83%+, 速度: 较慢
```

**OCR性能优化**:
```java
// 1. 图像预处理优化
BufferedImage image = renderer.renderImageWithDPI(pageIndex, 300, ImageType.RGB);

// 2. 批量处理优化
Tesseract tesseract = new Tesseract();
tesseract.setLanguage(language);
// 复用tesseract实例，避免重复初始化

// 3. 并行处理（注意线程安全）
List<CompletableFuture<String>> futures = new ArrayList<>();
for (int i = 0; i < pageCount; i++) {
    final int pageIndex = i;
    futures.add(CompletableFuture.supplyAsync(() -> {
        // OCR处理逻辑
    }));
}
```

## 内存优化策略

### 1. 大文件处理优化

```java
// 避免一次性加载整个文件到内存
public Long processBigPdf(MultipartFile file) throws IOException {
    // 分块处理策略
    try (InputStream inputStream = file.getInputStream();
         BufferedInputStream bufferedInput = new BufferedInputStream(inputStream)) {
        
        // 使用流式处理
        byte[] buffer = new byte[8192];
        // 处理逻辑
    }
}
```

### 2. 资源管理最佳实践

```java
// 1. 使用try-with-resources
try (PDDocument document = Loader.loadPDF(fileBytes);
     XSSFWorkbook workbook = new XSSFWorkbook();
     FileOutputStream fos = new FileOutputStream(outputPath.toFile())) {
    
    // 处理逻辑
    
} catch (Exception e) {
    // 异常处理
}

// 2. 及时清理临时资源
ObjectExtractor extractor = new ObjectExtractor(document);
try {
    // 使用extractor
} finally {
    extractor.close(); // 确保资源释放
}
```

### 3. JVM调优建议

```bash
# 生产环境JVM参数建议
java -Xms512m -Xmx2g \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -XX:+PrintGCDetails \
     -jar pdf-tool.jar
```

## 并发处理优化

### 1. 异步处理架构

```java
@Service
public class AsyncPdfService {
    
    @Async
    public CompletableFuture<Long> convertPdfToWordAsync(MultipartFile file) {
        try {
            Long result = convertPdfToWord(file);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}
```

### 2. 线程池配置

```java
@Configuration
@EnableAsync
public class AsyncConfig {
    
    @Bean(name = "pdfProcessorExecutor")
    public Executor pdfProcessorExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("pdf-processor-");
        executor.initialize();
        return executor;
    }
}
```

## 错误处理与重试机制

### 1. 智能重试策略

```java
@Retryable(value = {IOException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
public Long convertPdfWithRetry(MultipartFile file) throws IOException {
    try {
        return convertPdfToWord(file);
    } catch (IOException e) {
        log.warn("PDF转换失败，尝试重试: {}", e.getMessage());
        throw e;
    }
}

@Recover
public Long recoverFromFailure(IOException e, MultipartFile file) {
    log.error("PDF转换最终失败: {}", e.getMessage());
    // 返回默认值或抛出业务异常
    throw new BusinessException("PDF转换失败，请检查文件格式");
}
```

### 2. 超时控制

```java
@Transactional(timeout = 60) // 60秒超时
public Long convertPdfWithTimeout(MultipartFile file) throws IOException {
    return convertPdfToWord(file);
}
```

## 缓存优化策略

### 1. 结果缓存

```java
@Cacheable(value = "pdfConversion", key = "#file.originalFilename + '_' + #file.size")
public Long convertPdfToCachedWord(MultipartFile file) throws IOException {
    return convertPdfToWord(file);
}
```

### 2. Tesseract实例缓存

```java
@Component
public class TesseractCache {
    private final Map<String, Tesseract> tesseractInstances = new ConcurrentHashMap<>();
    
    public Tesseract getTesseract(String language) {
        return tesseractInstances.computeIfAbsent(language, lang -> {
            Tesseract tesseract = new Tesseract();
            tesseract.setLanguage(lang);
            return tesseract;
        });
    }
}
```

## 监控与性能指标

### 1. 性能监控配置

```java
@Component
public class PdfProcessingMetrics {
    
    private final Timer conversionTimer;
    private final Counter successCounter;
    private final Counter errorCounter;
    
    public PdfProcessingMetrics(MeterRegistry meterRegistry) {
        this.conversionTimer = Timer.builder("pdf.conversion.duration")
                                   .register(meterRegistry);
        this.successCounter = Counter.builder("pdf.conversion.success")
                                    .register(meterRegistry);
        this.errorCounter = Counter.builder("pdf.conversion.error")
                                  .register(meterRegistry);
    }
    
    public void recordConversion(Duration duration, boolean success) {
        conversionTimer.record(duration);
        if (success) {
            successCounter.increment();
        } else {
            errorCounter.increment();
        }
    }
}
```

### 2. 关键性能指标

```yaml
# 监控指标
performance.metrics:
  - name: pdf_conversion_duration
    type: histogram
    description: PDF转换耗时
    
  - name: pdf_conversion_memory_usage
    type: gauge
    description: PDF转换内存使用量
    
  - name: pdf_conversion_success_rate
    type: ratio
    description: PDF转换成功率
```

## 部署优化建议

### 1. 生产环境配置

```yaml
# application-prod.yml
spring:
  servlet:
    multipart:
      max-file-size: 50MB
      max-request-size: 50MB
      
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
        
logging:
  level:
    com.yourcompany.pdfapp: INFO
  pattern:
    file: "%d{ISO8601} [%thread] %-5level %logger{36} - %msg%n"
```

### 2. 容器化部署

```dockerfile
FROM openjdk:17-jdk-slim

# 安装Tesseract OCR
RUN apt-get update && \
    apt-get install -y tesseract-ocr tesseract-ocr-chi-sim tesseract-ocr-chi-tra && \
    rm -rf /var/lib/apt/lists/*

# 应用配置
COPY target/pdf-tool.jar app.jar
EXPOSE 8080

# JVM优化参数
ENV JAVA_OPTS="-Xms512m -Xmx2g -XX:+UseG1GC"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app.jar"]
```

## 常见性能问题与解决方案

### 1. 内存溢出 (OutOfMemoryError)

**原因**: 处理大文件时内存不足
**解决方案**:
- 增加JVM堆内存 `-Xmx2g`
- 实现流式处理
- 添加文件大小限制

### 2. 处理速度慢

**原因**: CPU密集型操作
**解决方案**:
- 启用并行处理
- 优化算法选择策略
- 添加结果缓存

### 3. OCR准确度低

**原因**: 图像质量差或语言模型不匹配
**解决方案**:
- 提高渲染DPI (300-600)
- 图像预处理优化
- 选择合适的语言模型

### 4. 表格识别失败

**原因**: 复杂表格布局
**解决方案**:
- 使用双算法策略
- 添加表格质量评估
- 提供手动调整选项

## 测试建议

### 1. 性能测试用例

```java
@Test
public void testPdfConversionPerformance() {
    // 小文件测试 (< 1MB)
    // 中等文件测试 (1-10MB)
    // 大文件测试 (> 10MB)
    
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    
    Long result = pdfService.convertPdfToWord(testFile);
    
    stopWatch.stop();
    
    assertThat(stopWatch.getTotalTimeMillis()).isLessThan(5000); // 5秒内完成
    assertThat(result).isNotNull();
}
```

### 2. 并发测试

```java
@Test
public void testConcurrentConversion() {
    int threadCount = 10;
    CountDownLatch latch = new CountDownLatch(threadCount);
    
    for (int i = 0; i < threadCount; i++) {
        executor.submit(() -> {
            try {
                pdfService.convertPdfToWord(testFile);
            } finally {
                latch.countDown();
            }
        });
    }
    
    assertThat(latch.await(30, TimeUnit.SECONDS)).isTrue();
}
```

通过以上优化策略和监控机制，可以显著提升PDF转换算法的性能和稳定性。 