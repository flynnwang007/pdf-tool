<template>
  <div class="forgot-password-container">
    <el-card class="forgot-password-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <h2>重置密码</h2>
          <p class="card-description">输入您的邮箱地址，我们将发送重置密码链接</p>
        </div>
      </template>

      <el-form
        ref="forgotPasswordFormRef"
        :model="forgotPasswordForm"
        :rules="rules"
        label-position="top"
        @submit.prevent="handleResetPassword"
      >
        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="forgotPasswordForm.email"
            type="email"
            placeholder="请输入您注册时使用的邮箱"
            size="large"
            :disabled="authStore.loading"
            @keyup.enter="handleResetPassword"
          >
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
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
            @click="handleResetPassword"
          >
            {{ authStore.loading ? '发送中...' : '发送重置链接' }}
          </el-button>
        </el-form-item>

        <el-form-item class="back-link">
          <div class="text-center">
            <router-link to="/auth/login" class="link">
              返回登录
            </router-link>
          </div>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 发送成功对话框 -->
    <el-dialog
      v-model="showSuccessDialog"
      title="邮件已发送"
      width="400px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
      center
    >
      <div class="success-content">
        <el-icon size="60" color="#67c23a" class="success-icon">
          <CircleCheck />
        </el-icon>
        <p class="success-message">重置密码链接已发送到您的邮箱</p>
        <p class="instruction">
          请检查您的邮箱（包括垃圾邮件箱），点击链接完成密码重置。
        </p>
        <p class="email-sent-to">
          邮件已发送至：<strong>{{ forgotPasswordForm.email }}</strong>
        </p>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="resendEmail" :loading="resending">
            重新发送
          </el-button>
          <el-button type="primary" @click="goToLogin">
            返回登录
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { User, CircleCheck } from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()

const forgotPasswordFormRef = ref<FormInstance>()
const showSuccessDialog = ref(false)
const resending = ref(false)

const forgotPasswordForm = reactive({
  email: ''
})

const rules: FormRules = {
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

const handleResetPassword = async () => {
  if (!forgotPasswordFormRef.value) return

  const valid = await forgotPasswordFormRef.value.validate().catch(() => false)
  if (!valid) return

  authStore.clearError()

  const result = await authStore.resetPassword(forgotPasswordForm.email)
  
  if (result.success) {
    showSuccessDialog.value = true
  } else {
    ElMessage.error(result.error || '发送重置邮件失败')
  }
}

const resendEmail = async () => {
  resending.value = true
  const result = await authStore.resetPassword(forgotPasswordForm.email)
  
  if (result.success) {
    ElMessage.success('重置邮件已重新发送')
  } else {
    ElMessage.error(result.error || '重新发送失败')
  }
  
  resending.value = false
}

const goToLogin = () => {
  showSuccessDialog.value = false
  router.push('/auth/login')
}

onMounted(() => {
  authStore.clearError()
})
</script>

<style scoped>
.forgot-password-container {
  display: flex;
  min-height: 100vh;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  position: relative;
  overflow: hidden;
}

.forgot-password-container::before {
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

.forgot-password-container::after {
  content: '';
  position: absolute;
  bottom: 20%;
  right: 15%;
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, rgba(195, 207, 226, 0.2) 0%, transparent 70%);
  border-radius: 50%;
  animation: pulse 6s ease-in-out infinite;
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

.forgot-password-card {
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
  line-height: 1.4;
}

.error-item {
  margin-bottom: 16px;
}

.back-link {
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
  transition: color 0.3s;
  font-weight: 500;
}

.link:hover {
  color: #5a67d8;
  text-decoration: underline;
}

.success-content {
  text-align: center;
  padding: 20px 0;
}

.success-icon {
  margin-bottom: 16px;
}

.success-message {
  font-size: 16px;
  font-weight: 500;
  color: #374151;
  margin: 0 0 12px 0;
}

.instruction {
  font-size: 14px;
  color: #6b7280;
  margin: 0 0 12px 0;
  line-height: 1.5;
}

.email-sent-to {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

.dialog-footer {
  display: flex;
  gap: 8px;
  justify-content: center;
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

:deep(.el-dialog__body) {
  padding: 10px 20px 20px 20px;
}

/* 移动端适配 */
@media (max-width: 480px) {
  .forgot-password-container {
    padding: 16px;
  }
  
  .forgot-password-card {
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