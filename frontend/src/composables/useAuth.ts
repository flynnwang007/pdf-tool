import { ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

// 全局登录提示状态
const showLoginPrompt = ref(false)
const loginPromptMessage = ref('')

export function useAuth() {
  const authStore = useAuthStore()

  /**
   * 检查用户是否已登录，如果未登录则显示登录提示
   * @param message 自定义提示消息
   * @returns Promise<boolean> 是否已登录
   */
  const requireAuth = async (message?: string): Promise<boolean> => {
    if (authStore.isAuthenticated) {
      return true
    }

    // 设置提示消息并显示登录对话框
    loginPromptMessage.value = message || '此功能需要登录后才能使用，请先登录您的账户。'
    showLoginPrompt.value = true
    
    return false
  }

  /**
   * 快速检查登录状态，不显示提示
   */
  const isLoggedIn = (): boolean => {
    return authStore.isAuthenticated
  }

  /**
   * 获取当前用户信息
   */
  const getCurrentUser = () => {
    return authStore.user
  }

  /**
   * 获取用户token
   */
  const getToken = () => {
    return authStore.session?.access_token
  }

  /**
   * 关闭登录提示
   */
  const closeLoginPrompt = () => {
    showLoginPrompt.value = false
    loginPromptMessage.value = ''
  }

  /**
   * 显示需要登录的消息提示
   */
  const showAuthRequiredMessage = (message?: string) => {
    ElMessage.warning(message || '请先登录后再使用此功能')
  }

  return {
    // 状态
    showLoginPrompt,
    loginPromptMessage,
    
    // 方法
    requireAuth,
    isLoggedIn,
    getCurrentUser,
    getToken,
    closeLoginPrompt,
    showAuthRequiredMessage,
    
    // 从 authStore 导出的常用属性
    user: authStore.user,
    isAuthenticated: authStore.isAuthenticated,
    loading: authStore.loading
  }
} 