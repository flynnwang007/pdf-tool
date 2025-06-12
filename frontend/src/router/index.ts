import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: {
      title: '首页'
    }
  },
  {
    path: '/upload',
    name: 'Upload',
    component: () => import('@/views/Upload.vue'),
    meta: {
      title: '文件上传'
    }
  },
  {
    path: '/files',
    name: 'Files',
    component: () => import('@/views/Files.vue'),
    meta: {
      title: '文件管理'
    }
  },
  {
    path: '/tools',
    name: 'Tools',
    component: () => import('@/views/Tools.vue'),
    meta: {
      title: 'PDF工具'
    }
  },
  // 认证相关路由
  {
    path: '/auth',
    name: 'Auth',
    redirect: '/auth/login',
    meta: {
      title: '认证',
      requiresGuest: true
    },
    children: [
      {
        path: 'login',
        name: 'Login',
        component: () => import('@/views/auth/LoginView.vue'),
        meta: {
          title: '登录',
          requiresGuest: true
        }
      },
      {
        path: 'signup',
        name: 'Signup',
        component: () => import('@/views/auth/SignupView.vue'),
        meta: {
          title: '注册',
          requiresGuest: true
        }
      },
      {
        path: 'forgot-password',
        name: 'ForgotPassword',
        component: () => import('@/views/auth/ForgotPasswordView.vue'),
        meta: {
          title: '忘记密码',
          requiresGuest: true
        }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: {
      title: '页面不存在'
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()
  
  // 如果认证状态还在初始化中，等待初始化完成
  if (authStore.loading && !authStore.user && !authStore.session) {
    await authStore.initialize()
  }
  
  // 设置页面标题
  if (to.meta?.title) {
    document.title = `${to.meta.title} - PDF工具`
  }
  
  // 检查是否需要认证（现在只有少数页面需要强制登录）
  if (to.meta?.requiresAuth && !authStore.isAuthenticated) {
    // 保存用户想要访问的页面，登录后跳转
    next({
      name: 'Login',
      query: { redirect: to.fullPath }
    })
    return
  }
  
  // 检查是否需要游客状态（已登录用户不能访问登录/注册页面）
  if (to.meta?.requiresGuest && authStore.isAuthenticated) {
    next({ name: 'Home' })
    return
  }
  
  next()
})

export default router 