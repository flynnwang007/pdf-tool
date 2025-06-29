<template>
  <div class="pdf-converter">
    <div class="converter-header">
      <h2>PDF转换工具</h2>
      <p class="subtitle">参照Stirling-PDF实现策略，支持多种格式转换</p>
    </div>

    <!-- 功能选择 -->
    <div class="function-selector">
      <div class="function-tabs">
        <button 
          v-for="tab in functionTabs" 
          :key="tab.key"
          :class="['tab-button', { active: activeTab === tab.key }]"
          @click="activeTab = tab.key"
        >
          <i :class="tab.icon"></i>
          {{ tab.label }}
        </button>
      </div>
    </div>

    <!-- 文件选择方式 -->
    <div class="file-source-selector">
      <div class="source-tabs">
        <button 
          :class="['source-tab', { active: fileSource === 'upload' }]"
          @click="fileSource = 'upload'"
        >
          <i class="fas fa-upload"></i>
          上传新文件
        </button>
        <button 
          :class="['source-tab', { active: fileSource === 'existing' }]"
          @click="fileSource = 'existing'; loadExistingFiles()"
        >
          <i class="fas fa-folder"></i>
          选择已有文件
        </button>
      </div>
    </div>

    <!-- 文件上传区域 (上传新文件) -->
    <div v-if="fileSource === 'upload'" class="upload-section">
      <div 
        class="upload-area"
        :class="{ 'drag-over': isDragOver }"
        @drop="handleDrop"
        @dragover.prevent="isDragOver = true"
        @dragleave="isDragOver = false"
      >
        <!-- Direct Inputs for Debugging -->
        <p><strong>调试模式：请直接点击下面的按钮</strong></p>
        <div style="padding: 20px; display: flex; flex-direction: column; gap: 10px;">
          <label>PDF转其他: <input type="file" accept="application/pdf" @change="handleFileSelect"></label>
          <label>图片转PDF: <input type="file" accept="image/*" @change="handleFileSelect"></label>
          <label>OCR: <input type="file" accept="application/pdf,image/*" @change="handleFileSelect"></label>
        </div>
        
        <div class="upload-content" style="display: none;">
          <i class="fas fa-cloud-upload-alt upload-icon"></i>
          <p class="upload-text">
            {{ getUploadText() }}
          </p>
          <p class="upload-hint">
            支持的格式: {{ getSupportedFormats() }}
          </p>
        </div>
      </div>
    </div>

    <!-- 已有文件选择区域 -->
    <div v-if="fileSource === 'existing'" class="existing-files-section">
      <div class="files-header">
        <h3>选择已上传的文件</h3>
        <button class="refresh-btn" @click="loadExistingFiles" :disabled="loadingFiles">
          <i class="fas fa-sync" :class="{ 'fa-spin': loadingFiles }"></i>
          刷新
        </button>
      </div>
      
      <div v-if="loadingFiles" class="loading-files">
        <i class="fas fa-spinner fa-spin"></i>
        加载中...
      </div>
      
      <div v-else-if="existingFiles.length === 0" class="no-files">
        <i class="fas fa-folder-open"></i>
        <p>暂无已上传的文件</p>
        <button class="upload-first-btn" @click="fileSource = 'upload'">
          去上传文件
        </button>
      </div>
      
      <div v-else class="files-grid">
        <div 
          v-for="file in existingFiles"
          :key="file.id"
          :class="['file-item', { selected: selectedFileId === file.id }]"
          @click="selectExistingFile(file)"
        >
          <div class="file-icon">
            <i :class="getFileIcon(file.fileType)"></i>
          </div>
          <div class="file-info">
            <div class="file-name" :title="file.originalName">{{ file.originalName }}</div>
            <div class="file-meta">
              <span class="file-size">{{ formatFileSize(file.fileSize) }}</span>
              <span class="file-date">{{ formatDate(file.createdAt) }}</span>
            </div>
          </div>
          <div v-if="selectedFileId === file.id" class="selected-indicator">
            <i class="fas fa-check-circle"></i>
          </div>
        </div>
      </div>
    </div>

    <!-- 选中的文件 -->
    <div v-if="selectedFile || selectedFileId" class="selected-file">
      <div class="file-info">
        <i class="fas fa-file-pdf file-icon"></i>
        <div class="file-details">
          <span class="file-name">{{ getSelectedFileName() }}</span>
          <span class="file-size">{{ getSelectedFileSize() }}</span>
        </div>
        <button class="remove-file" @click="removeFile">
          <i class="fas fa-times"></i>
        </button>
      </div>
    </div>

    <!-- 转换选项 -->
    <div v-if="selectedFile || selectedFileId" class="conversion-options">
      <!-- PDF转Word选项 -->
      <div v-if="activeTab === 'toWord'" class="option-group">
        <h3>PDF转Word设置</h3>
        <div class="option-item">
          <label>转换算法:</label>
          <span class="algorithm-info">文本提取 + Apache POI</span>
        </div>
        <div class="option-item">
          <label>输出格式:</label>
          <select v-model="wordOptions.format">
            <option value="docx">DOCX (推荐)</option>
            <option value="doc">DOC</option>
          </select>
        </div>
      </div>

      <!-- PDF转Excel选项 -->
      <div v-if="activeTab === 'toExcel'" class="option-group">
        <h3>PDF转Excel设置</h3>
        <div class="option-item">
          <label>转换算法:</label>
          <span class="algorithm-info">Tabula表格识别 + Apache POI</span>
        </div>
        <div class="option-item">
          <label>表格检测:</label>
          <select v-model="excelOptions.algorithm">
            <option value="auto">自动检测</option>
            <option value="spreadsheet">电子表格算法</option>
            <option value="basic">基础算法</option>
          </select>
        </div>
      </div>

      <!-- OCR选项 -->
      <div v-if="activeTab === 'ocr'" class="option-group">
        <h3>OCR识别设置</h3>
        <div class="option-item">
          <label>识别语言:</label>
          <select v-model="ocrOptions.language">
            <option value="eng">英语</option>
            <option value="chi_sim">简体中文</option>
            <option value="chi_tra">繁体中文</option>
            <option value="jpn">日语</option>
            <option value="kor">韩语</option>
            <option value="fra">法语</option>
            <option value="deu">德语</option>
            <option value="spa">西班牙语</option>
          </select>
        </div>
        <div class="option-item">
          <label>OCR引擎:</label>
          <span class="algorithm-info">Tesseract OCR 5.8.0</span>
        </div>
      </div>

      <!-- PDF分析选项 -->
      <div v-if="activeTab === 'analyze'" class="option-group">
        <h3>PDF分析设置</h3>
        <div class="option-item">
          <label>分析算法:</label>
          <span class="algorithm-info">PDFBox + Tabula表格检测</span>
        </div>
      </div>

      <!-- Word转PDF选项 -->
      <div v-if="activeTab === 'wordToPdf'" class="option-group">
        <WordToPdf />
      </div>
    </div>

    <!-- 转换按钮 -->
    <div v-if="selectedFile || selectedFileId" class="action-section">
      <button 
        class="convert-button"
        :disabled="isProcessing"
        @click="startConversion"
      >
        <i v-if="isProcessing" class="fas fa-spinner fa-spin"></i>
        <i v-else :class="getCurrentFunctionIcon()"></i>
        {{ isProcessing ? '处理中...' : getCurrentFunctionLabel() }}
      </button>
    </div>

    <!-- 处理进度 -->
    <div v-if="isProcessing" class="progress-section">
      <div class="progress-bar">
        <div class="progress-fill" :style="{ width: progress + '%' }"></div>
      </div>
      <p class="progress-text">{{ progressText }}</p>
    </div>

    <!-- 结果展示 -->
    <div v-if="conversionResult" class="result-section">
      <div class="result-header">
        <h3>转换完成</h3>
        <div class="result-info">
          <span class="algorithm-used">使用算法: {{ conversionResult.algorithm }}</span>
        </div>
      </div>

      <!-- OCR结果特殊显示 -->
      <div v-if="activeTab === 'ocr' && conversionResult.recognizedText" class="ocr-result">
        <h4>识别结果:</h4>
        <div class="text-result">
          <textarea 
            v-model="conversionResult.recognizedText" 
            readonly 
            rows="10"
            class="ocr-text"
          ></textarea>
          <div class="text-stats">
            <span>识别文本长度: {{ conversionResult.textLength }} 字符</span>
            <span>识别语言: {{ getLanguageName(conversionResult.language) }}</span>
          </div>
        </div>
      </div>

      <!-- PDF分析结果 -->
      <div v-if="activeTab === 'analyze' && conversionResult.pdfInfo" class="analysis-result">
        <h4>PDF分析结果:</h4>
        <div class="info-grid">
          <div class="info-item">
            <label>页数:</label>
            <span>{{ conversionResult.pdfInfo.pageCount }}</span>
          </div>
          <div class="info-item">
            <label>文件大小:</label>
            <span>{{ formatFileSize(conversionResult.pdfInfo.fileSize) }}</span>
          </div>
          <div class="info-item">
            <label>包含表格:</label>
            <span>{{ conversionResult.pdfInfo.hasTable ? '是' : '否' }}</span>
          </div>
          <div class="info-item">
            <label>表格数量:</label>
            <span>{{ conversionResult.pdfInfo.tableCount }}</span>
          </div>
          <div v-if="conversionResult.pdfInfo.title" class="info-item">
            <label>标题:</label>
            <span>{{ conversionResult.pdfInfo.title }}</span>
          </div>
          <div v-if="conversionResult.pdfInfo.author" class="info-item">
            <label>作者:</label>
            <span>{{ conversionResult.pdfInfo.author }}</span>
          </div>
        </div>
      </div>

      <!-- 文件下载 -->
      <div v-if="conversionResult.fileId" class="download-section">
        <button class="download-button" @click="downloadResult">
          <i class="fas fa-download"></i>
          下载转换结果
        </button>
      </div>
    </div>

    <!-- 支持的功能列表 -->
    <div class="features-section">
      <h3>支持的功能</h3>
      <div class="features-grid">
        <div class="feature-card">
          <i class="fas fa-file-word"></i>
          <h4>PDF转Word</h4>
          <p>文本提取 + Apache POI</p>
        </div>
        <div class="feature-card">
          <i class="fas fa-file-excel"></i>
          <h4>PDF转Excel</h4>
          <p>Tabula表格识别</p>
        </div>
        <div class="feature-card">
          <i class="fas fa-file-csv"></i>
          <h4>PDF转CSV</h4>
          <p>表格数据提取</p>
        </div>
        <div class="feature-card">
          <i class="fas fa-eye"></i>
          <h4>OCR识别</h4>
          <p>Tesseract OCR引擎</p>
        </div>
        <div class="feature-card">
          <i class="fas fa-search"></i>
          <h4>PDF分析</h4>
          <p>文档信息提取</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { pdfApi, utils } from '../api'
import api from '@/api'
import WordToPdf from './WordToPdf.vue'

// 响应式数据
const activeTab = ref('toWord')
const selectedFile = ref<File | null>(null)
const isDragOver = ref(false)
const isProcessing = ref(false)
const progress = ref(0)
const progressText = ref('')
const conversionResult = ref<any>(null)
const fileSource = ref('upload')
const selectedFileId = ref<string | null>(null)
const loadingFiles = ref(false)
const existingFiles = ref<any[]>([])

// 文件输入引用
const pdfInput = ref<HTMLInputElement>();
const imageInput = ref<HTMLInputElement>();
const ocrInput = ref<HTMLInputElement>();

// 转换选项
const wordOptions = ref({
  format: 'docx'
})

const excelOptions = ref({
  algorithm: 'auto'
})

const ocrOptions = ref({
  language: 'eng'
})

// 功能标签页
const functionTabs = [
  { key: 'toWord', label: 'PDF转Word', icon: 'fas fa-file-word' },
  { key: 'toExcel', label: 'PDF转Excel', icon: 'fas fa-file-excel' },
  { key: 'toCsv', label: 'PDF转CSV', icon: 'fas fa-file-csv' },
  { key: 'ocr', label: 'OCR识别', icon: 'fas fa-eye' },
  { key: 'analyze', label: 'PDF分析', icon: 'fas fa-search' },
  { key: 'fromImages', label: '图片转PDF', icon: 'fas fa-file-image' },
  { key: 'wordToPdf', label: 'Word转PDF', icon: 'fas fa-file-word' }
]

// 计算属性
const getAcceptedFileTypes = computed(() => {
  if (activeTab.value === 'fromImages') {
    return 'image/*'
  }
  if (activeTab.value === 'ocr') {
    return 'application/pdf,image/*'
  }
  return 'application/pdf'
})

const getUploadText = () => {
  const texts = {
    toWord: '选择PDF文件转换为Word文档',
    toExcel: '选择PDF文件转换为Excel表格',
    toCsv: '选择PDF文件转换为CSV文件',
    ocr: '选择PDF或图片文件进行OCR识别',
    analyze: '选择PDF文件进行分析',
    fromImages: '选择图片文件转换为PDF',
    wordToPdf: '选择Word文件转换为PDF'
  }
  return texts[activeTab.value as keyof typeof texts] || '选择文件'
}

const getSupportedFormats = () => {
  if (activeTab.value === 'ocr' || activeTab.value === 'fromImages') {
    return 'JPG, PNG, TIFF'
  }
  return 'PDF'
}

const getCurrentFunctionIcon = () => {
  const tab = functionTabs.find(t => t.key === activeTab.value)
  return tab?.icon || 'fas fa-cog'
}

const getCurrentFunctionLabel = () => {
  const labels = {
    toWord: '转换为Word',
    toExcel: '转换为Excel',
    toCsv: '转换为CSV',
    ocr: '开始OCR识别',
    analyze: '分析PDF',
    fromImages: '转换为PDF',
    wordToPdf: '转换为PDF'
  }
  return labels[activeTab.value as keyof typeof labels] || '开始处理'
}

// 方法
const triggerFileInput = () => {
  if (activeTab.value === 'fromImages') {
    imageInput.value?.click();
  } else if (activeTab.value === 'ocr') {
    ocrInput.value?.click();
  } else {
    pdfInput.value?.click();
  }
}

const handleFileSelect = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.files && target.files[0]) {
    selectedFile.value = target.files[0]
    conversionResult.value = null
  }
}

const handleDrop = (event: DragEvent) => {
  event.preventDefault()
  isDragOver.value = false
  
  if (event.dataTransfer?.files && event.dataTransfer.files[0]) {
    selectedFile.value = event.dataTransfer.files[0]
    conversionResult.value = null
  }
}

const removeFile = () => {
  selectedFile.value = null
  selectedFileId.value = null
  conversionResult.value = null
  if (pdfInput.value) {
    pdfInput.value.value = ''
  }
  if (imageInput.value) {
    imageInput.value.value = ''
  }
  if (ocrInput.value) {
    ocrInput.value.value = ''
  }
}

const formatFileSize = (bytes: number): string => {
  return utils.formatFileSize(bytes)
}

const getLanguageName = (code: string): string => {
  const languages: Record<string, string> = {
    'eng': '英语',
    'chi_sim': '简体中文',
    'chi_tra': '繁体中文',
    'jpn': '日语',
    'kor': '韩语',
    'fra': '法语',
    'deu': '德语',
    'spa': '西班牙语'
  }
  return languages[code] || code
}

const startConversion = async () => {
  if (!selectedFile.value && !selectedFileId.value) return

  isProcessing.value = true
  progress.value = 0
  progressText.value = '开始处理...'
  conversionResult.value = null

  try {
    // 模拟进度更新
    const progressInterval = setInterval(() => {
      if (progress.value < 90) {
        progress.value += Math.random() * 10
        progressText.value = `处理中... ${Math.round(progress.value)}%`
      }
    }, 500)

    let result
    switch (activeTab.value) {
      case 'toWord':
        progressText.value = '正在转换为Word文档...'
        if (selectedFile.value) {
          result = await pdfApi.convertPdfToWord(selectedFile.value)
        } else {
          // 使用已有文件ID进行转换 - 这里需要后端支持根据文件ID转换
          result = await pdfApi.convertPdfToWordById(selectedFileId.value!)
        }
        break
      case 'toExcel':
        progressText.value = '正在识别表格并转换为Excel...'
        if (selectedFile.value) {
          result = await pdfApi.convertPdfToExcel(selectedFile.value)
        } else {
          result = await pdfApi.convertPdfToExcelById(selectedFileId.value!)
        }
        break
      case 'toCsv':
        progressText.value = '正在提取表格数据...'
        if (selectedFile.value) {
          result = await pdfApi.convertPdfToCsv(selectedFile.value)
        } else {
          result = await pdfApi.convertPdfToCsvById(selectedFileId.value!)
        }
        break
      case 'ocr':
        progressText.value = '正在进行OCR文字识别...'
        if (selectedFile.value) {
          result = await pdfApi.performOcr(selectedFile.value, ocrOptions.value.language)
        } else {
          result = await pdfApi.performOcrById(selectedFileId.value!, ocrOptions.value.language)
        }
        break
      case 'analyze':
        progressText.value = '正在分析PDF文档...'
        if (selectedFile.value) {
          result = await pdfApi.analyzePdf(selectedFile.value)
        } else {
          result = await pdfApi.analyzePdfById(selectedFileId.value!)
        }
        break
      case 'fromImages':
        progressText.value = '正在将图片转换为PDF...'
        if (selectedFile.value) {
          // This case should ideally handle multiple files, 
          // but the current UI only supports single file selection.
          // For now, we'll convert the single selected image.
          result = await pdfApi.convertImagesToPdf([selectedFile.value])
        } else {
          // This case implies multiple existing files should be selected, 
          // which the UI doesn't support yet. 
          // We'll assume a single selected file ID for now.
          result = await pdfApi.imagesToPdfByIds([parseInt(selectedFileId.value!)])
        }
        break
      case 'wordToPdf':
        progressText.value = '正在将Word文档转换为PDF...'
        if (selectedFile.value) {
          result = await pdfApi.convertWordToPdf(selectedFile.value)
        } else {
          throw new Error('请先选择Word文件')
        }
        break
      default:
        throw new Error('未知的转换类型')
    }

    clearInterval(progressInterval)
    progress.value = 100
    progressText.value = '处理完成!'
    conversionResult.value = result

  } catch (error: any) {
    console.error('转换失败:', error)
    progressText.value = '处理失败: ' + error.message
  } finally {
    isProcessing.value = false
  }
}

const downloadResult = async () => {
  if (conversionResult.value?.fileId) {
    try {
      await utils.downloadFileById(conversionResult.value.fileId)
    } catch (error) {
      console.error('下载失败:', error)
    }
  }
}

const loadExistingFiles = async () => {
  loadingFiles.value = true
  try {
    const response = await api.files.getAll()
    if (response.success) {
      // 过滤只显示PDF文件（如果需要的话可以根据当前功能调整）
      existingFiles.value = response.data.filter((file: any) => {
        if (activeTab.value === 'fromImages') {
          return file.fileType.startsWith('image')
        } else if (activeTab.value === 'merge' || activeTab.value === 'split' || activeTab.value === 'compress' || activeTab.value === 'toImages') {
          return file.fileType === 'PDF'
        } else {
          return true // 其他功能支持多种格式
        }
      })
      if (existingFiles.value.length === 0) {
        fileSource.value = 'upload'
      }
    } else {
      console.error('获取文件列表失败:', response.message)
    }
  } catch (error) {
    console.error('加载文件失败:', error)
  } finally {
    loadingFiles.value = false
  }
}

const selectExistingFile = (file: any) => {
  selectedFile.value = null
  conversionResult.value = null
  selectedFileId.value = file.id
}

const getFileIcon = (fileType: string) => {
  const icons: Record<string, string> = {
    'application/pdf': 'fas fa-file-pdf',
    'application/msword': 'fas fa-file-word',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document': 'fas fa-file-word',
    'application/vnd.ms-excel': 'fas fa-file-excel',
    'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet': 'fas fa-file-excel',
    'application/csv': 'fas fa-file-csv',
    'image/jpeg': 'fas fa-file-image',
    'image/png': 'fas fa-file-image',
    'image/tiff': 'fas fa-file-image',
    'image/gif': 'fas fa-file-image'
  }
  return icons[fileType] || 'fas fa-file'
}

const formatDate = (date: string): string => {
  const formattedDate = new Date(date).toLocaleDateString()
  return formattedDate.replace(',', ' ')
}

const getSelectedFileName = () => {
  if (selectedFile.value) {
    return selectedFile.value.name
  } else if (selectedFileId.value) {
    const file = existingFiles.value.find(f => f.id === selectedFileId.value)
    return file?.originalName || '未命名文件'
  }
  return '未命名文件'
}

const getSelectedFileSize = () => {
  if (selectedFile.value) {
    return formatFileSize(selectedFile.value.size)
  } else if (selectedFileId.value) {
    const file = existingFiles.value.find(f => f.id === selectedFileId.value)
    return file ? formatFileSize(file.fileSize) : '未知大小'
  }
  return '未知大小'
}
</script>

<style scoped>
.pdf-converter {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.converter-header {
  text-align: center;
  margin-bottom: 30px;
}

.converter-header h2 {
  color: #2c3e50;
  margin-bottom: 10px;
}

.subtitle {
  color: #7f8c8d;
  font-size: 14px;
}

.function-selector {
  margin-bottom: 30px;
}

.function-tabs {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: center;
}

.tab-button {
  padding: 12px 20px;
  border: 2px solid #e0e0e0;
  background: white;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.tab-button:hover {
  border-color: #3498db;
  background: #f8f9fa;
}

.tab-button.active {
  border-color: #3498db;
  background: #3498db;
  color: white;
}

.file-source-selector {
  margin-bottom: 30px;
}

.source-tabs {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: center;
}

.source-tab {
  padding: 12px 20px;
  border: 2px solid #e0e0e0;
  background: white;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.source-tab:hover {
  border-color: #3498db;
  background: #f8f9fa;
}

.source-tab.active {
  border-color: #3498db;
  background: #3498db;
  color: white;
}

.upload-section {
  margin-bottom: 20px;
}

.upload-area {
  border: 2px dashed #bdc3c7;
  border-radius: 12px;
  padding: 40px 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #fafafa;
}

.upload-area:hover,
.upload-area.drag-over {
  border-color: #3498db;
  background: #f0f8ff;
}

.upload-icon {
  font-size: 48px;
  color: #bdc3c7;
  margin-bottom: 15px;
}

.upload-text {
  font-size: 16px;
  color: #2c3e50;
  margin-bottom: 5px;
}

.upload-hint {
  font-size: 12px;
  color: #7f8c8d;
}

.existing-files-section {
  margin-bottom: 20px;
}

.files-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.files-header h3 {
  color: #2c3e50;
  margin-bottom: 5px;
}

.refresh-btn {
  background: none;
  border: none;
  color: #7f8c8d;
  cursor: pointer;
  padding: 0;
  font: inherit;
  outline: inherit;
}

.loading-files {
  text-align: center;
  margin-bottom: 10px;
}

.no-files {
  text-align: center;
  margin-bottom: 10px;
}

.files-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 10px;
}

.file-item {
  display: flex;
  align-items: center;
  padding: 10px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e0e0e0;
  cursor: pointer;
  transition: all 0.3s ease;
}

.file-item:hover {
  background: #f0f8ff;
}

.file-icon {
  font-size: 24px;
  color: #e74c3c;
  margin-right: 10px;
}

.file-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.file-name {
  font-weight: 500;
  color: #2c3e50;
}

.file-meta {
  font-size: 12px;
  color: #7f8c8d;
}

.selected-indicator {
  margin-left: auto;
  color: #27ae60;
}

.selected-file {
  margin-bottom: 20px;
}

.file-info {
  display: flex;
  align-items: center;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e0e0e0;
}

.file-icon {
  font-size: 24px;
  color: #e74c3c;
  margin-right: 15px;
}

.file-details {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.file-name {
  font-weight: 500;
  color: #2c3e50;
}

.file-size {
  font-size: 12px;
  color: #7f8c8d;
}

.remove-file {
  background: none;
  border: none;
  color: #e74c3c;
  cursor: pointer;
  padding: 5px;
  border-radius: 4px;
}

.remove-file:hover {
  background: #ffeaea;
}

.conversion-options {
  margin-bottom: 20px;
}

.option-group {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #e0e0e0;
}

.option-group h3 {
  margin-bottom: 15px;
  color: #2c3e50;
}

.option-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.option-item label {
  min-width: 100px;
  font-weight: 500;
  color: #2c3e50;
}

.algorithm-info {
  color: #27ae60;
  font-size: 14px;
  font-weight: 500;
}

.option-item select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: white;
}

.action-section {
  text-align: center;
  margin-bottom: 20px;
}

.convert-button {
  background: #3498db;
  color: white;
  border: none;
  padding: 15px 30px;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  gap: 10px;
}

.convert-button:hover:not(:disabled) {
  background: #2980b9;
  transform: translateY(-2px);
}

.convert-button:disabled {
  background: #bdc3c7;
  cursor: not-allowed;
  transform: none;
}

.progress-section {
  margin-bottom: 20px;
}

.progress-bar {
  width: 100%;
  height: 8px;
  background: #ecf0f1;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 10px;
}

.progress-fill {
  height: 100%;
  background: #3498db;
  transition: width 0.3s ease;
}

.progress-text {
  text-align: center;
  color: #7f8c8d;
  font-size: 14px;
}

.result-section {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #e0e0e0;
  margin-bottom: 20px;
}

.result-header {
  margin-bottom: 15px;
}

.result-header h3 {
  color: #27ae60;
  margin-bottom: 5px;
}

.algorithm-used {
  color: #7f8c8d;
  font-size: 14px;
}

.ocr-result,
.analysis-result {
  margin-bottom: 15px;
}

.text-result {
  margin-top: 10px;
}

.ocr-text {
  width: 100%;
  padding: 15px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-family: monospace;
  font-size: 14px;
  line-height: 1.5;
  resize: vertical;
}

.text-stats {
  display: flex;
  gap: 20px;
  margin-top: 10px;
  font-size: 12px;
  color: #7f8c8d;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 10px;
  margin-top: 10px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #eee;
}

.info-item label {
  font-weight: 500;
  color: #2c3e50;
}

.download-section {
  text-align: center;
}

.download-button {
  background: #27ae60;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.download-button:hover {
  background: #229954;
  transform: translateY(-1px);
}

.features-section {
  margin-top: 40px;
  text-align: center;
}

.features-section h3 {
  margin-bottom: 20px;
  color: #2c3e50;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 15px;
}

.feature-card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #e0e0e0;
  text-align: center;
  transition: all 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.feature-card i {
  font-size: 32px;
  color: #3498db;
  margin-bottom: 10px;
}

.feature-card h4 {
  margin-bottom: 5px;
  color: #2c3e50;
}

.feature-card p {
  font-size: 12px;
  color: #7f8c8d;
}

@media (max-width: 768px) {
  .function-tabs {
    flex-direction: column;
  }
  
  .tab-button {
    justify-content: center;
  }
  
  .info-grid {
    grid-template-columns: 1fr;
  }
  
  .features-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style> 