import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'

// Element Plus
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// 移动端弹窗样式优化
import './assets/mobile-dialog.css'

// 全局移动端弹窗样式覆盖
const mobileDialogStyles = `
  <style>
    /* 强制覆盖Element Plus弹窗样式 - 移动端优化 */
    @media (max-width: 768px) {
      .el-dialog {
        margin: 5vh auto !important;
        width: calc(100vw - 16px) !important;
        max-width: calc(100vw - 16px) !important;
        min-width: auto !important;
      }
      
      body .el-dialog {
        margin: 5vh auto !important;
        width: calc(100vw - 16px) !important;
        max-width: calc(100vw - 16px) !important;
        min-width: auto !important;
      }
      
      html .el-dialog {
        margin: 5vh auto !important;
        width: calc(100vw - 16px) !important;
        max-width: calc(100vw - 16px) !important;
        min-width: auto !important;
      }
      
      .tool-dialog .el-dialog,
      .progress-dialog .el-dialog,
      .result-dialog .el-dialog {
        margin: 5vh auto !important;
        width: calc(100vw - 16px) !important;
        max-width: calc(100vw - 16px) !important;
        min-width: auto !important;
      }
      
      /* 弹窗内容优化 */
      .el-dialog__header {
        padding: 12px 16px 8px !important;
      }
      
      .el-dialog__body {
        padding: 8px 16px !important;
        max-height: calc(90vh - 120px) !important;
        overflow-y: auto !important;
      }
      
      .el-dialog__footer {
        padding: 8px 16px 16px !important;
      }
      
      .el-dialog__title {
        font-size: 16px !important;
        line-height: 1.4 !important;
      }
    }
    
    @media (max-width: 414px) {
      .el-dialog {
        margin: 2vh auto !important;
        width: calc(100vw - 8px) !important;
        max-width: calc(100vw - 8px) !important;
        min-width: auto !important;
      }
      
      body .el-dialog {
        margin: 2vh auto !important;
        width: calc(100vw - 8px) !important;
        max-width: calc(100vw - 8px) !important;
        min-width: auto !important;
      }
      
      html .el-dialog {
        margin: 2vh auto !important;
        width: calc(100vw - 8px) !important;
        max-width: calc(100vw - 8px) !important;
        min-width: auto !important;
      }
      
      .tool-dialog .el-dialog,
      .progress-dialog .el-dialog,
      .result-dialog .el-dialog {
        margin: 2vh auto !important;
        width: calc(100vw - 8px) !important;
        max-width: calc(100vw - 8px) !important;
        min-width: auto !important;
      }
      
      .el-dialog__header {
        padding: 8px 12px 6px !important;
      }
      
      .el-dialog__body {
        padding: 6px 12px !important;
        max-height: calc(95vh - 100px) !important;
      }
      
      .el-dialog__footer {
        padding: 6px 12px 12px !important;
      }
      
      .el-dialog__title {
        font-size: 14px !important;
      }
    }
  </style>
`;

// 注入全局样式
document.head.insertAdjacentHTML('beforeend', mobileDialogStyles);

const app = createApp(App)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 配置Element Plus移动端优化
app.use(ElementPlus, {
  // 移动端优化配置
  size: 'default',
  zIndex: 3000,
})

app.use(createPinia())
app.use(router)

// 等待DOM加载完成后挂载应用
document.addEventListener('DOMContentLoaded', () => {
  app.mount('#app')
  
  // 添加移动端优化的CSS类
  document.body.classList.add('mobile-optimized')
  
  // 移除初始加载状态
  setTimeout(() => {
    document.body.classList.add('loaded')
  }, 500)
})

// 处理路由错误
router.onError((error: any) => {
  console.error('Router error:', error)
})

// 添加全局错误处理
app.config.errorHandler = (err: any, vm: any, info: any) => {
  console.error('Global error:', err, info)
}

// 移动端特有的优化
if ('ontouchstart' in window) {
  // 添加触摸设备标识
  document.documentElement.classList.add('touch-device')
  
  // 禁用长按菜单
  document.addEventListener('contextmenu', (e) => {
    e.preventDefault()
  })
}

// PWA支持检测
if ('serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    // 可以在这里注册Service Worker
    console.log('PWA ready for service worker registration')
  })
} 