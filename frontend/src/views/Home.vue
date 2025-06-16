<template>
  <div class="home-page">
    <!-- Hero区域 -->
    <div class="hero-section">
      <div class="hero-card">
        <div class="hero-content">
          <div class="logo-container">
            <div class="logo-wrapper">
              <span class="logo-icon">📄</span>
            </div>
          </div>
          <h1 class="hero-title">PDF工具集</h1>
          <p class="hero-subtitle">一站式PDF处理解决方案</p>
          <div class="hero-buttons">
            <el-button 
              type="primary" 
              size="large" 
              class="hero-btn primary-btn"
              @click="$router.push('/upload')"
            >
              <el-icon><Upload /></el-icon>
              立即上传
            </el-button>
            <el-button 
              size="large" 
              class="hero-btn secondary-btn"
              @click="$router.push('/files')"
            >
              <el-icon><Folder /></el-icon>
              我的文件
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 功能特性区域 -->
    <div class="features-section">
      <div class="section-header">
        <h2 class="section-title">
          <span class="title-icon">⚡</span>
          功能特性
        </h2>
      </div>
      <div class="features-grid">
        <div 
          class="feature-card"
          v-for="(feature, index) in features"
          :key="index"
          :style="{ animationDelay: `${index * 100}ms` }"
          @click="navigateToFeature(feature.path)"
        >
          <div class="feature-icon-wrapper">
            <span class="feature-icon" :style="{ backgroundColor: feature.color }">
              {{ feature.icon }}
            </span>
          </div>
          <h3 class="feature-title">{{ feature.title }}</h3>
          <p class="feature-description">{{ feature.description }}</p>
          <div class="feature-arrow">
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>
      </div>
    </div>

    <!-- 统计信息区域 -->
    <div class="stats-section">
      <div class="stats-grid">
        <div 
          class="stat-card"
          v-for="(stat, index) in stats"
          :key="index"
          :style="{ animationDelay: `${index * 150}ms` }"
        >
          <div class="stat-content">
            <div class="stat-number">{{ stat.number }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Upload, Folder, ArrowRight } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const router = useRouter()

interface Feature {
  icon: string
  title: string
  description: string
  color: string
  path: string
}

interface Stat {
  number: string
  label: string
}

const features: Feature[] = [
  {
    icon: '🔄',
    title: '格式转换',
    description: 'PDF与其他格式互转',
    color: '#07c160',
    path: '/tools'
  },
  {
    icon: '✂️',
    title: '页面分割',
    description: '按需拆分PDF页面',
    color: '#1890ff',
    path: '/tools'
  },
  {
    icon: '📄',
    title: '页面合并',
    description: '多个PDF合并成一个',
    color: '#52c41a',
    path: '/tools'
  },
  {
    icon: '🔒',
    title: '加密解密',
    description: '保护PDF文件安全',
    color: '#ff4d4f',
    path: '/tools'
  }
]

const stats: Stat[] = [
  {
    number: '10K+',
    label: '处理文件'
  },
  {
    number: '99.9%',
    label: '成功率'
  },
  {
    number: '5K+',
    label: '活跃用户'
  }
]

const navigateToFeature = (path: string) => {
  router.push(path)
}
</script>

<style scoped>
.home-page {
  min-height: 100%;
  background: #f6f6f6;
  padding: env(safe-area-inset-top) 0 0 0;
  padding: constant(safe-area-inset-top) 0 0 0;
  -webkit-overflow-scrolling: touch;
}

/* Hero 区域 */
.hero-section {
  padding: 20px 16px 32px;
  background: #f6f6f6;
}

.hero-card {
  background: white;
  border-radius: 12px;
  padding: 32px 24px;
  text-align: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(0, 0, 0, 0.05);
  animation: slide-up 0.8s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.hero-content {
  position: relative;
  z-index: 2;
}

.logo-container {
  margin-bottom: 24px;
}

.logo-wrapper {
  display: inline-block;
  position: relative;
}

.logo-icon {
  font-size: 48px;
  display: block;
  animation: logo-float 3s ease-in-out infinite;
  filter: drop-shadow(0 4px 8px rgba(7, 193, 96, 0.2));
}

@keyframes logo-float {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  50% { transform: translateY(-6px) rotate(1deg); }
}

.hero-title {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 12px 0;
  letter-spacing: 0.5px;
}

.hero-subtitle {
  font-size: 16px;
  color: #666666;
  margin: 0 0 32px 0;
  line-height: 1.5;
  font-weight: 400;
}

.hero-buttons {
  display: flex;
  gap: 12px;
  justify-content: center;
  flex-wrap: wrap;
}

.hero-btn {
  border-radius: 8px;
  padding: 12px 24px;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.3s ease;
  border: none;
  position: relative;
  overflow: hidden;
}

.primary-btn {
  background: #07c160;
  color: white;
  box-shadow: 0 2px 8px rgba(7, 193, 96, 0.3);
}

.primary-btn:hover {
  background: #06ad56;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(7, 193, 96, 0.4);
}

.secondary-btn {
  background: white;
  color: #1a1a1a;
  border: 1px solid #d9d9d9;
}

.secondary-btn:hover {
  background: #f5f5f5;
  border-color: #07c160;
  color: #07c160;
  transform: translateY(-1px);
}

/* 功能特性区域 */
.features-section {
  padding: 0 16px 32px;
}

.section-header {
  margin-bottom: 20px;
}

.section-title {
  font-size: 20px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-icon {
  font-size: 22px;
  animation: title-glow 2s ease-in-out infinite;
}

@keyframes title-glow {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.feature-card {
  background: white;
  border-radius: 12px;
  padding: 20px 16px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(0, 0, 0, 0.05);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  animation: slide-up 0.6s cubic-bezier(0.4, 0, 0.2, 1) both;
  position: relative;
  overflow: hidden;
}

.feature-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  border-color: rgba(7, 193, 96, 0.3);
}

.feature-card:active {
  transform: translateY(0px);
}

.feature-icon-wrapper {
  margin-bottom: 12px;
}

.feature-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: white;
  font-weight: bold;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  position: relative;
  overflow: hidden;
}

.feature-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px 0;
}

.feature-description {
  font-size: 13px;
  color: #666666;
  margin: 0 0 12px 0;
  line-height: 1.4;
}

.feature-arrow {
  color: #07c160;
  opacity: 0;
  transform: translateX(-10px);
  transition: all 0.3s ease;
}

.feature-card:hover .feature-arrow {
  opacity: 1;
  transform: translateX(0);
}

/* 统计信息区域 */
.stats-section {
  padding: 0 16px 32px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 20px 16px;
  text-align: center;
  border: 1px solid rgba(0, 0, 0, 0.05);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  animation: slide-up 0.6s cubic-bezier(0.4, 0, 0.2, 1) both;
  position: relative;
  overflow: hidden;
}

.stat-content {
  position: relative;
  z-index: 2;
}

.stat-number {
  font-size: 20px;
  font-weight: 700;
  color: #07c160;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  color: #666666;
  font-weight: 500;
}

/* 动画定义 */
@keyframes slide-up {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 移动端响应式 */
@media (max-width: 414px) {
  .hero-section {
    padding: 16px 12px 24px;
  }
  
  .hero-card {
    padding: 24px 20px;
  }
  
  .logo-icon {
    font-size: 40px;
  }
  
  .hero-title {
    font-size: 24px;
  }
  
  .hero-subtitle {
    font-size: 14px;
  }
  
  .hero-buttons {
    gap: 8px;
  }
  
  .hero-btn {
    padding: 10px 20px;
    font-size: 13px;
  }
  
  .features-section {
    padding: 0 12px 24px;
  }
  
  .section-title {
    font-size: 18px;
  }
  
  .feature-card {
    padding: 16px 12px;
  }
  
  .feature-icon {
    width: 36px;
    height: 36px;
    font-size: 16px;
  }
  
  .feature-title {
    font-size: 14px;
  }
  
  .feature-description {
    font-size: 12px;
  }
  
  .stats-section {
    padding: 0 12px 24px;
  }
  
  .stat-card {
    padding: 16px 12px;
  }
  
  .stat-number {
    font-size: 18px;
  }
  
  .stat-label {
    font-size: 12px;
  }
}

/* 超小屏优化 */
@media (max-width: 375px) {
  .features-grid {
    gap: 8px;
  }
  
  .stats-grid {
    gap: 8px;
  }
  
  .hero-title {
    font-size: 22px;
  }
  
  .feature-icon {
    width: 32px;
    height: 32px;
    font-size: 14px;
  }
}

/* 触摸优化 */
.feature-card,
.hero-btn {
  -webkit-tap-highlight-color: transparent;
  user-select: none;
}

/* 滚动优化 */
.home-page {
  -webkit-overflow-scrolling: touch;
}
</style> 