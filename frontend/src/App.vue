<template>
  <div id="app" class="awesome-app">
    <!-- 顶部状态栏 -->
    <div class="status-bar glassmorphism safe-area-top">
      <div class="status-content">
        <span class="app-title">
          <span class="title-icon">📄</span>
          <span class="title-text">PDF工具</span>
        </span>
        <!-- 用户头像组件 -->
        <div class="user-section">
          <UserAvatar />
        </div>
      </div>
    </div>

    <!-- 主内容区域 -->
    <div class="main-container">
      <router-view />
    </div>

    <!-- 底部导航栏 -->
    <div class="bottom-nav glassmorphism safe-area-bottom">
      <div class="nav-container">
        <div 
          class="nav-item"
          :class="{ active: $route.path === '/' }"
          @click="$router.push('/')"
        >
          <div class="nav-icon-wrapper">
            <el-icon :size="22"><House /></el-icon>
            <div class="nav-glow" v-if="$route.path === '/'"></div>
          </div>
          <span class="nav-label">首页</span>
        </div>
        <div 
          class="nav-item"
          :class="{ active: $route.path === '/files' }"
          @click="$router.push('/files')"
        >
          <div class="nav-icon-wrapper">
            <el-icon :size="22"><Folder /></el-icon>
            <div class="nav-glow" v-if="$route.path === '/files'"></div>
          </div>
          <span class="nav-label">文件</span>
        </div>
        <div 
          class="nav-item"
          :class="{ active: $route.path === '/tools' }"
          @click="$router.push('/tools')"
        >
          <div class="nav-icon-wrapper">
            <el-icon :size="22"><Tools /></el-icon>
            <div class="nav-glow" v-if="$route.path === '/tools'"></div>
          </div>
          <span class="nav-label">工具</span>
        </div>
        <div 
          class="nav-item upload-special"
          :class="{ active: $route.path === '/upload' }"
          @click="$router.push('/upload')"
        >
          <div class="nav-icon-wrapper special-upload">
            <el-icon :size="22"><Upload /></el-icon>
            <div class="upload-glow"></div>
          </div>
          <span class="nav-label">上传</span>
        </div>
      </div>
    </div>

    <!-- 全局登录提示对话框 -->
    <LoginPrompt 
      v-model="showLoginPrompt"
      :message="loginPromptMessage"
      @cancel="closeLoginPrompt"
    />
  </div>
</template>

<script setup lang="ts">
import { House, Upload, Folder, Tools } from '@element-plus/icons-vue'
import UserAvatar from '@/components/auth/UserAvatar.vue'
import LoginPrompt from '@/components/auth/LoginPrompt.vue'
import { useAuth } from '@/composables/useAuth'
import { ref } from 'vue'

const showLoginPrompt = ref(false)
const loginPromptMessage = ref('请先登录后再进行操作')
function closeLoginPrompt() {
  showLoginPrompt.value = false
}
</script>

<style scoped>
.awesome-app {
  height: 100vh;
  height: -webkit-fill-available;
  display: flex;
  flex-direction: column;
  background: #f6f6f6;
  overflow: hidden;
  position: relative;
  max-width: 100vw;
  overflow-x: hidden;
}

/* 毛玻璃效果 - 微信风格 */
.glassmorphism {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 0, 0, 0.05);
}

/* 安全区域适配 */
.safe-area-top {
  padding-top: env(safe-area-inset-top);
}

.safe-area-bottom {
  padding-bottom: env(safe-area-inset-bottom);
}

/* 顶部状态栏 */
.status-bar {
  color: #1a1a1a;
  padding: 12px 16px;
  flex-shrink: 0;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.status-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 414px;
  margin: 0 auto;
}

.app-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  letter-spacing: 0.3px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.title-icon {
  font-size: 20px;
  animation: title-float 3s ease-in-out infinite;
}

.title-text {
  color: #07c160;
  font-weight: 700;
}

.user-section {
  display: flex;
  align-items: center;
}

@keyframes title-float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-1px); }
}

/* 主容器 */
.main-container {
  flex: 1;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  position: relative;
  background: #f6f6f6;
}

/* 底部导航栏 - 精简高度 */
.bottom-nav {
  flex-shrink: 0;
  box-shadow: 0 -1px 3px rgba(0, 0, 0, 0.1);
  position: sticky;
  bottom: 0;
  z-index: 100;
}

.nav-container {
  display: flex;
  padding: 6px 8px 8px;
  max-width: 414px;
  margin: 0 auto;
  justify-content: space-around;
  position: relative;
}

.nav-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 4px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 8px;
  position: relative;
  /* 确保触摸区域足够大 */
  min-width: 50px;
  min-height: 44px;
  justify-content: center;
  -webkit-tap-highlight-color: transparent;
  user-select: none;
}

.nav-icon-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.nav-glow {
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  background: radial-gradient(circle, rgba(7, 193, 96, 0.2) 0%, transparent 70%);
  border-radius: 10px;
  z-index: -1;
  animation: nav-glow-pulse 2s ease-in-out infinite;
}

@keyframes nav-glow-pulse {
  0%, 100% { opacity: 0.6; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.05); }
}

/* 上传按钮特殊样式 */
.upload-special .nav-icon-wrapper.special-upload {
  background: #07c160;
  box-shadow: 0 2px 8px rgba(7, 193, 96, 0.3);
  color: white;
}

.upload-glow {
  position: absolute;
  top: -3px;
  left: -3px;
  right: -3px;
  bottom: -3px;
  background: radial-gradient(circle, rgba(7, 193, 96, 0.3) 0%, transparent 70%);
  border-radius: 11px;
  z-index: -1;
  animation: upload-glow-pulse 2s ease-in-out infinite;
}

@keyframes upload-glow-pulse {
  0%, 100% { opacity: 0.8; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.1); }
}

.nav-item:active {
  transform: scale(0.96);
}

.nav-item.active {
  color: #07c160;
}

.nav-item.active .nav-icon-wrapper {
  background: rgba(7, 193, 96, 0.1);
  transform: translateY(-1px);
}

.nav-item:not(.active) {
  color: #666666;
}

.nav-item:not(.active):hover {
  color: #333333;
}

.nav-label {
  font-size: 10px;
  margin-top: 2px;
  line-height: 1;
  font-weight: 500;
  white-space: nowrap;
  transition: all 0.3s ease;
}

.nav-item.active .nav-label {
  color: #07c160;
  font-weight: 600;
}

/* 移动端专用样式 */
@media (max-width: 414px) {
  .status-bar {
    padding: 10px 16px;
  }
  
  .app-title {
    font-size: 17px;
  }
  
  .title-icon {
    font-size: 18px;
  }
  
  .nav-container {
    padding: 5px 6px 6px;
  }
  
  .nav-item {
    padding: 3px 2px;
    min-height: 40px;
  }
  
  .nav-icon-wrapper {
    width: 28px;
    height: 28px;
  }
  
  .nav-label {
    font-size: 9px;
    margin-top: 1px;
  }
}

/* 超小屏幕优化 */
@media (max-width: 375px) {
  .nav-item {
    padding: 2px 1px;
    min-height: 38px;
  }
  
  .nav-icon-wrapper {
    width: 26px;
    height: 26px;
  }
  
  .nav-label {
    font-size: 8px;
  }
}

/* 大屏幕时居中显示 */
@media (min-width: 768px) {
  .awesome-app {
    max-width: 414px;
    margin: 0 auto;
    box-shadow: 0 0 20px rgba(0, 0, 0, 0.1);
  }
}

/* 触摸反馈优化 */
.nav-item {
  -webkit-user-select: none;
  -moz-user-select: none;
  user-select: none;
}

/* 滚动条优化 */
.main-container::-webkit-scrollbar {
  width: 0;
  background: transparent;
}

/* iOS安全区域优化 */
@supports (padding: max(0px)) {
  .safe-area-top {
    padding-top: max(12px, env(safe-area-inset-top));
  }
  
  .safe-area-bottom {
    padding-bottom: max(6px, env(safe-area-inset-bottom));
  }
}
</style>

<style>
/* 微信风格的 MessageBox */
.el-message-box {
  border-radius: 16px !important;
  box-shadow: 0 8px 32px rgba(0,0,0,0.12) !important;
  max-width: 90vw;
  min-width: 260px;
  padding: 0 0 16px 0 !important;
}
.el-message-box__header {
  padding: 24px 24px 0 24px !important;
  text-align: center;
}
.el-message-box__title {
  font-size: 18px !important;
  font-weight: 600;
  color: #1a1a1a;
  text-align: center;
}
.el-message-box__content {
  font-size: 16px !important;
  color: #333;
  padding: 20px 24px 0 24px !important;
  text-align: center;
}
.el-message-box__btns {
  justify-content: center !important;
  padding: 16px 24px 0 24px !important;
}
.el-button--primary {
  background: #07c160 !important;
  border-color: #07c160 !important;
  border-radius: 24px !important;
  font-size: 16px !important;
  min-width: 96px;
}
.el-button--primary:hover {
  background: #06ad56 !important;
  border-color: #06ad56 !important;
}
/* 顶部安全区适配，防止 WebView 顶到刘海/电池 */
html, body, #app {
  padding-top: env(safe-area-inset-top) !important;
  padding-top: constant(safe-area-inset-top) !important;
}
</style> 