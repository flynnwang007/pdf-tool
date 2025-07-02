import axios from 'axios'
import { useAuthStore } from '@/stores/auth'

// API基础配置 - 使用环境变量
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'
console.log('[API Config] 环境:', import.meta.env.MODE, '| API地址:', API_BASE_URL)

// 检测移动端，设置不同的超时时间
const isMobile = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)
const timeoutDuration = isMobile ? 300000 : 120000 // 移动端5分钟，桌面端2分钟
console.log('[API Config] 设备类型:', isMobile ? '移动端' : '桌面端', '超时时间:', timeoutDuration + 'ms')

// 创建axios实例
const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: timeoutDuration,
  headers: {
    'Content-Type': 'multipart/form-data'
  }
})

// 请求拦截器：添加认证信息
api.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore()
    
    // 添加Authorization头部
    if (authStore.session?.access_token) {
      config.headers.Authorization = `Bearer ${authStore.session.access_token}`
    }
    
    // 添加用户ID头部（临时方案）
    if (authStore.user?.id) {
      config.headers['X-User-ID'] = authStore.user.id
    }
    
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 通用请求函数
async function request(url: string, options: RequestInit = {}) {
  const response = await fetch(`${API_BASE_URL}${url}`, {
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
    ...options,
  })
  
  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`)
  }
  
  return response.json()
}

// 文件上传（FormData）
async function uploadRequest(url: string, formData: FormData) {
  const response = await fetch(`${API_BASE_URL}${url}`, {
    method: 'POST',
    body: formData,
  })
  
  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`)
  }
  
  return response.json()
}

// 文件管理API
export const fileApi = {
  // 获取文件列表
  getFiles: async (page = 0, size = 10) => {
    const response = await api.get(`/files?page=${page}&size=${size}`, {
      headers: { 'Content-Type': 'application/json' }
    })
    return response.data
  },

  // 上传文件
  uploadFile: async (file: File) => {
    console.log('[Upload Debug] 准备上传文件:', file.name, '大小:', file.size, '类型:', file.type)
    const formData = new FormData()
    formData.append('file', file)
    
    try {
      const response = await api.post('/files/upload', formData)
      console.log('[Upload Debug] 上传响应:', response.data)
      return response.data
    } catch (error: any) {
      console.error('[Upload Debug] 上传失败:', error)
      throw error
    }
  },

  // 下载文件
  downloadFile: async (id: number) => {
    const response = await api.get(`/files/${id}/download`, {
      responseType: 'blob',
      headers: { 'Content-Type': 'application/json' }
    })
    return response.data
  },

  // 删除文件
  deleteFile: async (id: number) => {
    const response = await api.delete(`/files/${id}`, {
      headers: { 'Content-Type': 'application/json' }
    })
    return response.data
  },

  // 获取所有文件 (兼容旧版本)
  getAll: async () => {
    const response = await api.get('/files', {
      headers: { 'Content-Type': 'application/json' }
    })
    return response.data
  },

  // 上传文件 (兼容旧版本)
  upload: async (file: File) => {
    return fileApi.uploadFile(file)
  },

  // 获取单个文件信息
  getById: async (fileId: number) => {
    const response = await api.get(`/files/${fileId}`, {
      headers: { 'Content-Type': 'application/json' }
    })
    return response.data
  },

  // 下载文件 (兼容旧版本)
  download: (fileId: number) => {
    return `${API_BASE_URL}/files/${fileId}/download`
  },

  // 删除文件 (兼容旧版本)
  delete: async (fileId: number) => {
    return fileApi.deleteFile(fileId)
  },

  // 按类型获取文件
  getByType: async (fileType: string) => {
    const response = await api.get(`/files/types/${fileType}`, {
      headers: { 'Content-Type': 'application/json' }
    })
    return response.data
  },

  uploadFiles: async (files: File[]) => {
    const ids: number[] = []
    for (const file of files) {
      const res = await fileApi.uploadFile(file)
      if (res.success) ids.push(res.data.fileId)
    }
    return ids
  },
}

// PDF工具API
export const pdfApi = {
  // === 基础功能 ===
  
  // 合并PDF
  mergePdfs: async (files: File[], outputFileName = 'merged.pdf') => {
    // 注意：这个API需要fileIds，但这里传入的是File对象
    // 需要先上传文件再合并，这里暂时返回错误
    throw new Error('合并功能需要先上传文件，请使用已有文件进行合并')
  },

  // 基于文件ID合并PDF
  mergePdfsByIds: async (fileIds: number[], outputFileName = 'merged.pdf') => {
    const response = await api.post('/pdf-tools/merge', {
      fileIds,
      outputFileName
    }, {
      headers: { 'Content-Type': 'application/json' }
    })
    return response.data
  },

  // 分割PDF
  splitPdf: async (file: File, pageNumbers?: number[]) => {
    // 注意：这个API需要fileId，但这里传入的是File对象
    // 需要先上传文件再分割，这里暂时返回错误
    throw new Error('分割功能需要先上传文件，请使用已有文件进行分割')
  },

  // 基于文件ID分割PDF
  splitPdfById: async (fileId: number, splitType = 'by-page', options = {}) => {
    const response = await api.post('/pdf-tools/split', {
      fileId,
      splitType,
      options
    }, {
      headers: { 'Content-Type': 'application/json' }
    })
    return response.data
  },

  // 压缩PDF
  compressPdf: async (file: File, compressionLevel = 0.5) => {
    // 注意：这个API需要fileId，但这里传入的是File对象
    // 需要先上传文件再压缩，这里暂时返回错误
    throw new Error('压缩功能需要先上传文件，请使用已有文件进行压缩')
  },

  // 基于文件ID压缩PDF
  compressPdfById: async (fileId: number, compressionLevel = 'medium') => {
    const response = await api.post('/pdf-tools/compress', {
      fileId,
      compressionLevel
    }, {
      headers: { 'Content-Type': 'application/json' }
    })
    return response.data
  },

  // PDF转图片
  convertPdfToImages: async (file: File, format = 'PNG', dpi = 150) => {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('format', format)
    formData.append('dpi', dpi.toString())
    
    const response = await api.post('/pdf-tools/upload-to-images', formData)
    return response.data
  },

  // 图片转PDF
  convertImagesToPdf: async (files: File[], outputFileName = 'images_to_pdf.pdf') => {
    // 先上传图片，拿到 fileId
    const imageFileIds = await fileApi.uploadFiles(files)
    // 再合成 PDF
    const response = await api.post('/pdf-tools/from-images', {
      imageFileIds,
      outputFileName
    }, {
      headers: { 'Content-Type': 'application/json' }
    })
    return response.data
  },

  // 基于文件ID的图片转PDF
  imagesToPdfByIds: async (imageFileIds: number[], outputFileName = 'images_to_pdf.pdf') => {
    const response = await api.post('/pdf-tools/from-images', {
      imageFileIds,
      outputFileName
    }, {
      headers: { 'Content-Type': 'application/json' }
    })
    return response.data
  },

  // === 新增功能：参照Stirling-PDF实现策略 ===

  // PDF转Word文档
  convertPdfToWord: async (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    
    const response = await api.post('/pdf-tools/to-word', formData)
    return response.data
  },

  // PDF转Word文档 - 基于文件ID
  convertPdfToWordById: async (fileId: string | number) => {
    const response = await api.post(`/pdf-tools/to-word/${fileId}`, {}, {
      headers: { 'Content-Type': 'application/json' }
    })
    return response.data
  },

  // PDF转Excel表格
  convertPdfToExcel: async (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    
    const response = await api.post('/pdf-tools/to-excel', formData)
    return response.data
  },

  // PDF转Excel表格 - 基于文件ID
  convertPdfToExcelById: async (fileId: string | number) => {
    const response = await api.post(`/pdf-tools/to-excel/${fileId}`, {}, {
      headers: { 'Content-Type': 'application/json' }
    })
    return response.data
  },

  // PDF转CSV
  convertPdfToCsv: async (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    
    const response = await api.post('/pdf-tools/to-csv', formData)
    return response.data
  },

  // PDF转CSV - 基于文件ID
  convertPdfToCsvById: async (fileId: string | number) => {
    const response = await api.post(`/pdf-tools/to-csv/${fileId}`, {}, {
      headers: { 'Content-Type': 'application/json' }
    })
    return response.data
  },

  // OCR文字识别
  performOcr: async (file: File, language = 'eng') => {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('language', language)
    
    const response = await api.post('/pdf-tools/ocr', formData)
    return response.data
  },

  // OCR文字识别 - 基于文件ID
  performOcrById: async (fileId: string | number, language = 'eng') => {
    console.log('[OCR API Debug] 开始OCR识别，文件ID:', fileId, '语言:', language)
    try {
      const response = await api.post(`/pdf-tools/ocr/${fileId}?language=${language}`, {}, {
        headers: { 'Content-Type': 'application/json' }
      })
      console.log('[OCR API Debug] OCR响应:', response.data)
      return response.data
    } catch (error: any) {
      console.error('[OCR API Debug] OCR失败:', error)
      if (error.response) {
        console.error('[OCR API Debug] 错误响应:', error.response.data)
      }
      throw error
    }
  },

  // PDF信息分析
  analyzePdf: async (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    
    const response = await api.post('/pdf-tools/analyze', formData)
    return response.data
  },

  // PDF信息分析 - 基于文件ID
  analyzePdfById: async (fileId: string | number) => {
    const response = await api.post(`/pdf-tools/analyze/${fileId}`, {}, {
      headers: { 'Content-Type': 'application/json' }
    })
    return response.data
  },

  // 获取支持的功能列表
  getSupportedFeatures: async () => {
    const response = await api.get('/pdf-tools/features', {
      headers: { 'Content-Type': 'application/json' }
    })
    return response.data
  },

  // 获取PDF信息
  getPdfInfo: async (id: number) => {
    const response = await api.get(`/pdf-tools/info/${id}`, {
      headers: { 'Content-Type': 'application/json' }
    })
    return response.data
  },

  // PDF转图片 - 基于文件ID
  pdfToImagesById: async (fileId: number, imageFormat = 'PNG', dpi = 150, pageRange = 'all', customRange = '') => {
    const response = await api.post('/pdf-tools/to-images', {
      fileId,
      imageFormat,
      dpi,
      pageRange,
      customRange
    }, {
      headers: { 'Content-Type': 'application/json' }
    })
    return response.data
  },

  // 添加水印 - 基于文件ID（支持文字水印）
  addWatermarkById: async (fileId: number, watermarkOptions: any) => {
    const response = await api.post('/pdf-tools/watermark', {
      fileId,
      watermarkOptions
    }, {
      headers: { 'Content-Type': 'application/json' }
    })
    return response.data
  },

  // 添加水印 - 基于文件ID（支持图片水印）
  addWatermarkWithImageById: async (fileId: number, watermarkImage: File, watermarkOptions: any) => {
    const formData = new FormData()
    formData.append('fileId', fileId.toString())
    formData.append('watermarkType', watermarkOptions.watermarkType)
    
    // 使用严格的条件检查，避免 0 值被判断为 false
    if (watermarkOptions.watermarkPosition !== undefined && watermarkOptions.watermarkPosition !== null) {
      formData.append('watermarkPosition', watermarkOptions.watermarkPosition)
    }
    if (watermarkOptions.watermarkOpacity !== undefined && watermarkOptions.watermarkOpacity !== null) {
      formData.append('watermarkOpacity', watermarkOptions.watermarkOpacity.toString())
    }
    if (watermarkOptions.watermarkRotation !== undefined && watermarkOptions.watermarkRotation !== null) {
      formData.append('watermarkRotation', watermarkOptions.watermarkRotation.toString())
    }
    if (watermarkOptions.pageRange !== undefined && watermarkOptions.pageRange !== null) {
      formData.append('pageRange', watermarkOptions.pageRange)
    }
    if (watermarkOptions.customRange !== undefined && watermarkOptions.customRange !== null && watermarkOptions.customRange !== '') {
      formData.append('customRange', watermarkOptions.customRange)
    }
    
    if (watermarkOptions.watermarkType === 'text') {
      if (watermarkOptions.watermarkText !== undefined && watermarkOptions.watermarkText !== null && watermarkOptions.watermarkText !== '') {
        formData.append('watermarkText', watermarkOptions.watermarkText)
      }
      if (watermarkOptions.watermarkSize !== undefined && watermarkOptions.watermarkSize !== null) {
        formData.append('watermarkSize', watermarkOptions.watermarkSize.toString())
      }
      if (watermarkOptions.watermarkColor !== undefined && watermarkOptions.watermarkColor !== null && watermarkOptions.watermarkColor !== '') {
        formData.append('watermarkColor', watermarkOptions.watermarkColor)
      }
    } else if (watermarkOptions.watermarkType === 'image') {
      formData.append('watermarkImage', watermarkImage)
    }
    
    const response = await api.post('/pdf-tools/watermark-with-image', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    return response.data
  },

  // === 页面操作功能 ===
  
  // 旋转页面 - 基于文件ID
  rotatePagesById: async (fileId: number, options: any) => {
    const response = await api.post('/pdf-tools/rotate', {
      fileId,
      pageRange: options.pageRange || 'all',
      customRange: options.customRange,
      rotation: options.rotation || 90
    }, {
      headers: { 'Content-Type': 'application/json' }
    })
    return response.data
  },

  // 删除页面 - 基于文件ID
  deletePagesById: async (fileId: number, options: any) => {
    const response = await api.post('/pdf-tools/delete-pages', {
      fileId,
      pageRange: options.pageRange || 'custom',
      customRange: options.customRange
    }, {
      headers: { 'Content-Type': 'application/json' }
    })
    return response.data
  },

  // 提取页面 - 基于文件ID
  extractPagesById: async (fileId: number, options: any) => {
    const response = await api.post('/pdf-tools/extract-pages', {
      fileId,
      pageRange: options.pageRange || 'custom',
      customRange: options.customRange
    }, {
      headers: { 'Content-Type': 'application/json' }
    })
    return response.data
  },

  // 重新排序页面 - 基于文件ID
  reorderPagesById: async (fileId: number, options: any) => {
    const response = await api.post('/pdf-tools/reorder-pages', {
      fileId,
      pageOrder: options.pageOrder || [] // 新的页面顺序数组
    }, {
      headers: { 'Content-Type': 'application/json' }
    })
    return response.data
  },

  // === 安全工具 ===

  // PDF加密 - 基于文件ID
  encryptPdfById: async (fileId: number, userPassword: string, ownerPassword?: string) => {
    const response = await api.post('/pdf-tools/encrypt', {
      fileId,
      userPassword,
      ownerPassword: ownerPassword || userPassword
    }, {
      headers: { 'Content-Type': 'application/json' }
    })
    return response.data
  },

  // PDF解密 - 基于文件ID
  decryptPdfById: async (fileId: number, password: string) => {
    const response = await api.post('/pdf-tools/decrypt', {
      fileId,
      password
    }, {
      headers: { 'Content-Type': 'application/json' }
    })
    return response.data
  },

  // 内容编辑（涂黑） - 基于文件ID
  redactPdfById: async (fileId: number, keywords: string[], pageRange?: string, customRange?: string) => {
    const response = await api.post('/pdf-tools/redact', {
      fileId,
      keywords,
      pageRange: pageRange || 'all',
      customRange
    }, {
      headers: { 'Content-Type': 'application/json' }
    })
    return response.data
  },

  // 数字签名 - 基于文件ID
  digitalSignPdfById: async (fileId: number, signerName: string, reason?: string, location?: string, pageRange?: string, customRange?: string) => {
    const response = await api.post('/pdf-tools/digital-sign', {
      fileId,
      signerName,
      reason: reason || '数字签名',
      location: location || '中国',
      pageRange: pageRange || 'all',
      customRange
    }, {
      headers: { 'Content-Type': 'application/json' }
    })
    return response.data
  },

  // === 新增功能：Word转PDF ===
  convertWordToPdf: async (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    const response = await api.post('/pdf-tools/word-to-pdf', formData)
    return response.data
  },
}

// 兼容旧版本的pdfToolsApi
export const pdfToolsApi = {
  // 合并PDF (兼容旧版本)
  merge: async (fileIds: number[], outputFileName?: string) => {
    const response = await api.post('/pdf-tools/merge', {
      fileIds,
      outputFileName
    }, {
      headers: { 'Content-Type': 'application/json' }
    })
    return response.data
  },

  // 分割PDF (兼容旧版本)
  split: async (fileId: number, splitType: string, options: any) => {
    const response = await api.post('/pdf-tools/split', {
      fileId,
      splitType,
      options
    }, {
      headers: { 'Content-Type': 'application/json' }
    })
    return response.data
  },

  // 压缩PDF (兼容旧版本)
  compress: async (fileId: number, compressionLevel: string = 'medium') => {
    const response = await api.post('/pdf-tools/compress', {
      fileId,
      compressionLevel
    }, {
      headers: { 'Content-Type': 'application/json' }
    })
    return response.data
  },

  // PDF转图片 (兼容旧版本)
  toImages: async (fileId: number, imageFormat: string = 'PNG', dpi: number = 300) => {
    const response = await api.post('/pdf-tools/to-images', {
      fileId,
      imageFormat,
      dpi
    }, {
      headers: { 'Content-Type': 'application/json' }
    })
    return response.data
  },

  // 图片转PDF (兼容旧版本)
  fromImages: async (imageFileIds: number[], outputFileName?: string) => {
    const response = await api.post('/pdf-tools/from-images', {
      imageFileIds,
      outputFileName
    }, {
      headers: { 'Content-Type': 'application/json' }
    })
    return response.data
  },

  // 获取PDF信息 (兼容旧版本)
  getInfo: async (fileId: number) => {
    const response = await api.get(`/pdf-tools/info/${fileId}`, {
      headers: { 'Content-Type': 'application/json' }
    })
    return response.data
  }
}

// 工具函数
export const utils = {
  // 下载文件
  downloadFileById: async (fileId: number, fileName?: string) => {
    try {
      const blob = await fileApi.downloadFile(fileId)
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = fileName || `file_${fileId}`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
    } catch (error) {
      console.error('下载文件失败:', error)
      throw error
    }
  },

  // 批量下载文件
  downloadMultipleFiles: async (fileIds: number[], fileNames?: string[]) => {
    for (let i = 0; i < fileIds.length; i++) {
      const fileId = fileIds[i]
      const fileName = fileNames?.[i] || `file_${fileId}`
      await utils.downloadFileById(fileId, fileName)
      
      // 添加延迟避免浏览器阻止多个下载
      if (i < fileIds.length - 1) {
        await new Promise(resolve => setTimeout(resolve, 500))
      }
    }
  },

  // 格式化文件大小
  formatFileSize: (bytes: number): string => {
    if (bytes === 0) return '0 Bytes'
    
    const k = 1024
    const sizes = ['Bytes', 'KB', 'MB', 'GB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
  },

  // 验证文件类型
  validateFileType: (file: File, allowedTypes: string[]): boolean => {
    return allowedTypes.includes(file.type)
  },

  // 验证PDF文件
  validatePdfFile: (file: File): boolean => {
    return file.type === 'application/pdf'
  },

  // 验证图片文件
  validateImageFile: (file: File): boolean => {
    return file.type.startsWith('image/')
  },

  // 获取文件扩展名
  getFileExtension: (fileName: string): string => {
    return fileName.split('.').pop()?.toLowerCase() || ''
  },

  // 生成唯一文件名
  generateUniqueFileName: (originalName: string, suffix?: string): string => {
    const timestamp = Date.now()
    const extension = utils.getFileExtension(originalName)
    const baseName = originalName.replace(/\.[^/.]+$/, '')
    
    if (suffix) {
      return `${baseName}_${suffix}_${timestamp}.${extension}`
    }
    return `${baseName}_${timestamp}.${extension}`
  }
}

// 错误处理
api.interceptors.response.use(
  response => response,
  error => {
    console.error('API请求错误:', error)
    
    if (error.response) {
      // 服务器返回错误状态码
      const { status, data } = error.response
      
      switch (status) {
        case 400:
          throw new Error(data.message || '请求参数错误')
        case 401:
          throw new Error('未授权访问')
        case 403:
          throw new Error('禁止访问')
        case 404:
          throw new Error('资源不存在')
        case 500:
          throw new Error(data.message || '服务器内部错误')
        default:
          throw new Error(data.message || `请求失败 (${status})`)
      }
    } else if (error.request) {
      // 网络错误
      throw new Error('网络连接失败，请检查网络设置')
    } else {
      // 其他错误
      throw new Error(error.message || '未知错误')
    }
  }
)

// 导出默认API对象 (兼容旧版本)
export default {
  files: fileApi,
  pdfTools: pdfToolsApi,
} 