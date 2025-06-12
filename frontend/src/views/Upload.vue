<template>
  <div class="upload-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">上传文件</h1>
      <p class="page-subtitle">支持PDF、Word、Excel、PowerPoint等格式</p>
    </div>

    <!-- 主上传区域 -->
    <div class="upload-section">
      <div class="upload-card">
        <div 
          class="upload-area"
          :class="{ 
            'is-dragover': isDragOver,
            'is-uploading': isUploading 
          }"
          @drop="handleDrop"
          @dragover.prevent="handleDragOver"
          @dragenter.prevent="handleDragEnter"
          @dragleave="handleDragLeave"
          @click="triggerFileInput"
        >
          <input
            ref="fileInput"
            type="file"
            class="file-input"
            :accept="acceptedTypes"
            multiple
            @change="handleFileSelect"
          >
          
          <div v-if="!isUploading" class="upload-content">
            <div class="upload-icon">📁</div>
            <div class="upload-text">
              <div class="upload-title">点击或拖拽上传文件</div>
              <div class="upload-subtitle">
                支持PDF、Word、Excel、PPT等多种格式
              </div>
              <div class="upload-limit">单个文件最大 {{ maxSizeText }}</div>
            </div>
          </div>

          <div v-else class="uploading-content">
            <div class="uploading-icon">
              <el-icon :size="48" color="#07c160">
                <Loading />
              </el-icon>
            </div>
            <div class="uploading-text">
              <div class="uploading-title">正在上传文件...</div>
              <div class="uploading-progress">
                {{ uploadedCount }} / {{ totalFiles }} 个文件已完成
              </div>
            </div>
          </div>
        </div>

        <!-- 快速操作 -->
        <div class="quick-actions">
          <el-button 
            type="primary" 
            @click="triggerFileInput"
            :disabled="isUploading"
            class="upload-btn"
          >
            <el-icon><Plus /></el-icon>
            选择文件
          </el-button>
          <el-button 
            @click="showHistory = true"
            :disabled="isUploading"
            class="history-btn"
          >
            <el-icon><Clock /></el-icon>
            上传历史
          </el-button>
        </div>
      </div>
    </div>

    <!-- 上传列表 -->
    <div v-if="uploadQueue.length > 0" class="upload-list-section">
      <div class="section-header">
        <h2 class="section-title">
          <span class="title-icon">📋</span>
          上传队列 ({{ uploadQueue.length }})
        </h2>
        <div class="section-actions">
          <el-button 
            text 
            @click="clearCompleted" 
            v-if="hasCompleted"
            class="clear-btn"
          >
            清除已完成
          </el-button>
          <el-button 
            text 
            @click="clearAll"
            :disabled="isUploading"
            class="clear-all-btn"
          >
            清空队列
          </el-button>
        </div>
      </div>

      <div class="upload-list">
        <div 
          v-for="(item, index) in uploadQueue"
          :key="index"
          class="upload-item"
          :class="{ 
            'is-uploading': item.status === 'uploading',
            'is-success': item.status === 'success',
            'is-error': item.status === 'error'
          }"
        >
          <div class="file-icon">
            <span class="file-type">{{ getFileIcon(item.file.name) }}</span>
          </div>
          <div class="file-info">
            <div class="file-name">{{ item.file.name }}</div>
            <div class="file-meta">
              <span class="file-size">{{ formatFileSize(item.file.size) }}</span>
              <span class="file-status">{{ getStatusText(item.status) }}</span>
            </div>
            <div v-if="item.status === 'uploading'" class="progress-bar">
              <el-progress 
                :percentage="item.progress" 
                :stroke-width="4"
                :show-text="false"
                color="#07c160"
              />
            </div>
          </div>
          <div class="item-actions">
            <el-icon 
              v-if="item.status === 'success'" 
              color="#07c160"
              class="status-icon"
            >
              <CircleCheck />
            </el-icon>
            <el-icon 
              v-else-if="item.status === 'error'" 
              color="#ff4d4f"
              class="status-icon"
              @click="retryUpload(index)"
            >
              <RefreshRight />
            </el-icon>
            <el-icon 
              v-else-if="item.status === 'uploading'" 
              color="#07c160"
              class="status-icon loading"
            >
              <Loading />
            </el-icon>
            <el-icon 
              v-else
              color="#999"
              class="remove-icon"
              @click="removeFromQueue(index)"
            >
              <Close />
            </el-icon>
          </div>
        </div>
      </div>
    </div>

    <!-- 支持格式说明 -->
    <div class="format-info-section">
      <div class="format-card">
        <h3 class="format-title">
          <span class="format-icon">ℹ️</span>
          支持的文件格式
        </h3>
        <div class="format-list">
          <div 
            v-for="format in supportedFormats"
            :key="format.type"
            class="format-item"
          >
            <span class="format-emoji">{{ format.emoji }}</span>
            <div class="format-details">
              <div class="format-name">{{ format.name }}</div>
              <div class="format-extensions" v-html="formatExtensions(format.extensions)"></div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 上传历史抽屉 -->
    <el-drawer
      v-model="showHistory"
      title=""
      direction="btt"
      size="80%"
      class="history-drawer"
    >
      <template #header>
        <div class="history-header">
          <span class="history-title">上传历史</span>
          <el-button 
            text 
            :icon="RefreshRight"
            @click="refreshUploadHistory"
            class="refresh-btn"
            title="刷新历史记录"
          />
        </div>
      </template>
      
      <div class="history-content">
        <div v-if="uploadHistory.length === 0" class="empty-history">
          <div class="empty-icon">📝</div>
          <div class="empty-text">暂无上传记录</div>
        </div>
        
        <div v-else class="history-list">
          <div 
            v-for="(record, index) in uploadHistory"
            :key="index"
            class="history-item"
          >
            <div class="history-date">{{ formatDate(record.date) }}</div>
            <div class="history-files">
              <div 
                v-for="(file, fileIndex) in record.files"
                :key="fileIndex"
                class="history-file"
              >
                <span class="file-icon">{{ getFileIcon(file.name) }}</span>
                <div class="file-details">
                  <div class="file-name">{{ file.name }}</div>
                  <div class="file-size">{{ formatFileSize(file.size) }}</div>
                </div>
                <el-button 
                  text 
                  size="small"
                  @click="downloadFile(file)"
                  class="download-btn"
                >
                  <el-icon><Download /></el-icon>
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-drawer>

    <!-- 上传成功提示 -->
    <el-dialog
      v-model="showSuccessDialog"
      title="上传完成"
      width="calc(100vw - 32px)"
      class="success-dialog"
      center
    >
      <div class="success-content">
        <div class="success-icon">✅</div>
        <div class="success-text">
          <div class="success-title">文件上传成功</div>
          <div class="success-subtitle">共上传 {{ completedFiles.length }} 个文件</div>
        </div>
        <div class="success-actions">
          <el-button 
            type="primary" 
            @click="goToFiles"
            class="view-files-btn"
          >
            查看文件
          </el-button>
          <el-button 
            @click="continueUpload"
            class="continue-btn"
          >
            继续上传
          </el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 登录提示对话框 -->
    <LoginPrompt 
      v-model="showLoginPrompt"
      :message="loginPromptMessage"
      @cancel="closeLoginPrompt"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElLoading } from 'element-plus'
import {
  Plus,
  Clock,
  CircleCheck,
  RefreshRight,
  Loading,
  Close,
  Download,
  Upload as UploadIcon
} from '@element-plus/icons-vue'
import api from '@/api'
import { useAuth } from '@/composables/useAuth'
import LoginPrompt from '@/components/auth/LoginPrompt.vue'

const router = useRouter()
const { requireAuth, showLoginPrompt, loginPromptMessage, closeLoginPrompt, isLoggedIn } = useAuth()

// 响应式数据
const isDragOver = ref(false)
const isUploading = ref(false)
const uploadedCount = ref(0)
const totalFiles = ref(0)
const uploadQueue = ref<any[]>([])
const showHistory = ref(false)
const showSuccessDialog = ref(false)
const completedFiles = ref<any[]>([])
const fileInput = ref<HTMLInputElement>()

// 上传历史数据 - 从后端API加载
const uploadHistory = ref<any[]>([])

// 支持的文件格式
const supportedFormats = [
  {
    type: 'pdf',
    name: 'PDF文档',
    emoji: '📄',
    extensions: ['.pdf']
  },
  {
    type: 'word',
    name: 'Word文档',
    emoji: '📝',
    extensions: ['.doc', '.docx']
  },
  {
    type: 'excel',
    name: 'Excel表格',
    emoji: '📊',
    extensions: ['.xls', '.xlsx']
  },
  {
    type: 'powerpoint',
    name: 'PowerPoint',
    emoji: '📺',
    extensions: ['.ppt', '.pptx']
  },
  {
    type: 'image',
    name: '图片文件',
    emoji: '🖼️',
    extensions: ['.jpg', '.jpeg', '.png', '.gif', '.bmp']
  },
  {
    type: 'text',
    name: '文本文件',
    emoji: '📃',
    extensions: ['.txt', '.rtf']
  }
]

// 计算属性
const acceptedTypes = computed(() => {
  return supportedFormats.flatMap(format => format.extensions).join(',')
})

const acceptedExtensions = computed(() => {
  return supportedFormats.flatMap(format => format.extensions)
})

const maxSizeText = '100MB'
const maxSize = 100 * 1024 * 1024 // 100MB

const hasCompleted = computed(() => {
  return uploadQueue.value.some(item => item.status === 'success')
})

// 工具函数
const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i]
}

const formatExtensions = (extensions: string[]): string => {
  // 所有格式都用逗号连接，不分行
  return extensions.join(', ')
}

const formatDate = (dateString: string): string => {
  const date = new Date(dateString)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (days === 0) {
    return '今天'
  } else if (days === 1) {
    return '昨天'
  } else if (days < 7) {
    return `${days}天前`
  } else {
    return date.toLocaleDateString('zh-CN')
  }
}

const getFileIcon = (fileName: string): string => {
  const ext = fileName.toLowerCase().split('.').pop()
  if (!ext) return '📁'
  
  if (['.pdf'].includes(`.${ext}`)) return '📄'
  if (['.doc', '.docx'].includes(`.${ext}`)) return '📝'
  if (['.xls', '.xlsx'].includes(`.${ext}`)) return '📊'
  if (['.ppt', '.pptx'].includes(`.${ext}`)) return '📺'
  if (['.jpg', '.jpeg', '.png', '.gif', '.bmp'].includes(`.${ext}`)) return '🖼️'
  if (['.txt', '.rtf'].includes(`.${ext}`)) return '📃'
  return '📁'
}

const getStatusText = (status: string): string => {
  switch (status) {
    case 'pending': return '等待上传'
    case 'uploading': return '上传中'
    case 'success': return '上传成功'
    case 'error': return '上传失败'
    default: return ''
  }
}

// 事件处理
const triggerFileInput = () => {
  if (!isUploading.value) {
    fileInput.value?.click()
  }
}

const handleFileSelect = async (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.files) {
    const files = Array.from(target.files)
    await addFilesToQueue(files)
    target.value = '' // 清空input
  }
}

const handleDrop = async (event: DragEvent) => {
  event.preventDefault()
  isDragOver.value = false
  
  if (isUploading.value) return
  
  const files = Array.from(event.dataTransfer?.files || [])
  await addFilesToQueue(files)
}

const handleDragOver = () => {
  if (!isUploading.value) {
    isDragOver.value = true
  }
}

const handleDragEnter = () => {
  if (!isUploading.value) {
    isDragOver.value = true
  }
}

const handleDragLeave = () => {
  isDragOver.value = false
}

const addFilesToQueue = async (files: File[]) => {
  // 检查登录状态
  const isAuthenticated = await requireAuth('上传文件需要登录，请先登录您的账户。')
  if (!isAuthenticated) {
    return
  }

  const validFiles = files.filter(file => {
    // 检查文件大小
    if (file.size > maxSize) {
      ElMessage.warning(`文件 ${file.name} 大小超过限制`)
      return false
    }
    
    // 检查文件类型
    const ext = `.${file.name.toLowerCase().split('.').pop()}`
    if (!acceptedExtensions.value.includes(ext)) {
      ElMessage.warning(`不支持的文件格式: ${file.name}`)
      return false
    }
    
    return true
  })
  
  if (validFiles.length === 0) return
  
  const newItems = validFiles.map(file => ({
    file,
    status: 'pending',
    progress: 0,
    id: Date.now() + Math.random()
  }))
  
  uploadQueue.value.push(...newItems)
  
  if (!isUploading.value) {
    startUpload()
  }
}

const startUpload = async () => {
  if (isUploading.value || uploadQueue.value.length === 0) return
  
  isUploading.value = true
  uploadedCount.value = 0
  totalFiles.value = uploadQueue.value.filter(item => item.status === 'pending').length
  
  for (const item of uploadQueue.value) {
    if (item.status !== 'pending') continue
    
    try {
      item.status = 'uploading'
      await uploadFile(item)
      item.status = 'success'
      uploadedCount.value++
    } catch (error) {
      item.status = 'error'
      ElMessage.error(`上传失败: ${item.file.name}`)
    }
  }
  
  isUploading.value = false
  
  // 收集成功上传的文件
  completedFiles.value = uploadQueue.value
    .filter(item => item.status === 'success')
    .map(item => item.file)
  
  if (completedFiles.value.length > 0) {
    await completeUpload()
  }
}

const uploadFile = (item: any): Promise<void> => {
  return new Promise(async (resolve, reject) => {
    try {
      // 开始上传进度动画
      let progress = 0
      const progressInterval = setInterval(() => {
        if (progress < 90) { // 保持在90%以下，等待实际响应
          progress += Math.random() * 10
          item.progress = Math.min(progress, 90)
        }
      }, 200)
      
      // 调用API上传文件
      const response = await api.files.upload(item.file)
      
      // 清除进度动画
      clearInterval(progressInterval)
      
      if (response.success) {
        // 完成上传
        item.progress = 100
        item.uploadedFileId = response.data.fileId
        setTimeout(() => {
          resolve()
        }, 500)
      } else {
        reject(new Error(response.message || '上传失败'))
      }
    } catch (error) {
      console.error('Upload error:', error)
      reject(error)
    }
  })
}

const retryUpload = async (index: number) => {
  const item = uploadQueue.value[index]
  if (item.status === 'error') {
    item.status = 'pending'
    item.progress = 0
    
    if (!isUploading.value) {
      startUpload()
    }
  }
}

const removeFromQueue = (index: number) => {
  uploadQueue.value.splice(index, 1)
}

const clearCompleted = () => {
  uploadQueue.value = uploadQueue.value.filter(item => item.status !== 'success')
}

const clearAll = () => {
  if (!isUploading.value) {
    uploadQueue.value = []
  }
}

const downloadFile = (file: any) => {
  // 模拟下载
  ElMessage.success(`开始下载 ${file.name}`)
}

const goToFiles = () => {
  showSuccessDialog.value = false
  router.push('/files')
}

const continueUpload = async () => {
  showSuccessDialog.value = false
  // 清除已完成的文件
  clearCompleted()
  
  if (completedFiles.value.length > 0) {
    // 重新加载上传历史
    await loadUploadHistory()
  }
}

// 加载上传历史
const loadUploadHistory = async () => {
  // 如果用户未登录，显示空历史记录
  if (!isLoggedIn()) {
    uploadHistory.value = []
    return
  }

  try {
    const response = await api.files.getAll()
    if (response.success) {
      // 按日期分组文件
      const filesByDate = response.data.reduce((groups: any, file: any) => {
        const date = new Date(file.createdAt).toDateString()
        if (!groups[date]) {
          groups[date] = []
        }
        groups[date].push({
          name: file.originalName,
          size: file.fileSize,
          url: `/api/files/${file.id}/download`,
          id: file.id
        })
        return groups
      }, {})
      
      // 转换为历史记录格式，按日期倒序排列
      uploadHistory.value = Object.entries(filesByDate)
        .map(([date, files]) => ({
          date: new Date(date).toISOString(),
          files: files
        }))
        .sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime())
    } else {
      console.error('获取上传历史失败:', response.message)
    }
  } catch (error) {
    console.error('加载上传历史出错:', error)
    // 只有在已登录状态下才显示错误消息
    if (isLoggedIn()) {
      ElMessage.error('加载上传历史失败')
    }
  }
}

// 修改上传完成后的历史记录添加逻辑
const completeUpload = async () => {
  if (completedFiles.value.length > 0) {
    // 显示成功提示
    showSuccessMessage()
    
    // 重新加载上传历史
    await loadUploadHistory()
  }
}

// 强制应用移动端样式的方法
const applyMobileDialogStyles = () => {
  if (window.innerWidth <= 768) {
    setTimeout(() => {
      const dialogs = document.querySelectorAll('.el-dialog')
      dialogs.forEach(dialog => {
        const element = dialog as HTMLElement
        if (window.innerWidth <= 414) {
          element.style.setProperty('width', 'calc(100vw - 8px)', 'important')
          element.style.setProperty('max-width', 'calc(100vw - 8px)', 'important')
          element.style.setProperty('min-width', 'auto', 'important')
          element.style.setProperty('margin', '2vh auto', 'important')
        } else {
          element.style.setProperty('width', 'calc(100vw - 16px)', 'important')
          element.style.setProperty('max-width', 'calc(100vw - 16px)', 'important')
          element.style.setProperty('min-width', 'auto', 'important')
          element.style.setProperty('margin', '5vh auto', 'important')
        }
      })
    }, 100)
  }
}

// 显示成功对话框
const showSuccessMessage = () => {
  showSuccessDialog.value = true
  
  // 强制应用移动端样式
  nextTick(() => {
    applyMobileDialogStyles()
  })
}

// 监听窗口大小变化
const handleResize = () => {
  applyMobileDialogStyles()
}

// 手动刷新上传历史
const refreshUploadHistory = async () => {
  await loadUploadHistory()
  ElMessage.success('上传历史已刷新')
}

onMounted(() => {
  // 加载上传历史
  loadUploadHistory()
  
  window.addEventListener('resize', handleResize)
  
  // 添加全局样式覆盖
  const style = document.createElement('style')
  style.innerHTML = `
    @media (max-width: 768px) {
      .success-dialog .el-dialog {
        margin: 5vh auto !important;
        width: calc(100vw - 16px) !important;
        max-width: calc(100vw - 16px) !important;
        min-width: auto !important;
      }
    }
    @media (max-width: 414px) {
      .success-dialog .el-dialog {
        margin: 2vh auto !important;
        width: calc(100vw - 8px) !important;
        max-width: calc(100vw - 8px) !important;
        min-width: auto !important;
      }
    }
  `
  document.head.appendChild(style)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.upload-page {
  min-height: 100%;
  background: #f6f6f6;
  padding: 0;
  max-width: 100vw;
  overflow-x: hidden;
  box-sizing: border-box;
}

/* 页面头部 */
.page-header {
  padding: 20px 16px 16px;
  text-align: center;
  background: #f6f6f6;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 8px 0;
}

.page-subtitle {
  font-size: 14px;
  color: #666666;
  margin-bottom: 4px;
  max-width: 100%;
  word-wrap: break-word;
  line-height: 1.4;
}

/* 上传区域 */
.upload-section {
  padding: 0 16px 20px;
}

.upload-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.upload-area {
  border: 2px dashed #d9d9d9;
  border-radius: 12px;
  padding: 40px 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #fafafa;
  margin-bottom: 20px;
}

.upload-area:hover {
  border-color: #07c160;
  background: #f0f9f0;
}

.upload-area.is-dragover {
  border-color: #07c160;
  background: #f0f9f0;
  transform: scale(1.02);
}

.upload-area.is-uploading {
  border-color: #07c160;
  background: #f0f9f0;
  cursor: not-allowed;
}

.file-input {
  display: none;
}

.upload-content {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.upload-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.upload-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 8px;
}

.upload-subtitle {
  font-size: 14px;
  color: #666666;
  margin-bottom: 4px;
  max-width: 100%;
  word-wrap: break-word;
  line-height: 1.4;
}

.upload-limit {
  font-size: 12px;
  color: #999999;
}

.uploading-content {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.uploading-icon {
  margin-bottom: 16px;
}

.uploading-title {
  font-size: 16px;
  font-weight: 600;
  color: #07c160;
  margin-bottom: 8px;
}

.uploading-progress {
  font-size: 14px;
  color: #666666;
}

.quick-actions {
  display: flex;
  gap: 12px;
}

.upload-btn {
  flex: 1;
  background: #07c160;
  border-color: #07c160;
  border-radius: 8px;
  height: 44px;
}

.upload-btn:hover {
  background: #06ad56;
  border-color: #06ad56;
}

.history-btn {
  flex: 1;
  border-radius: 8px;
  height: 44px;
}

/* 上传列表 */
.upload-list-section {
  padding: 0 16px 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 6px;
}

.title-icon {
  font-size: 18px;
}

.section-actions {
  display: flex;
  gap: 8px;
}

.clear-btn,
.clear-all-btn {
  font-size: 12px;
  color: #666666;
}

.upload-list {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.upload-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  transition: all 0.3s ease;
}

.upload-item:last-child {
  border-bottom: none;
}

.upload-item.is-uploading {
  background: #f0f9f0;
}

.upload-item.is-success {
  background: #f6ffed;
}

.upload-item.is-error {
  background: #fff2f0;
}

.file-icon {
  margin-right: 12px;
}

.file-type {
  font-size: 20px;
  display: block;
}

.file-info {
  flex: 1;
  min-width: 0;
}

.file-name {
  font-size: 14px;
  font-weight: 500;
  color: #1a1a1a;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #666666;
  margin-bottom: 4px;
}

.progress-bar {
  width: 100%;
}

.item-actions {
  margin-left: 12px;
}

.status-icon,
.remove-icon {
  cursor: pointer;
  font-size: 18px;
}

.status-icon.loading {
  animation: rotate 2s linear infinite;
}

.remove-icon:hover {
  color: #ff4d4f;
}

/* 格式信息 */
.format-info-section {
  padding: 0 16px 20px;
}

.format-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.format-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 16px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.format-icon {
  font-size: 18px;
}

.format-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.format-item {
  display: flex;
  align-items: flex-start;
  padding: 8px;
  background: #f8f9fa;
  border-radius: 8px;
  min-height: 48px;
}

.format-emoji {
  font-size: 20px;
  margin-right: 8px;
  flex-shrink: 0;
  margin-top: 2px;
}

.format-details {
  flex: 1;
  min-width: 0;
}

.format-name {
  font-size: 13px;
  font-weight: 500;
  color: #1a1a1a;
  margin-bottom: 4px;
  line-height: 1.3;
}

.format-extensions {
  font-size: 11px;
  color: #666666;
  line-height: 1.4;
  word-wrap: break-word;
  word-break: break-word;
  overflow-wrap: break-word;
  hyphens: auto;
  white-space: normal;
  max-width: 100%;
}

/* 历史抽屉 */
.history-drawer :deep(.el-drawer) {
  border-radius: 16px 16px 0 0;
}

.history-drawer :deep(.el-drawer__header) {
  padding: 20px 16px 12px;
  margin-bottom: 0;
  border-bottom: 1px solid #f0f0f0;
}

.history-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.history-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
}

.history-header .refresh-btn {
  color: #07c160;
  padding: 8px;
  min-height: 32px;
  min-width: 32px;
}

.history-header .refresh-btn:hover {
  background: #f0f9f0;
}

.history-drawer :deep(.el-drawer__title) {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
}

.history-drawer :deep(.el-drawer__close-btn) {
  width: 32px;
  height: 32px;
  font-size: 18px;
}

.history-content {
  padding: 20px 16px;
}

.empty-history {
  text-align: center;
  padding: 60px 20px;
}

.empty-icon {
  font-size: 80px;
  margin-bottom: 20px;
}

.empty-text {
  font-size: 16px;
  color: #666666;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.history-item {
  background: white;
  border-radius: 12px;
  padding: 16px;
  border: 1px solid #f0f0f0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.history-date {
  font-size: 14px;
  font-weight: 600;
  color: #07c160;
  margin-bottom: 12px;
}

.history-files {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.history-file {
  display: flex;
  align-items: center;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
  min-height: 56px;
}

.history-file .file-icon {
  font-size: 18px;
  margin-right: 12px;
  width: 24px;
  text-align: center;
}

.file-details {
  flex: 1;
  min-width: 0;
}

.history-file .file-name {
  font-size: 14px;
  font-weight: 500;
  color: #1a1a1a;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.3;
}

.history-file .file-size {
  font-size: 12px;
  color: #666666;
}

.download-btn {
  color: #07c160;
  padding: 8px;
  min-height: 40px;
  min-width: 40px;
}

.download-btn:hover {
  background: #f0f9f0;
}

/* 成功弹窗 */
.success-dialog :deep(.el-dialog) {
  border-radius: 16px;
  margin: 8vh auto;
  max-width: 400px;
  width: calc(100vw - 32px);
}

.success-dialog :deep(.el-dialog__header) {
  padding: 24px 20px 12px;
  text-align: center;
}

.success-dialog :deep(.el-dialog__title) {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
}

.success-dialog :deep(.el-dialog__close) {
  width: 32px;
  height: 32px;
  font-size: 18px;
}

.success-dialog :deep(.el-dialog__body) {
  padding: 0 20px 20px;
}

.success-dialog :deep(.el-dialog__footer) {
  padding: 0 20px 24px;
}

.success-content {
  text-align: center;
  padding: 0;
}

.success-icon {
  font-size: 72px;
  margin-bottom: 20px;
}

.success-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 8px;
}

.success-subtitle {
  font-size: 14px;
  color: #666666;
  margin-bottom: 32px;
  line-height: 1.4;
}

.success-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.view-files-btn,
.continue-btn {
  flex: 1;
  height: 48px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 500;
}

.view-files-btn {
  background: #07c160;
  border-color: #07c160;
}

.view-files-btn:hover {
  background: #06ad56;
  border-color: #06ad56;
}

.continue-btn {
  border: 1px solid #d9d9d9;
  color: #666666;
}

.continue-btn:hover {
  border-color: #07c160;
  color: #07c160;
}

/* 移动端响应式优化 */
@media (max-width: 768px) {
  /* 防止任何元素超出视口 */
  * {
    max-width: 100%;
    box-sizing: border-box;
  }
  
  .format-list {
    grid-template-columns: 1fr;
    gap: 10px;
  }
  
  .format-item {
    min-height: auto;
    padding: 10px;
    align-items: flex-start;
  }
  
  .format-emoji {
    font-size: 18px;
    margin-top: 1px;
    flex-shrink: 0;
  }
  
  .format-details {
    flex: 1;
    min-width: 0;
    max-width: calc(100% - 30px);
  }
  
  .format-name {
    font-size: 12px;
    margin-bottom: 3px;
  }
  
  .format-extensions {
    font-size: 10px;
    line-height: 1.3;
    word-break: break-all;
    overflow-wrap: anywhere;
    white-space: normal;
    max-width: 100%;
  }
}

@media (max-width: 414px) {
  /* 防止任何元素超出视口 */
  * {
    max-width: 100%;
    box-sizing: border-box;
  }
  
  .page-header {
    padding: 16px 12px 12px;
  }
  
  .page-title {
    font-size: 20px;
  }
  
  .upload-section,
  .upload-list-section,
  .format-info-section {
    padding: 0 12px 16px;
  }
  
  .upload-card,
  .format-card {
    padding: 16px;
    margin: 0;
    box-sizing: border-box;
  }
  
  .upload-area {
    padding: 32px 16px;
  }
  
  .upload-icon {
    font-size: 40px;
  }
  
  .upload-title {
    font-size: 16px;
    max-width: 100%;
    word-wrap: break-word;
    overflow-wrap: break-word;
  }
  
  .upload-subtitle {
    font-size: 13px;
    max-width: 100%;
    word-wrap: break-word;
    overflow-wrap: break-word;
    line-height: 1.3;
  }
  
  .upload-limit {
    font-size: 11px;
    max-width: 100%;
    word-wrap: break-word;
    overflow-wrap: break-word;
  }
  
  .format-title {
    font-size: 15px;
    margin-bottom: 12px;
  }
  
  .format-list {
    grid-template-columns: 1fr;
    gap: 8px;
  }
  
  .format-item {
    padding: 8px;
    min-height: auto;
    align-items: flex-start;
    width: 100%;
    box-sizing: border-box;
  }
  
  .format-emoji {
    font-size: 16px;
    margin-right: 6px;
    flex-shrink: 0;
  }
  
  .format-details {
    flex: 1;
    min-width: 0;
    max-width: calc(100% - 25px);
    overflow: hidden;
  }
  
  .format-name {
    font-size: 11px;
    margin-bottom: 2px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  
  .format-extensions {
    font-size: 9px;
    line-height: 1.2;
    word-break: break-all;
    overflow-wrap: anywhere;
    white-space: normal;
    max-width: 100%;
    hyphens: auto;
    overflow: hidden;
  }
  
  .upload-item {
    padding: 12px;
  }
  
  .file-name {
    font-size: 13px;
  }
  
  .file-meta {
    font-size: 11px;
  }
}

/* 触摸优化 */
.upload-area,
.upload-item,
.format-item,
.history-file,
.download-btn,
.view-files-btn,
.continue-btn {
  -webkit-tap-highlight-color: transparent;
  user-select: none;
}

/* 加载动画 */
@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.uploading-icon .el-icon {
  animation: rotate 2s linear infinite;
}

/* 安全区域适配 */
@supports (padding: max(0px)) {
  .history-drawer :deep(.el-drawer) {
    padding-bottom: max(16px, env(safe-area-inset-bottom));
  }
  
  .success-dialog :deep(.el-dialog) {
    margin-bottom: max(8vh, env(safe-area-inset-bottom) + 8vh);
  }
}

/* 历史抽屉移动端优化 */
.history-drawer :deep(.el-drawer__header) {
  padding: 16px 12px 8px;
}

.history-drawer :deep(.el-drawer__title) {
  font-size: 16px;
}

.history-content {
  padding: 16px 12px;
}

.empty-history {
  padding: 40px 16px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-text {
  font-size: 14px;
}

.history-item {
  padding: 12px;
}

.history-date {
  font-size: 13px;
  margin-bottom: 8px;
}

.history-file {
  padding: 10px;
  min-height: 52px;
}

.history-file .file-icon {
  font-size: 16px;
  width: 20px;
}

.history-file .file-name {
  font-size: 13px;
  margin-bottom: 2px;
}

.history-file .file-size {
  font-size: 11px;
}

/* 成功弹窗移动端优化 */
.success-dialog :deep(.el-dialog) {
  margin: 5vh auto;
  width: calc(100vw - 24px);
  border-radius: 12px;
}

.success-dialog :deep(.el-dialog__header) {
  padding: 20px 16px 8px;
}

.success-dialog :deep(.el-dialog__title) {
  font-size: 16px;
}

.success-dialog :deep(.el-dialog__close) {
  width: 28px;
  height: 28px;
  font-size: 16px;
}

.success-dialog :deep(.el-dialog__body) {
  padding: 0 16px 16px;
}

.success-dialog :deep(.el-dialog__footer) {
  padding: 0 16px 20px;
}

.success-icon {
  font-size: 60px;
  margin-bottom: 16px;
}

.success-title {
  font-size: 16px;
  margin-bottom: 6px;
}

.success-subtitle {
  font-size: 13px;
  margin-bottom: 24px;
}

.view-files-btn,
.continue-btn {
  height: 44px;
  border-radius: 10px;
  font-size: 15px;
}
</style> 