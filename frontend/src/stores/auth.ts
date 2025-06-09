import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { supabase } from '@/lib/supabase'
import type { User, Session, AuthError } from '@supabase/supabase-js'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(null)
  const session = ref<Session | null>(null)
  const loading = ref(true)
  const error = ref<string | null>(null)

  // 计算属性
  const isAuthenticated = computed(() => !!user.value)
  const userEmail = computed(() => user.value?.email)

  // 初始化认证状态
  const initialize = async () => {
    try {
      loading.value = true
      const { data: { session: currentSession } } = await supabase.auth.getSession()
      
      if (currentSession) {
        session.value = currentSession
        user.value = currentSession.user
      }

      // 监听认证状态变化
      supabase.auth.onAuthStateChange((event, currentSession) => {
        session.value = currentSession
        user.value = currentSession?.user ?? null
        
        if (event === 'SIGNED_OUT') {
          clearUser()
        }
      })
    } catch (err) {
      console.error('初始化认证状态失败:', err)
      error.value = err instanceof Error ? err.message : '未知错误'
    } finally {
      loading.value = false
    }
  }

  // 登录
  const signIn = async (email: string, password: string) => {
    try {
      loading.value = true
      error.value = null
      
      const { data, error: authError } = await supabase.auth.signInWithPassword({
        email,
        password
      })

      if (authError) {
        throw authError
      }

      if (data.user && data.session) {
        user.value = data.user
        session.value = data.session
      }

      return { success: true }
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : '登录失败'
      error.value = errorMessage
      return { success: false, error: errorMessage }
    } finally {
      loading.value = false
    }
  }

  // 注册
  const signUp = async (email: string, password: string) => {
    try {
      loading.value = true
      error.value = null
      
      const { data, error: authError } = await supabase.auth.signUp({
        email,
        password,
        options: {
          emailRedirectTo: `${window.location.origin}/auth/verify`
        }
      })

      if (authError) {
        throw authError
      }

      return { 
        success: true, 
        needsVerification: !data.session,
        message: data.session ? '注册成功！' : '注册成功！请检查邮箱并验证您的账户。'
      }
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : '注册失败'
      error.value = errorMessage
      return { success: false, error: errorMessage }
    } finally {
      loading.value = false
    }
  }

  // 登出
  const signOut = async () => {
    try {
      loading.value = true
      const { error: authError } = await supabase.auth.signOut()
      
      if (authError) {
        throw authError
      }

      clearUser()
      return { success: true }
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : '登出失败'
      error.value = errorMessage
      return { success: false, error: errorMessage }
    } finally {
      loading.value = false
    }
  }

  // 重置密码
  const resetPassword = async (email: string) => {
    try {
      loading.value = true
      error.value = null
      
      const { error: authError } = await supabase.auth.resetPasswordForEmail(email, {
        redirectTo: `${window.location.origin}/auth/reset-password`
      })

      if (authError) {
        throw authError
      }

      return { success: true, message: '重置密码链接已发送到您的邮箱' }
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : '重置密码失败'
      error.value = errorMessage
      return { success: false, error: errorMessage }
    } finally {
      loading.value = false
    }
  }

  // 更新密码
  const updatePassword = async (password: string) => {
    try {
      loading.value = true
      error.value = null
      
      const { error: authError } = await supabase.auth.updateUser({
        password
      })

      if (authError) {
        throw authError
      }

      return { success: true, message: '密码更新成功' }
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : '密码更新失败'
      error.value = errorMessage
      return { success: false, error: errorMessage }
    } finally {
      loading.value = false
    }
  }

  // 清除用户数据
  const clearUser = () => {
    user.value = null
    session.value = null
    error.value = null
  }

  // 清除错误
  const clearError = () => {
    error.value = null
  }

  return {
    // 状态
    user,
    session,
    loading,
    error,
    
    // 计算属性
    isAuthenticated,
    userEmail,
    
    // 方法
    initialize,
    signIn,
    signUp,
    signOut,
    resetPassword,
    updatePassword,
    clearUser,
    clearError
  }
}) 