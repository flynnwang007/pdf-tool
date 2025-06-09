<template>
  <div class="login-form-container">
    <el-card class="login-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <h2>登录</h2>
          <p class="card-description">输入您的邮箱和密码以登录您的账户</p>
        </div>
      </template>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="rules"
        label-position="top"
        @submit.prevent="handleLogin"
      >
        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="loginForm.email"
            type="email"
            placeholder="请输入您的邮箱"
            size="large"
            :disabled="authStore.loading"
            @keyup.enter="handleLogin"
          >
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入您的密码"
            size="large"
            show-password
            :disabled="authStore.loading"
            @keyup.enter="handleLogin"
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
          <div class="forgot-password">
            <router-link to="/auth/forgot-password" class="forgot-link">
              忘记密码？
            </router-link>
          </div>
        </el-form-item>

        <el-form-item v-if="authStore.error" class="error-item">
          <el-alert
            :title="authStore.error"
            type="error"
            :closable="false"
            show-icon
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            style="width: 100%"
            :loading="authStore.loading"
            @click="handleLogin"
          >
            {{ authStore.loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>

        <el-form-item class="signup-link">
          <div class="text-center">
            <span>还没有账户？</span>
            <router-link to="/auth/signup" class="link">
              立即注册
            </router-link>
          </div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()

const loginFormRef = ref<FormInstance>()
const loginForm = reactive({
  email: '',
  password: ''
})

const rules: FormRules = {
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return

  const valid = await loginFormRef.value.validate().catch(() => false)
  if (!valid) return

  authStore.clearError()

  const result = await authStore.signIn(loginForm.email, loginForm.password)
  
  if (result.success) {
    ElMessage.success('登录成功！')
    // 跳转到主页或用户之前想访问的页面
    const redirectTo = router.currentRoute.value.query.redirect as string || '/'
    router.push(redirectTo)
  } else {
    ElMessage.error(result.error || '登录失败')
  }
}

onMounted(() => {
  authStore.clearError()
})
</script>

<style scoped>
.login-form-container {
  display: flex;
  min-height: 100vh;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  position: relative;
  overflow: hidden;
}

.login-form-container::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: url('data:image/svg+xml,<svg width="100" height="100" xmlns="http://www.w3.org/2000/svg"><defs><pattern id="grid" width="100" height="100" patternUnits="userSpaceOnUse"><path d="M 100 0 L 0 0 0 100" fill="none" stroke="rgba(255,255,255,0.1)" stroke-width="1"/></pattern></defs><rect width="100%" height="100%" fill="url(%23grid)"/></svg>');
  animation: float 20s ease-in-out infinite;
  pointer-events: none;
}

.login-form-container::after {
  content: '';
  position: absolute;
  top: 10%;
  right: 10%;
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(64, 158, 255, 0.1) 0%, transparent 70%);
  border-radius: 50%;
  animation: pulse 4s ease-in-out infinite;
  pointer-events: none;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) rotate(0deg); }
  25% { transform: translate(-1%, -1%) rotate(1deg); }
  50% { transform: translate(1%, -0.5%) rotate(-1deg); }
  75% { transform: translate(-0.5%, 1%) rotate(0.5deg); }
}

@keyframes pulse {
  0%, 100% { opacity: 0.3; transform: scale(1); }
  50% { opacity: 0.1; transform: scale(1.1); }
}

.login-card {
  width: 100%;
  max-width: 400px;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1), 0 8px 20px rgba(0, 0, 0, 0.06);
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.95);
  border: 1px solid rgba(255, 255, 255, 0.2);
  position: relative;
  z-index: 10;
}

.card-header {
  text-align: center;
  margin-bottom: 8px;
}

.card-header h2 {
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.card-description {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
}

.forgot-password {
  display: flex;
  justify-content: flex-end;
  margin-top: 4px;
}

.forgot-link {
  color: #667eea;
  text-decoration: none;
  font-size: 12px;
  transition: color 0.3s;
}

.forgot-link:hover {
  color: #5a67d8;
  text-decoration: underline;
}

.error-item {
  margin-bottom: 16px;
}

.signup-link {
  text-align: center;
  margin-bottom: 0;
}

.text-center {
  font-size: 14px;
  color: #6b7280;
}

.link {
  color: #667eea;
  text-decoration: none;
  margin-left: 4px;
  transition: color 0.3s;
  font-weight: 500;
}

.link:hover {
  color: #5a67d8;
  text-decoration: underline;
}

:deep(.el-form-item__label) {
  font-weight: 600;
  color: #374151;
  margin-bottom: 6px;
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 12px;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.3);
  transition: all 0.3s ease;
}

:deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 12px 30px rgba(102, 126, 234, 0.4);
}

:deep(.el-input__wrapper) {
  border-radius: 12px;
  height: 48px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid #e5e7eb;
  transition: all 0.3s ease;
}

:deep(.el-input__wrapper:hover) {
  border-color: #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.1);
}

:deep(.el-input__wrapper.is-focus) {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

:deep(.el-card__body) {
  padding: 32px;
}

:deep(.el-card__header) {
  padding: 32px 32px 0;
}

/* 移动端适配 */
@media (max-width: 480px) {
  .login-form-container {
    padding: 16px;
  }
  
  .login-card {
    border-radius: 12px;
  }
  
  .card-header h2 {
    font-size: 24px;
  }
  
  :deep(.el-card__body) {
    padding: 24px;
  }
  
  :deep(.el-card__header) {
    padding: 24px 24px 0;
  }
}
</style> 