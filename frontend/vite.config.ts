import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

export default defineConfig({
  plugins: [
    vue(),
    AutoImport({
      resolvers: [ElementPlusResolver()],
      imports: [
        'vue',
        'vue-router',
        'pinia',
        '@vueuse/core'
      ],
      dts: true
    }),
    Components({
      resolvers: [ElementPlusResolver()]
    })
  ],
  define: {
    // Vue 3 必需的变量
    __VUE_OPTIONS_API__: 'true',
    __VUE_PROD_DEVTOOLS__: 'false',
    __VUE_PROD_HYDRATION_MISMATCH_DETAILS__: 'false',
    
    // Vite 内部变量
    __VITE_IS_MODERN__: 'true',
    __VITE_LEGACY__: 'false',
    
    // 所有可能的 HMR 相关变量
    __DEFINES__: '{}',
    __HMR_CONFIG_NAME__: 'null',
    __HMR_PROTOCOL__: 'null', 
    __HMR_HOSTNAME__: 'null',
    __HMR_PORT__: 'null',
    __HMR_TIMEOUT__: 'null',
    __HMR_ENABLE_OVERLAY__: 'false',
    __HMR_BASE__: '"/"',
    __HMR_FALLBACK__: 'false',
    __HMR_DIRECT_TARGET__: 'null',
    
    // 基础路径变量
    __BASE__: '"/"',
    __SERVER_HOST__: 'null',
    __VITE_BASE__: '"/"',
    
    // 环境变量
    'process.env.NODE_ENV': '"production"',
    'process.env.BASE_URL': '"/"',
    'process.env.VITE_APP_ENV': '"production"',
    
    // 模块和环境标识
    'import.meta.hot': 'undefined',
    'import.meta.env.DEV': 'false',
    'import.meta.env.PROD': 'true',
    'import.meta.env.SSR': 'false',
    'import.meta.env.MODE': '"production"',
    'import.meta.env.BASE_URL': '"/"',
    
    // 全局对象
    global: 'globalThis'
  },
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    host: '0.0.0.0',
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  preview: {
    host: '0.0.0.0',
    port: 4173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  build: {
    outDir: 'dist',
    assetsDir: 'assets',
    sourcemap: false,
    // 使用esbuild进行代码压缩（更快）
    minify: 'esbuild',
    rollupOptions: {
      output: {
        manualChunks: {
          vendor: ['vue', 'vue-router', 'pinia'],
          elementPlus: ['element-plus', '@element-plus/icons-vue'],
          pdfjs: ['pdfjs-dist', 'vue-pdf-embed']
        }
      }
    }
  }
}) 