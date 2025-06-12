<template>
  <el-dialog
    v-model="visible"
    title="需要登录"
    width="400px"
    :close-on-click-modal="false"
    center
    class="login-prompt-dialog"
  >
    <div class="login-prompt-content">
      <div class="prompt-icon">🔐</div>
      <div class="prompt-text">
        <h3 class="prompt-title">登录后继续使用</h3>
        <p class="prompt-message">{{ message || '此功能需要登录后才能使用，请先登录您的账户。' }}</p>
      </div>
    </div>
    
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleLogin">去登录</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'

interface Props {
  modelValue: boolean
  message?: string
  redirectPath?: string
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'confirm'): void
  (e: 'cancel'): void
}

const props = withDefaults(defineProps<Props>(), {
  message: '',
  redirectPath: ''
})

const emit = defineEmits<Emits>()

const router = useRouter()

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const handleLogin = () => {
  visible.value = false
  emit('confirm')
  
  // 构建登录URL，包含回跳参数
  const currentPath = props.redirectPath || router.currentRoute.value.fullPath
  router.push({
    name: 'Login',
    query: { redirect: currentPath }
  })
}

const handleCancel = () => {
  visible.value = false
  emit('cancel')
}
</script>

<style scoped>
.login-prompt-dialog :deep(.el-dialog) {
  border-radius: 12px;
  overflow: hidden;
}

.login-prompt-dialog :deep(.el-dialog__header) {
  padding: 24px 24px 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.login-prompt-dialog :deep(.el-dialog__title) {
  color: white;
  font-weight: 600;
}

.login-prompt-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: white;
}

.login-prompt-content {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px;
}

.prompt-icon {
  font-size: 48px;
  flex-shrink: 0;
}

.prompt-text {
  flex: 1;
}

.prompt-title {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
}

.prompt-message {
  margin: 0;
  font-size: 14px;
  color: #666666;
  line-height: 1.5;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 0 24px 24px;
}

/* 移动端适配 */
@media (max-width: 480px) {
  .login-prompt-dialog :deep(.el-dialog) {
    width: 90% !important;
    margin: 5vh auto;
  }
  
  .prompt-icon {
    font-size: 36px;
  }
  
  .prompt-title {
    font-size: 16px;
  }
  
  .prompt-message {
    font-size: 13px;
  }
  
  .dialog-footer {
    flex-direction: column;
    gap: 8px;
  }
  
  .dialog-footer .el-button {
    width: 100%;
    margin: 0;
  }
}
</style> 