# PDF转换算法实现说明

## 概述

本项目实现了完整的PDF转换工具，参照Stirling-PDF的实现策略，支持多种PDF处理和转换功能。

## 实现的核心算法

### 1. PDF转Word文档 (`convertPdfToWord`)

**算法策略**: PDFBox文本提取 + Apache POI Word文档生成

**实现细节**:
- 使用`PDFTextStripper`按页提取PDF中的文本内容
- 利用Apache POI的`XWPFDocument`创建Word文档
- 保持页面结构，在页面间插入分页符
- 输出格式：DOCX

**技术栈**:
- PDFBox 3.0.5 (文本提取)
- Apache POI 5.2.5 (Word文档生成)

### 2. PDF转Excel表格 (`convertPdfToExcel`)

**算法策略**: Tabula表格识别 + Apache POI Excel生成

**实现细节**:
- 使用Tabula的`SpreadsheetExtractionAlgorithm`进行智能表格检测
- 当电子表格算法无法识别时，回退使用`BasicExtractionAlgorithm`
- 为每个检测到的表格创建独立的Excel工作表
- 自动调整列宽，智能识别数字和文本类型
- 如未检测到表格，创建提示工作表

**技术栈**:
- Tabula 1.0.5 (表格识别)
- Apache POI 5.2.5 (Excel生成)

### 3. PDF转CSV (`convertPdfToCsv`)

**算法策略**: Tabula表格识别 + OpenCSV数据导出

**实现细节**:
- 同样使用双算法策略(电子表格算法 + 基础算法)
- 使用OpenCSV写入器生成标准CSV格式
- 支持多表格标识和分隔
- UTF-8编码确保中文字符正确显示

**技术栈**:
- Tabula 1.0.5 (表格识别)
- OpenCSV 5.11 (CSV生成)

### 4. OCR文字识别 (`performOcr`)

**算法策略**: Tesseract OCR引擎

**实现细节**:
- 支持PDF和图片文件OCR识别
- PDF文件先使用PDFRenderer转换为300 DPI图片
- 支持多语言识别：英语、简繁中文、日韩语、法德西语等
- 按页面分段输出识别结果

**技术栈**:
- Tesseract OCR 5.8.0 (文字识别引擎)
- PDFBox PDFRenderer (PDF转图片)

**支持的语言**:
- `eng` - 英语
- `chi_sim` - 简体中文
- `chi_tra` - 繁体中文
- `jpn` - 日语
- `kor` - 韩语
- `fra` - 法语
- `deu` - 德语
- `spa` - 西班牙语

### 5. PDF信息分析 (`getPdfInfo`)

**算法策略**: 综合分析引擎

**实现细节**:
- 提取PDF基本元数据（标题、作者、创建日期等）
- 使用Tabula进行表格检测和统计
- PDFTextStripper进行文本内容分析
- 页面尺寸和方向检测
- 字数统计（支持中英文混合）

**输出信息**:
- 页面数量、文件大小
- 文档元数据（标题、作者、创建者等）
- 表格检测结果（是否包含表格、表格数量）
- 文本分析（文本长度、行数、估算字数）
- 页面信息（宽高、方向）

## 依赖库版本

### 核心PDF处理
- Apache PDFBox: 3.0.5
- PDFBox Tools: 3.0.5

### 文档转换
- Apache POI: 5.2.5 (Office文档处理)
- OpenCSV: 5.11 (CSV处理)

### 表格识别
- Tabula: 1.0.5 (表格提取算法)

### OCR识别
- Tess4J: 5.8.0 (Tesseract Java包装)

### 图像处理
- TwelveMonkeys ImageIO: 3.12.0 (增强图像格式支持)

## API端点

```http
POST /api/pdf-tools/to-word      # PDF转Word
POST /api/pdf-tools/to-excel     # PDF转Excel
POST /api/pdf-tools/to-csv       # PDF转CSV
POST /api/pdf-tools/ocr          # OCR识别
POST /api/pdf-tools/analyze      # PDF分析
GET  /api/pdf-tools/features     # 获取功能列表
```

## 错误处理

所有方法都包含完善的错误处理机制：

1. **输入验证**: 检查文件类型和大小
2. **算法回退**: 表格识别支持多算法策略
3. **资源管理**: 自动关闭文档和流
4. **异常包装**: 统一的异常处理和错误消息

## 性能优化

1. **内存管理**: 及时关闭PDDocument和流资源
2. **算法选择**: 智能选择最适合的表格提取算法
3. **批处理支持**: 支持多页面批量处理
4. **文件流**: 使用流式处理避免大文件内存溢出

## 使用示例

### 前端调用示例

```javascript
// PDF转Word
const wordResult = await pdfApi.convertPdfToWord(file)

// PDF转Excel
const excelResult = await pdfApi.convertPdfToExcel(file)

// OCR识别
const ocrResult = await pdfApi.performOcr(file, 'chi_sim')

// PDF分析
const analysisResult = await pdfApi.analyzePdf(file)
```

## 部署注意事项

1. **Tesseract安装**: 生产环境需要安装Tesseract OCR引擎
2. **语言数据**: 需要下载对应语言的tessdata文件
3. **内存配置**: 建议JVM堆内存至少512MB
4. **文件权限**: 确保临时文件目录的读写权限

## 未来扩展

1. **更多格式支持**: PowerPoint、纯文本等
2. **高级表格识别**: 复杂表格结构处理
3. **图像优化**: OCR前的图像预处理
4. **并行处理**: 多线程处理大文件
5. **AI增强**: 集成AI模型提高识别准确度

## 参考项目

本实现参照了开源项目Stirling-PDF的算法策略和最佳实践，在此基础上进行了优化和本地化改进。 