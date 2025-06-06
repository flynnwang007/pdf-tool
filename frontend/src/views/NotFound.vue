<template>
  <div class="mobile-not-found">
    <div class="error-container">
      <!-- 错误图标 -->
      <div class="error-icon">
        <el-icon :size="120" color="#E6A23C">
          <WarnTriangleFilled />
        </el-icon>
      </div>
      
      <!-- 错误信息 -->
      <div class="error-content">
        <h1 class="error-code">404</h1>
        <h2 class="error-title">页面走丢了</h2>
        <p class="error-description">
          抱歉，您访问的页面不存在或已被移除。
          <br>
          请检查网址是否正确，或返回主页继续使用。
        </p>
      </div>
      
      <!-- 操作按钮 -->
      <div class="error-actions">
        <el-button 
          type="primary" 
          size="large"
          class="action-button"
          @click="goHome"
        >
          <el-icon><HomeFilled /></el-icon>
          返回主页
        </el-button>
        
        <el-button 
          size="large"
          class="action-button"
          @click="goBack"
        >
          <el-icon><Back /></el-icon>
          返回上页
        </el-button>
      </div>
      
      <!-- 快速链接 -->
      <div class="quick-links">
        <h3 class="links-title">您可能需要：</h3>
        <div class="links-grid">
          <div 
            class="link-item"
            @click="navigateTo('/upload')"
          >
            <el-icon :size="24" color="#409EFF">
              <Upload />
            </el-icon>
            <span>上传文件</span>
          </div>
          
          <div 
            class="link-item"
            @click="navigateTo('/files')"
          >
            <el-icon :size="24" color="#67C23A">
              <Folder />
            </el-icon>
            <span>我的文件</span>
          </div>
          
          <div 
            class="link-item"
            @click="navigateTo('/tools')"
          >
            <el-icon :size="24" color="#E6A23C">
              <Tools />
            </el-icon>
            <span>PDF工具</span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 底部提示 -->
    <div class="error-footer">
      <p class="footer-text">
        如果您认为这是一个错误，请
        <a href="mailto:support@example.com" class="contact-link">联系我们</a>
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import {
  WarnTriangleFilled,
  HomeFilled,
  Back,
  Upload,
  Folder,
  Tools
} from '@element-plus/icons-vue'

const router = useRouter()

// 返回主页
const goHome = () => {
  router.push('/')
}

// 返回上一页
const goBack = () => {
  if (window.history.length > 1) {
    router.go(-1)
  } else {
    router.push('/')
  }
}

// 导航到指定页面
const navigateTo = (path: string) => {
  router.push(path)
}
</script>

<style scoped>
.mobile-not-found {
  min-height: 100vh;
  background: #fafbfc;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  text-align: center;
}

.error-container {
  max-width: 400px;
  width: 100%;
}

/* 错误图标 */
.error-icon {
  margin-bottom: 24px;
  animation: bounce 2s infinite ease-in-out;
}

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% {
    transform: translateY(0);
  }
  40% {
    transform: translateY(-10px);
  }
  60% {
    transform: translateY(-5px);
  }
}

/* 错误内容 */
.error-content {
  margin-bottom: 32px;
}

.error-code {
  font-size: 72px;
  font-weight: 700;
  color: #E6A23C;
  margin: 0 0 12px 0;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
}

.error-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px 0;
}

.error-description {
  font-size: 16px;
  color: #606266;
  line-height: 1.5;
  margin: 0;
}

/* 操作按钮 */
.error-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 32px;
}

.action-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border-radius: 12px;
}

/* 快速链接 */
.quick-links {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  margin-bottom: 24px;
}

.links-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px 0;
}

.links-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.link-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 8px;
  background: #f8f9fa;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.link-item:active {
  transform: scale(0.95);
  background: #e9ecef;
}

.link-item span {
  font-size: 12px;
  color: #606266;
  font-weight: 500;
}

/* 底部提示 */
.error-footer {
  margin-top: auto;
}

.footer-text {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.contact-link {
  color: #409EFF;
  text-decoration: none;
  font-weight: 500;
}

.contact-link:hover {
  text-decoration: underline;
}

/* 响应式优化 */
@media (max-width: 375px) {
  .mobile-not-found {
    padding: 16px;
  }
  
  .error-code {
    font-size: 64px;
  }
  
  .error-title {
    font-size: 20px;
  }
  
  .error-description {
    font-size: 14px;
  }
  
  .quick-links {
    padding: 16px;
  }
  
  .links-grid {
    gap: 12px;
  }
  
  .link-item {
    padding: 12px 8px;
  }
}

/* 深色模式 */
@media (prefers-color-scheme: dark) {
  .mobile-not-found {
    background: linear-gradient(135deg, #1a1a1a 0%, #2d2d2d 100%);
  }
  
  .error-title,
  .links-title {
    color: #e5e5e5;
  }
  
  .error-description,
  .footer-text {
    color: #a0a0a0;
  }
  
  .quick-links {
    background: #2d2d2d;
  }
  
  .link-item {
    background: #1a1a1a;
  }
  
  .link-item:active {
    background: #404040;
  }
  
  .link-item span {
    color: #a0a0a0;
  }
}

/* 平板适配 */
@media (min-width: 768px) {
  .error-container {
    max-width: 500px;
  }
  
  .error-actions {
    flex-direction: row;
    gap: 16px;
  }
  
  .action-button {
    flex: 1;
  }
  
  .links-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;
  }
}
</style> 