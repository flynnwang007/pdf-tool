# PDF文字提取功能说明

## 📋 功能概览

本项目提供了完整的PDF文字提取功能，支持多种调用方式和灵活的页面范围选择。

## 🚀 核心接口

### 1. 基于文件上传的文字提取
```
POST /api/pdf-tools/extract-text
Content-Type: multipart/form-data
```

**参数：**
- `file`: PDF文件（multipart/form-data）

**返回：**
```json
{
  "success": true,
  "message": "PDF文本提取成功",
  "extractedText": "提取的完整文本内容...",
  "textLength": 1234,
  "algorithm": "PDFBox文本提取器"
}
```

### 2. 基于文件ID的文字提取
```
POST /api/pdf-tools/extract-text/{fileId}
```

**参数：**
- `fileId`: 文件ID（路径参数）

**返回：**同上

### 3. 支持页面范围的文字提取
```
POST /api/pdf-tools/extract-text-range
Content-Type: application/json
```

**参数：**
```json
{
  "fileId": 123,
  "pageRange": "custom",     // "all" 或 "custom"
  "customRange": "1-5"       // 页面范围
}
```

**页面范围格式：**
- `"1"` - 第1页
- `"1-5"` - 第1到5页
- `"1,3,5"` - 第1,3,5页
- `"1-3,5,7-9"` - 第1-3页、第5页、第7-9页

**返回：**
```json
{
  "success": true,
  "message": "PDF文本提取成功",
  "extractedText": "提取的文本内容...",
  "textLength": 1234,
  "algorithm": "PDFBox文本提取器",
  "pageRange": "custom"
}
```

## 💻 前端调用示例

### JavaScript/TypeScript
```javascript
class PdfTextExtractor {
  constructor(baseUrl = '') {
    this.baseUrl = baseUrl;
  }
  
  // 方法1：直接上传并提取
  async extractFromFile(file) {
    const formData = new FormData();
    formData.append('file', file);
    
    const response = await fetch(`${this.baseUrl}/api/pdf-tools/extract-text`, {
      method: 'POST',
      body: formData
    });
    
    return await response.json();
  }
  
  // 方法2：基于文件ID提取
  async extractById(fileId) {
    const response = await fetch(`${this.baseUrl}/api/pdf-tools/extract-text/${fileId}`, {
      method: 'POST'
    });
    
    return await response.json();
  }
  
  // 方法3：提取指定页面
  async extractByRange(fileId, pageRange = 'all', customRange = '') {
    const data = { fileId, pageRange };
    if (pageRange === 'custom') data.customRange = customRange;
    
    const response = await fetch(`${this.baseUrl}/api/pdf-tools/extract-text-range`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    });
    
    return await response.json();
  }
}

// 使用示例
const extractor = new PdfTextExtractor();

// 直接提取
const result1 = await extractor.extractFromFile(file);

// 基于ID提取
const result2 = await extractor.extractById(123);

// 提取前3页
const result3 = await extractor.extractByRange(123, 'custom', '1-3');
```

### Vue.js 组件示例
```vue
<template>
  <div class="pdf-text-extractor">
    <input type="file" @change="onFileSelect" accept=".pdf" />
    <button @click="extractText" :disabled="!file || loading">
      {{ loading ? '提取中...' : '提取文字' }}
    </button>
    
    <div v-if="result" class="result">
      <h3>提取结果</h3>
      <p>文字长度: {{ result.textLength }}</p>
      <textarea v-model="result.extractedText" rows="10" readonly></textarea>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      file: null,
      result: null,
      loading: false
    }
  },
  methods: {
    onFileSelect(event) {
      this.file = event.target.files[0];
    },
    
    async extractText() {
      if (!this.file) return;
      
      this.loading = true;
      try {
        const formData = new FormData();
        formData.append('file', this.file);
        
        const response = await fetch('/api/pdf-tools/extract-text', {
          method: 'POST',
          body: formData
        });
        
        this.result = await response.json();
      } catch (error) {
        console.error('提取失败:', error);
      } finally {
        this.loading = false;
      }
    }
  }
}
</script>
```

### Python 调用示例
```python
import requests

class PdfTextExtractor:
    def __init__(self, base_url='http://localhost:8080'):
        self.base_url = base_url
    
    def extract_from_file(self, file_path):
        """从PDF文件提取文字"""
        with open(file_path, 'rb') as file:
            files = {'file': file}
            response = requests.post(f'{self.base_url}/api/pdf-tools/extract-text', files=files)
        
        result = response.json()
        if result['success']:
            return result['extractedText']
        else:
            raise Exception(result['message'])
    
    def extract_by_id(self, file_id):
        """根据文件ID提取文字"""
        response = requests.post(f'{self.base_url}/api/pdf-tools/extract-text/{file_id}')
        
        result = response.json()
        if result['success']:
            return result['extractedText']
        else:
            raise Exception(result['message'])
    
    def extract_by_range(self, file_id, page_range='all', custom_range=''):
        """根据页面范围提取文字"""
        data = {'fileId': file_id, 'pageRange': page_range}
        if page_range == 'custom':
            data['customRange'] = custom_range
        
        response = requests.post(f'{self.base_url}/api/pdf-tools/extract-text-range', json=data)
        
        result = response.json()
        if result['success']:
            return result['extractedText']
        else:
            raise Exception(result['message'])

# 使用示例
extractor = PdfTextExtractor()

# 从文件提取
text = extractor.extract_from_file('/path/to/document.pdf')

# 从文件ID提取
text = extractor.extract_by_id(123)

# 提取前5页
text = extractor.extract_by_range(123, 'custom', '1-5')
```

## 🔧 技术实现

### 核心技术栈
- **PDF处理**: Apache PDFBox 3.0.5
- **文本提取**: PDFTextStripper
- **后端框架**: Spring Boot
- **文件管理**: 自定义FileService

### 实现特点
1. **高效处理**: 基于内存的PDF文档处理
2. **页面范围**: 支持灵活的页面选择
3. **错误处理**: 完善的异常处理机制
4. **资源管理**: 自动关闭文档资源
5. **编码支持**: 支持中文和多语言文本

## 🎯 使用建议

### 适用场景
- ✅ 文本搜索和分析
- ✅ 关键词提取
- ✅ 内容摘要生成
- ✅ 文档索引构建
- ✅ API集成开发

### 性能建议
1. **大文件处理**: 建议使用页面范围提取，避免一次性处理过大文档
2. **批量处理**: 使用文件ID方式，避免重复上传
3. **内存管理**: 处理完成后及时释放大文本变量

### 错误处理
```javascript
try {
  const result = await extractor.extractFromFile(file);
  if (result.success) {
    console.log('提取成功:', result.extractedText);
  } else {
    console.error('提取失败:', result.message);
  }
} catch (error) {
  console.error('网络错误:', error);
}
```

## 🔗 相关功能

本项目还提供其他PDF处理功能：
- PDF转Word/Excel/CSV
- OCR文字识别（用于扫描版PDF）
- PDF分析和信息提取
- PDF合并、分割、压缩
- 水印添加、页面操作等

## 📞 技术支持

如有问题或建议，请联系开发团队或提交Issue。 