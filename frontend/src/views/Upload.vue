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
138|
...
    font-size: 15px;
  }
  
  /* 微信风格的 MessageBox */
  :deep(.el-message-box) {
    border-radius: 16px !important;
    box-shadow: 0 8px 32px rgba(0,0,0,0.12) !important;
    max-width: 90vw;
    min-width: 260px;
    padding: 0 0 16px 0 !important;
  }
  :deep(.el-message-box__header) {
    padding: 24px 24px 0 24px !important;
    text-align: center;
  }
  :deep(.el-message-box__title) {
    font-size: 18px !important;
    font-weight: 600;
    color: #1a1a1a;
    text-align: center;
  }
  :deep(.el-message-box__content) {
    font-size: 16px !important;
    color: #333;
    padding: 20px 24px 0 24px !important;
    text-align: center;
  }
  :deep(.el-message-box__btns) {
    justify-content: center !important;
    padding: 16px 24px 0 24px !important;
  }
  :deep(.el-button--primary) {
    background: #07c160 !important;
    border-color: #07c160 !important;
    border-radius: 24px !important;
    font-size: 16px !important;
    min-width: 96px;
  }
  :deep(.el-button--primary):hover {
    background: #06ad56 !important;
    border-color: #06ad56 !important;
  }
  </style> 
