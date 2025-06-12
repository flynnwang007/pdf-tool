<template>
  <div class="files-page">
    <!-- 搜索和筛选 -->
    <div class="search-section">
      <div class="search-container">
        <el-input
          v-model="searchQuery"
          placeholder="搜索文件..."
          class="search-input"
          clearable
          @input="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button 
          class="filter-button" 
          @click="showFilterDrawer = true"
          :class="{ active: hasActiveFilters }"
          text
        >
          <el-icon><Filter /></el-icon>
        </el-button>
      </div>
      
      <!-- 快速筛选标签 -->
      <div class="filter-tags" v-if="quickFilters.length > 0">
        <div class="tags-scroll">
          <el-tag
            v-for="filter in quickFilters"
            :key="filter.key"
            :type="filter.active ? 'success' : undefined"
            :effect="filter.active ? 'dark' : 'plain'"
            class="filter-tag"
            @click="toggleQuickFilter(filter)"
          >
            {{ filter.label }}
          </el-tag>
        </div>
      </div>
    </div>

    <!-- 文件统计 -->
    <div class="stats-section">
      <div class="stats-card">
        <div class="stat-item">
          <span class="stat-value">{{ filteredFiles.length }}</span>
          <span class="stat-label">个文件</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="stat-value">{{ formatTotalSize }}</span>
          <span class="stat-label">总大小</span>
        </div>
        <div class="stat-actions">
          <el-button 
            text 
            :icon="RefreshRight"
            @click="refreshFiles"
            class="refresh-btn"
            title="刷新文件列表"
          />
          <el-button 
            text 
            :icon="viewMode === 'grid' ? List : Grid"
            @click="toggleViewMode"
            class="view-toggle"
          />
          <el-button 
            text
            :disabled="!hasSelectedFiles"
            @click="showBatchActions = true"
            class="batch-btn"
            v-if="selectionMode"
          >
            批量({{ selectedFiles.length }})
          </el-button>
        </div>
      </div>
    </div>

    <!-- 文件列表 -->
    <div class="files-section">
      <el-empty 
        v-if="filteredFiles.length === 0" 
        :description="isLoggedIn() ? '暂无文件' : '登录后查看您的文件'"
        class="empty-state"
      >
        <template #image>
          <div class="empty-icon">{{ isLoggedIn() ? '📁' : '🔐' }}</div>
        </template>
        <template #description>
          <span class="empty-text">{{ isLoggedIn() ? '暂无文件' : '登录后查看您的文件' }}</span>
        </template>
        <el-button 
          v-if="isLoggedIn()" 
          type="primary" 
          @click="navigateTo('/upload')" 
          class="upload-btn"
        >
          上传文件
        </el-button>
        <el-button 
          v-else 
          type="primary" 
          @click="requireAuth('查看文件需要登录，请先登录您的账户。')" 
          class="login-btn"
        >
          立即登录
        </el-button>
      </el-empty>

      <div v-else class="files-container" :class="viewMode">
        <div
          v-for="file in filteredFiles"
          :key="file.id"
          class="file-item"
          :class="{ selected: selectedFiles.includes(file.id) }"
          @click="handleFileClick(file)"
          @long-press="handleFileLongPress(file)"
        >
          <!-- 选择框 -->
          <div 
            v-if="selectionMode" 
            class="file-checkbox"
            @click.stop="toggleFileSelection(file.id)"
          >
            <el-checkbox 
              :model-value="selectedFiles.includes(file.id)"
              @change="toggleFileSelection(file.id)"
            />
          </div>

          <!-- 文件图标/缩略图 -->
          <div class="file-preview">
            <div v-if="file.thumbnail" class="file-thumbnail">
              <img :src="file.thumbnail" alt="缩略图" />
            </div>
            <div v-else class="file-icon">
              <span class="file-type-icon">{{ getFileTypeIcon(file.type) }}</span>
            </div>
            
            <!-- 文件状态标识 -->
            <div v-if="file.isProcessing" class="file-badge processing">
              <el-icon><Loading /></el-icon>
            </div>
            <div v-else-if="file.isFavorite" class="file-badge favorite">
              ⭐
            </div>
          </div>

          <!-- 文件信息 -->
          <div class="file-info">
            <div class="file-name" :title="file.name">
              {{ file.name }}
            </div>
            <div class="file-meta">
              <span class="file-size">{{ formatFileSize(file.size) }}</span>
              <span class="file-date">{{ formatDate(file.updatedAt) }}</span>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="file-actions" @click.stop v-if="!selectionMode">
            <el-dropdown trigger="click" @command="handleFileAction" placement="bottom-end">
              <el-button text circle class="action-btn">
                <el-icon><MoreFilled /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item :command="{ action: 'preview', file }">
                    <el-icon><View /></el-icon>
                    预览
                  </el-dropdown-item>
                  <el-dropdown-item :command="{ action: 'download', file }">
                    <el-icon><Download /></el-icon>
                    下载
                  </el-dropdown-item>
                  <el-dropdown-item :command="{ action: 'share', file }">
                    <el-icon><Share /></el-icon>
                    分享
                  </el-dropdown-item>
                  <el-dropdown-item :command="{ action: 'favorite', file }">
                    <el-icon><Star /></el-icon>
                    {{ file.isFavorite ? '取消收藏' : '收藏' }}
                  </el-dropdown-item>
                  <el-dropdown-item :command="{ action: 'rename', file }">
                    <el-icon><Edit /></el-icon>
                    重命名
                  </el-dropdown-item>
                  <el-dropdown-item :command="{ action: 'delete', file }" divided>
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>
    </div>

    <!-- 筛选抽屉 -->
    <el-drawer
      v-model="showFilterDrawer"
      title="筛选条件"
      direction="btt"
      size="60%"
      class="filter-drawer"
    >
      <div class="filter-content">
        <!-- 文件类型 -->
        <div class="filter-group">
          <h4 class="filter-title">文件类型</h4>
          <div class="filter-options">
            <div
              v-for="type in fileTypes"
              :key="type.value"
              class="filter-option"
              :class="{ active: filters.type === type.value }"
              @click="setFilter('type', type.value)"
            >
              {{ type.label }}
            </div>
          </div>
        </div>

        <!-- 文件大小 -->
        <div class="filter-group">
          <h4 class="filter-title">文件大小</h4>
          <div class="filter-options">
            <div
              v-for="size in fileSizes"
              :key="size.value"
              class="filter-option"
              :class="{ active: filters.size === size.value }"
              @click="setFilter('size', size.value)"
            >
              {{ size.label }}
            </div>
          </div>
        </div>

        <!-- 修改时间 -->
        <div class="filter-group">
          <h4 class="filter-title">修改时间</h4>
          <div class="filter-options">
            <div
              v-for="time in timeRanges"
              :key="time.value"
              class="filter-option"
              :class="{ active: filters.time === time.value }"
              @click="setFilter('time', time.value)"
            >
              {{ time.label }}
            </div>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="filter-actions">
          <el-button @click="clearFilters" class="clear-btn">重置</el-button>
          <el-button type="primary" @click="applyFilters" class="apply-btn">应用</el-button>
        </div>
      </div>
    </el-drawer>

    <!-- 批量操作抽屉 -->
    <el-drawer
      v-model="showBatchActions"
      title="批量操作"
      direction="btt"
      size="40%"
      class="batch-drawer"
    >
      <div class="batch-content">
        <div class="batch-info">
          已选择 {{ selectedFiles.length }} 个文件
        </div>
        <div class="batch-actions">
          <el-button @click="batchDownload" class="batch-action-btn">
            <el-icon><Download /></el-icon>
            批量下载
          </el-button>
          <el-button @click="batchShare" class="batch-action-btn">
            <el-icon><Share /></el-icon>
            批量分享
          </el-button>
          <el-button @click="batchFavorite" class="batch-action-btn">
            <el-icon><Star /></el-icon>
            批量收藏
          </el-button>
          <el-button @click="batchDelete" type="danger" class="batch-action-btn">
            <el-icon><Delete /></el-icon>
            批量删除
          </el-button>
        </div>
        <div class="batch-bottom">
          <el-button @click="cancelSelection" class="cancel-btn">取消选择</el-button>
        </div>
      </div>
    </el-drawer>

    <!-- 文件预览对话框 -->
    <el-dialog
      v-model="showPreview"
      :title="previewFile?.name"
      width="calc(100vw - 32px)"
      class="preview-dialog"
      center
    >
      <div class="preview-content">
        <div v-if="previewFile?.type === 'IMAGE'" class="image-preview">
          <img :src="previewFile.thumbnail || previewFile.url" alt="预览图片" />
        </div>
        <div v-else class="file-preview-placeholder">
          <span class="preview-icon">{{ getFileTypeIcon(previewFile?.type || '') }}</span>
          <div class="preview-info">
            <div class="preview-name">{{ previewFile?.name }}</div>
            <div class="preview-meta">
              {{ formatFileSize(previewFile?.size || 0) }} • {{ formatDate(previewFile?.updatedAt || '') }}
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="preview-actions">
          <el-button @click="downloadFile(previewFile)" type="primary">
            <el-icon><Download /></el-icon>
            下载
          </el-button>
          <el-button @click="shareFile(previewFile)">
            <el-icon><Share /></el-icon>
            分享
          </el-button>
        </div>
      </template>
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
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Filter,
  List,
  Grid,
  MoreFilled,
  View,
  Download,
  Share,
  Star,
  Edit,
  Delete,
  Loading,
  Close,
  RefreshRight
} from '@element-plus/icons-vue'
import api from '@/api'
import { useAuth } from '@/composables/useAuth'
import LoginPrompt from '@/components/auth/LoginPrompt.vue'

// 文件项类型定义
interface FileItem {
  id: number
  name: string
  type: string
  size: number
  updatedAt: string
  isFavorite: boolean
  isProcessing: boolean
  url: string
  thumbnail?: string | null
}

const router = useRouter()
const route = useRoute()
const { requireAuth, showLoginPrompt, loginPromptMessage, closeLoginPrompt, isLoggedIn } = useAuth()

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

// 响应式数据
const searchQuery = ref('')
const viewMode = ref('list') // 'list' | 'grid'
const selectionMode = ref(false)
const selectedFiles = ref<number[]>([])
const showFilterDrawer = ref(false)
const showBatchActions = ref(false)
const showPreview = ref(false)
const previewFile = ref<FileItem | null>(null)

// 筛选条件
const filters = ref({
  type: '',
  size: '',
  time: ''
})

// 快速筛选选项
const quickFilters = ref([
  { key: 'pdf', label: 'PDF', active: false },
  { key: 'image', label: '图片', active: false },
  { key: 'recent', label: '最近', active: false },
  { key: 'favorite', label: '收藏', active: false }
])

// 示例文件数据
const files = ref<FileItem[]>([])

// 筛选选项
const fileTypes = [
  { label: '全部', value: '' },
  { label: 'PDF', value: 'PDF' },
  { label: 'Word', value: 'WORD' },
  { label: 'Excel', value: 'EXCEL' },
  { label: 'PowerPoint', value: 'POWERPOINT' },
  { label: '图片', value: 'IMAGE' }
]

const fileSizes = [
  { label: '全部', value: '' },
  { label: '小于1MB', value: 'small' },
  { label: '1-10MB', value: 'medium' },
  { label: '10-100MB', value: 'large' },
  { label: '大于100MB', value: 'xlarge' }
]

const timeRanges = [
  { label: '全部', value: '' },
  { label: '今天', value: 'today' },
  { label: '本周', value: 'week' },
  { label: '本月', value: 'month' },
  { label: '更早', value: 'older' }
]

// 计算属性
const filteredFiles = computed(() => {
  let result = files.value

  // 搜索过滤
  if (searchQuery.value) {
    result = result.filter(file => 
      file.name.toLowerCase().includes(searchQuery.value.toLowerCase())
    )
  }

  // 快速筛选
  const activeFilters = quickFilters.value.filter(f => f.active)
  if (activeFilters.length > 0) {
    result = result.filter(file => {
      return activeFilters.some(filter => {
        switch (filter.key) {
          case 'pdf':
            return file.type === 'PDF'
          case 'image':
            return file.type === 'IMAGE'
          case 'recent':
            return isRecent(file.updatedAt)
          case 'favorite':
            return file.isFavorite
          default:
            return true
        }
      })
    })
  }

  // 高级筛选
  if (filters.value.type) {
    result = result.filter(file => file.type === filters.value.type)
  }

  if (filters.value.size) {
    result = result.filter(file => {
      const size = file.size
      switch (filters.value.size) {
        case 'small': return size < 1024 * 1024
        case 'medium': return size >= 1024 * 1024 && size < 10 * 1024 * 1024
        case 'large': return size >= 10 * 1024 * 1024 && size < 100 * 1024 * 1024
        case 'xlarge': return size >= 100 * 1024 * 1024
        default: return true
      }
    })
  }

  return result
})

const hasActiveFilters = computed(() => {
  return Object.values(filters.value).some(value => value !== '') ||
         quickFilters.value.some(filter => filter.active)
})

const hasSelectedFiles = computed(() => {
  return selectedFiles.value.length > 0
})

const formatTotalSize = computed(() => {
  const totalBytes = filteredFiles.value.reduce((sum, file) => sum + file.size, 0)
  return formatFileSize(totalBytes)
})

// 工具函数
const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i]
}

const formatDate = (dateString: string): string => {
  const now = new Date()
  const date = new Date(dateString)
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

const isRecent = (dateString: string): boolean => {
  const now = new Date()
  const date = new Date(dateString)
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  return days <= 7
}

const getFileTypeIcon = (type: string): string => {
  switch (type) {
    case 'PDF': return '📄'
    case 'WORD': return '📝'
    case 'EXCEL': return '📊'
    case 'POWERPOINT': return '📺'
    case 'IMAGE': return '🖼️'
    default: return '📁'
  }
}

// 事件处理
const navigateTo = (path: string) => {
  router.push(path)
}

const handleSearch = () => {
  // 搜索逻辑已在计算属性中处理
}

const toggleViewMode = () => {
  viewMode.value = viewMode.value === 'list' ? 'grid' : 'list'
}

const toggleQuickFilter = (filter: any) => {
  filter.active = !filter.active
}

const setFilter = (key: string, value: string) => {
  const filtersObj = filters.value as any
  filtersObj[key] = filtersObj[key] === value ? '' : value
}

const clearFilters = () => {
  filters.value = { type: '', size: '', time: '' }
  quickFilters.value.forEach(filter => filter.active = false)
}

const applyFilters = () => {
  showFilterDrawer.value = false
}

// 文件操作
const handleFileClick = (file: any) => {
  if (selectionMode.value) {
    toggleFileSelection(file.id)
  } else {
    showFilePreview(file)
  }
}

const handleFileLongPress = (file: any) => {
  if (!selectionMode.value) {
    selectionMode.value = true
    selectedFiles.value = [file.id]
  }
}

const toggleFileSelection = (fileId: number) => {
  const index = selectedFiles.value.indexOf(fileId)
  if (index > -1) {
    selectedFiles.value.splice(index, 1)
  } else {
    selectedFiles.value.push(fileId)
  }
  
  if (selectedFiles.value.length === 0) {
    selectionMode.value = false
  }
}

const handleFileAction = async ({ action, file }: any) => {
  // 对于需要登录的操作，先检查登录状态
  if (['download', 'share', 'favorite', 'rename', 'delete'].includes(action)) {
    const isAuthenticated = await requireAuth('此操作需要登录，请先登录您的账户。')
    if (!isAuthenticated) {
      return
    }
  }

  switch (action) {
    case 'preview':
      showFilePreview(file)
      break
    case 'download':
      downloadFile(file)
      break
    case 'share':
      shareFile(file)
      break
    case 'favorite':
      toggleFavorite(file)
      break
    case 'rename':
      renameFile(file)
      break
    case 'delete':
      deleteFile(file)
      break
  }
}

// 文件操作方法
const downloadFile = (file: any) => {
  const link = document.createElement('a')
  link.href = file.url
  link.download = file.name
  link.click()
  ElMessage.success('开始下载文件')
}

const shareFile = (file: any) => {
  if (navigator.share) {
    navigator.share({
      title: file.name,
      url: file.url
    }).catch(() => {
      copyToClipboard(file.url)
    })
  } else {
    copyToClipboard(file.url)
  }
}

const copyToClipboard = (text: string) => {
  navigator.clipboard.writeText(text).then(() => {
    ElMessage.success('链接已复制到剪贴板')
  })
}

const toggleFavorite = (file: any) => {
  file.isFavorite = !file.isFavorite
  ElMessage.success(file.isFavorite ? '已加入收藏' : '已取消收藏')
}

const renameFile = async (file: any) => {
  try {
    const { value: newName } = await ElMessageBox.prompt('请输入新的文件名', '重命名文件', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValue: file.name.split('.')[0],
      inputValidator: (value) => {
        if (!value) return '文件名不能为空'
        if (value.length > 50) return '文件名过长'
        return true
      }
    })
    
    if (newName) {
      const extension = file.name.split('.').pop()
      file.name = `${newName}.${extension}`
      ElMessage.success('文件重命名成功')
    }
  } catch {
    // 用户取消
  }
}

const deleteFile = async (file: any) => {
  try {
    await ElMessageBox.confirm(`确定要删除文件 "${file.name}" 吗？`, '删除确认', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    // 调用后端接口删除
    const res = await api.files.deleteFile(file.id)
    if (res.success) {
      const index = files.value.findIndex(f => f.id === file.id)
      if (index > -1) {
        files.value.splice(index, 1)
      }
      ElMessage.success('文件删除成功')
    } else {
      ElMessage.error(res.message || '文件删除失败')
    }
  } catch {
    // 用户取消
  }
}

// 批量操作
const batchDownload = () => {
  selectedFiles.value.forEach(fileId => {
    const file = files.value.find(f => f.id === fileId)
    if (file) downloadFile(file)
  })
  cancelSelection()
}

const batchShare = () => {
  const fileUrls = selectedFiles.value.map(fileId => {
    const file = files.value.find(f => f.id === fileId)
    return file?.url
  }).filter(Boolean).join('\n')
  
  copyToClipboard(fileUrls)
  cancelSelection()
}

const batchFavorite = () => {
  selectedFiles.value.forEach(fileId => {
    const file = files.value.find(f => f.id === fileId)
    if (file) file.isFavorite = true
  })
  ElMessage.success(`已收藏 ${selectedFiles.value.length} 个文件`)
  cancelSelection()
}

const batchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedFiles.value.length} 个文件吗？`, '批量删除确认', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    selectedFiles.value.forEach(fileId => {
      const index = files.value.findIndex(f => f.id === fileId)
      if (index > -1) files.value.splice(index, 1)
    })
    
    ElMessage.success('批量删除成功')
    cancelSelection()
  } catch {
    // 用户取消
  }
}

const cancelSelection = () => {
  selectionMode.value = false
  selectedFiles.value = []
  showBatchActions.value = false
}

// 显示文件预览
const showFilePreview = (file: any) => {
  previewFile.value = file
  showPreview.value = true
  
  // 强制应用移动端样式
  nextTick(() => {
    applyMobileDialogStyles()
  })
}

// 监听窗口大小变化
const handleResize = () => {
  applyMobileDialogStyles()
}

onMounted(() => {
  // 加载文件列表
  loadFiles()
  
  window.addEventListener('resize', handleResize)
  
  // 添加全局样式覆盖
  const style = document.createElement('style')
  style.innerHTML = `
    @media (max-width: 768px) {
      .preview-dialog .el-dialog {
        margin: 5vh auto !important;
        width: calc(100vw - 16px) !important;
        max-width: calc(100vw - 16px) !important;
        min-width: auto !important;
      }
    }
    @media (max-width: 414px) {
      .preview-dialog .el-dialog {
        margin: 2vh auto !important;
        width: calc(100vw - 8px) !important;
        max-width: calc(100vw - 8px) !important;
        min-width: auto !important;
      }
    }
  `
  document.head.appendChild(style)
})

// 监听路由变化，刷新文件列表
watch(() => route.path, (newPath) => {
  if (newPath === '/files') {
    loadFiles()
  }
})

// 手动刷新文件列表
const refreshFiles = async () => {
  await loadFiles()
  ElMessage.success('文件列表已刷新')
}

// 加载文件列表
const loadFiles = async () => {
  // 如果用户未登录，显示空列表
  if (!isLoggedIn()) {
    files.value = []
    return
  }

  try {
    const response = await api.files.getAll()
    if (response.success) {
      // 转换后端数据格式为前端需要的格式
      files.value = response.data.map((file: any) => ({
        id: file.id,
        name: file.originalName,
        type: file.fileType,
        size: file.fileSize,
        updatedAt: file.createdAt,
        isFavorite: false, // 默认值，后续可以扩展
        isProcessing: false, // 默认值
        url: `/api/files/${file.id}/download`,
        thumbnail: file.fileType === 'IMAGE' ? `/api/files/${file.id}/download` : null
      }))
    } else {
      console.error('获取文件列表失败:', response.message)
      ElMessage.error('获取文件列表失败')
    }
  } catch (error) {
    console.error('加载文件列表出错:', error)
    ElMessage.error('加载文件列表失败')
  }
}

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.files-page {
  min-height: 100%;
  background: #f6f6f6;
  padding: 0;
}

/* 搜索区域 */
.search-section {
  padding: 16px;
  background: #f6f6f6;
}

.search-container {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}

.search-input {
  flex: 1;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 20px;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.filter-button {
  width: 40px;
  height: 40px;
  border-radius: 20px;
  background: white;
  border: 1px solid rgba(0, 0, 0, 0.05);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.filter-button.active {
  background: #07c160;
  color: white;
  border-color: #07c160;
}

.filter-tags {
  overflow: hidden;
}

.tags-scroll {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding-bottom: 4px;
  -webkit-overflow-scrolling: touch;
}

.tags-scroll::-webkit-scrollbar {
  display: none;
}

.filter-tag {
  flex-shrink: 0;
  cursor: pointer;
  border-radius: 16px;
}

/* 统计区域 */
.stats-section {
  padding: 0 16px 16px;
}

.stats-card {
  background: white;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.stat-value {
  font-size: 18px;
  font-weight: 700;
  color: #07c160;
}

.stat-label {
  font-size: 12px;
  color: #666666;
  margin-top: 2px;
}

.stat-divider {
  width: 1px;
  height: 24px;
  background: #f0f0f0;
  margin: 0 16px;
}

.stat-actions {
  display: flex;
  gap: 8px;
}

.refresh-btn,
.view-toggle,
.batch-btn {
  color: #07c160;
}

/* 文件列表 */
.files-section {
  padding: 0 16px 20px;
}

.empty-state {
  background: white;
  border-radius: 12px;
  padding: 40px 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-text {
  color: #666666;
  font-size: 14px;
  margin-bottom: 20px;
  display: block;
}

.upload-btn {
  background: #07c160;
  border-color: #07c160;
  border-radius: 8px;
}

.upload-btn:hover {
  background: #06ad56;
  border-color: #06ad56;
}

.files-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.file-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
}

.file-item:last-child {
  border-bottom: none;
}

.file-item:hover {
  background: #f8f9fa;
}

.file-item.selected {
  background: #f0f9f0;
  border-color: #07c160;
}

.file-checkbox {
  margin-right: 12px;
}

.file-preview {
  margin-right: 12px;
  position: relative;
}

.file-thumbnail {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  overflow: hidden;
}

.file-thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.file-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8f9fa;
  border-radius: 8px;
}

.file-type-icon {
  font-size: 20px;
}

.file-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
}

.file-badge.processing {
  background: #1890ff;
  color: white;
}

.file-badge.favorite {
  background: #ff4d4f;
  color: white;
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
  color: #999999;
}

.file-actions {
  margin-left: 12px;
}

.action-btn {
  color: #666666;
}

/* 网格视图 */
.files-container.grid {
  padding: 16px;
  background: #f6f6f6;
}

.files-container.grid .file-item {
  background: white;
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.05);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  margin-bottom: 12px;
  padding: 16px;
  flex-direction: column;
  text-align: center;
}

.files-container.grid .file-preview {
  margin-right: 0;
  margin-bottom: 12px;
}

.files-container.grid .file-icon,
.files-container.grid .file-thumbnail {
  width: 60px;
  height: 60px;
}

.files-container.grid .file-type-icon {
  font-size: 32px;
}

.files-container.grid .file-info {
  margin-bottom: 12px;
}

.files-container.grid .file-actions {
  margin-left: 0;
}

/* 筛选抽屉 */
.filter-drawer :deep(.el-drawer) {
  border-radius: 16px 16px 0 0;
}

.filter-drawer :deep(.el-drawer__header) {
  padding: 20px 16px 12px;
  margin-bottom: 0;
  border-bottom: 1px solid #f0f0f0;
}

.filter-drawer :deep(.el-drawer__title) {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
}

.filter-drawer :deep(.el-drawer__close-btn) {
  width: 32px;
  height: 32px;
  font-size: 18px;
}

.filter-content {
  padding: 20px 16px;
}

.filter-group {
  margin-bottom: 28px;
}

.filter-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 16px 0;
}

.filter-options {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.filter-option {
  padding: 10px 16px;
  background: #f8f9fa;
  border-radius: 24px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 14px;
  color: #666666;
  border: 1px solid transparent;
  min-height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 60px;
}

.filter-option.active {
  background: #07c160;
  color: white;
  border-color: #07c160;
}

.filter-option:hover {
  background: #e8f5e8;
  border-color: #07c160;
}

.filter-option.active:hover {
  background: #06ad56;
}

.filter-actions {
  display: flex;
  gap: 12px;
  margin-top: 32px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.clear-btn,
.apply-btn {
  flex: 1;
  height: 48px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 500;
}

.clear-btn {
  border: 1px solid #d9d9d9;
  color: #666666;
}

.clear-btn:hover {
  border-color: #07c160;
  color: #07c160;
}

.apply-btn {
  background: #07c160;
  border-color: #07c160;
}

.apply-btn:hover {
  background: #06ad56;
  border-color: #06ad56;
}

/* 批量操作抽屉 */
.batch-drawer :deep(.el-drawer) {
  border-radius: 16px 16px 0 0;
}

.batch-drawer :deep(.el-drawer__header) {
  padding: 20px 16px 12px;
  margin-bottom: 0;
  border-bottom: 1px solid #f0f0f0;
}

.batch-drawer :deep(.el-drawer__title) {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
}

.batch-drawer :deep(.el-drawer__close-btn) {
  width: 32px;
  height: 32px;
  font-size: 18px;
}

.batch-content {
  padding: 20px 16px;
}

.batch-info {
  text-align: center;
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 28px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 12px;
}

.batch-actions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-bottom: 28px;
}

.batch-action-btn {
  border-radius: 12px;
  height: 64px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 500;
  border: 1px solid #f0f0f0;
  background: white;
  color: #1a1a1a;
  transition: all 0.3s ease;
}

.batch-action-btn:hover {
  border-color: #07c160;
  background: #f0f9f0;
}

.batch-action-btn .el-icon {
  font-size: 20px;
}

.batch-action-btn[type="danger"] {
  color: #ff4d4f;
}

.batch-action-btn[type="danger"]:hover {
  border-color: #ff4d4f;
  background: #fff2f0;
}

.batch-bottom {
  text-align: center;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.cancel-btn {
  border-radius: 12px;
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  border: 1px solid #d9d9d9;
  color: #666666;
}

.cancel-btn:hover {
  border-color: #07c160;
  color: #07c160;
}

/* 预览对话框 */
.preview-dialog :deep(.el-dialog) {
  border-radius: 16px;
  margin: 5vh auto;
  max-width: 500px;
  width: calc(100vw - 32px);
}

.preview-dialog :deep(.el-dialog__header) {
  padding: 24px 20px 12px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 0;
}

.preview-dialog :deep(.el-dialog__title) {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.preview-dialog :deep(.el-dialog__close) {
  width: 32px;
  height: 32px;
  font-size: 18px;
}

.preview-dialog :deep(.el-dialog__body) {
  padding: 20px;
}

.preview-dialog :deep(.el-dialog__footer) {
  padding: 16px 20px 24px;
  border-top: 1px solid #f0f0f0;
}

.preview-content {
  text-align: center;
  padding: 0;
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-preview {
  width: 100%;
}

.image-preview img {
  max-width: 100%;
  max-height: 400px;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.file-preview-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 20px;
  width: 100%;
}

.preview-icon {
  font-size: 80px;
  margin-bottom: 20px;
  color: #07c160;
}

.preview-info {
  text-align: center;
}

.preview-name {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 8px;
  word-break: break-all;
  line-height: 1.4;
}

.preview-meta {
  font-size: 14px;
  color: #666666;
}

.preview-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.preview-actions .el-button {
  flex: 1;
  height: 48px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 500;
}

.preview-actions .el-button[type="primary"] {
  background: #07c160;
  border-color: #07c160;
}

.preview-actions .el-button[type="primary"]:hover {
  background: #06ad56;
  border-color: #06ad56;
}

.preview-actions .el-button:not([type="primary"]) {
  border: 1px solid #d9d9d9;
  color: #666666;
}

.preview-actions .el-button:not([type="primary"]):hover {
  border-color: #07c160;
  color: #07c160;
}

/* 移动端响应式优化 */
@media (max-width: 414px) {
  .search-section {
    padding: 12px;
  }
  
  .stats-section {
    padding: 0 12px 12px;
  }
  
  .files-section {
    padding: 0 12px 16px;
  }
  
  .stats-card {
    padding: 12px;
  }
  
  .stat-value {
    font-size: 16px;
  }
  
  .stat-label {
    font-size: 11px;
  }
  
  .file-item {
    padding: 12px;
  }
  
  .file-name {
    font-size: 13px;
  }
  
  .file-meta {
    font-size: 11px;
  }
  
  .files-container.grid {
    padding: 12px;
  }
  
  .files-container.grid .file-item {
    padding: 12px;
  }

  /* 筛选抽屉移动端优化 */
  .filter-drawer :deep(.el-drawer__header) {
    padding: 16px 12px 8px;
  }
  
  .filter-drawer :deep(.el-drawer__title) {
    font-size: 16px;
  }
  
  .filter-content {
    padding: 16px 12px;
  }
  
  .filter-group {
    margin-bottom: 24px;
  }
  
  .filter-title {
    font-size: 15px;
    margin-bottom: 12px;
  }
  
  .filter-options {
    gap: 8px;
  }
  
  .filter-option {
    padding: 8px 12px;
    font-size: 13px;
    min-height: 40px;
    border-radius: 20px;
  }
  
  /* 批量操作抽屉移动端优化 */
  .batch-drawer :deep(.el-drawer__header) {
    padding: 16px 12px 8px;
  }
  
  .batch-drawer :deep(.el-drawer__title) {
    font-size: 16px;
  }
  
  .batch-content {
    padding: 16px 12px;
  }
  
  .batch-info {
    font-size: 15px;
    margin-bottom: 20px;
    padding: 12px;
  }
  
  .batch-actions {
    grid-template-columns: 1fr;
    gap: 10px;
    margin-bottom: 20px;
  }
  
  .batch-action-btn {
    height: 56px;
    border-radius: 10px;
    font-size: 13px;
    gap: 4px;
  }
  
  .batch-action-btn .el-icon {
    font-size: 18px;
  }
  
  .batch-bottom {
    padding-top: 12px;
  }
  
  .cancel-btn {
    height: 44px;
    border-radius: 10px;
    font-size: 15px;
  }
  
  /* 预览对话框移动端优化 */
  .preview-dialog :deep(.el-dialog) {
    margin: 3vh auto;
    width: calc(100vw - 24px);
    border-radius: 12px;
  }
  
  .preview-dialog :deep(.el-dialog__header) {
    padding: 20px 16px 8px;
  }
  
  .preview-dialog :deep(.el-dialog__title) {
    font-size: 15px;
  }
  
  .preview-dialog :deep(.el-dialog__close) {
    width: 28px;
    height: 28px;
    font-size: 16px;
  }
  
  .preview-dialog :deep(.el-dialog__body) {
    padding: 16px;
  }
  
  .preview-dialog :deep(.el-dialog__footer) {
    padding: 12px 16px 20px;
  }
  
  .preview-content {
    min-height: 150px;
  }
  
  .image-preview img {
    max-height: 300px;
    border-radius: 8px;
  }
  
  .file-preview-placeholder {
    padding: 30px 16px;
  }
  
  .preview-icon {
    font-size: 64px;
    margin-bottom: 16px;
  }
  
  .preview-name {
    font-size: 16px;
    margin-bottom: 6px;
  }
  
  .preview-meta {
    font-size: 13px;
  }
  
  .preview-actions .el-button {
    height: 44px;
    border-radius: 10px;
    font-size: 15px;
  }
}

/* 触摸优化 */
.file-item,
.filter-tag,
.filter-option,
.batch-action-btn,
.cancel-btn,
.preview-actions .el-button {
  -webkit-tap-highlight-color: transparent;
  user-select: none;
}

/* 加载动画 */
@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.file-badge.processing .el-icon {
  animation: rotate 2s linear infinite;
}

/* 安全区域适配 */
@supports (padding: max(0px)) {
  .filter-drawer :deep(.el-drawer),
  .batch-drawer :deep(.el-drawer) {
    padding-bottom: max(16px, env(safe-area-inset-bottom));
  }
  
  .preview-dialog :deep(.el-dialog) {
    margin-bottom: max(3vh, env(safe-area-inset-bottom) + 3vh);
  }
}
</style> 