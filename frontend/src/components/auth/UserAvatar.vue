<template>
  <div class="user-avatar-container">
    <!-- 未登录状态 -->
    <div v-if="!authStore.isAuthenticated" class="auth-buttons">
      <el-button size="small" @click="goToLogin">
        登录
      </el-button>
      <el-button type="primary" size="small" @click="goToSignup">
        注册
      </el-button>
    </div>

    <!-- 已登录状态 -->
    <el-dropdown v-else trigger="click" @command="handleCommand">
      <div class="user-info">
        <el-avatar :size="32" :src="avatarUrl">
          <el-icon><User /></el-icon>
        </el-avatar>
        <span class="username">{{ displayName }}</span>
        <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
      </div>
      
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item command="profile">
            <el-icon><User /></el-icon>
            个人资料
          </el-dropdown-item>
          <el-dropdown-item command="settings">
            <el-icon><Setting /></el-icon>
            设置
          </el-dropdown-item>
          <el-dropdown-item divided command="logout">
            <el-icon><SwitchButton /></el-icon>
            退出登录
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, ArrowDown, Setting, SwitchButton } from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()

// 计算用户显示名称
const displayName = computed(() => {
  if (!authStore.user) return ''
  
  // 优先显示用户元数据中的名称
  if (authStore.user.user_metadata?.full_name) {
    return authStore.user.user_metadata.full_name
  }
  
  // 其次显示邮箱的用户名部分
  if (authStore.userEmail) {
    return authStore.userEmail.split('@')[0]
  }
  
  return '用户'
})

// 头像URL（可以从用户元数据获取）
const avatarUrl = computed(() => {
  return authStore.user?.user_metadata?.avatar_url || ''
})

const goToLogin = () => {
  router.push('/auth/login')
}

const goToSignup = () => {
  router.push('/auth/signup')
}

const handleCommand = async (command: string) => {
  switch (command) {
    case 'profile':
      // 跳转到个人资料页面
      router.push('/profile')
      break
      
    case 'settings':
      // 跳转到设置页面
      router.push('/settings')
      break
      
    case 'logout':
      await handleLogout()
      break
  }
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要退出登录吗？',
      '确认退出',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const result = await authStore.signOut()
    
    if (result.success) {
      ElMessage.success('已成功退出登录')
      router.push('/')
    } else {
      ElMessage.error(result.error || '退出登录失败')
    }
  } catch {
    // 用户取消操作
  }
}
</script>

<style scoped>
.user-avatar-container {
  display: flex;
  align-items: center;
}

.auth-buttons {
  display: flex;
  gap: 8px;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 8px;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: var(--el-fill-color-light);
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: var(--el-text-color-primary);
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dropdown-icon {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  transition: transform 0.3s;
}

.user-info:hover .dropdown-icon {
  transform: rotate(180deg);
}

:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 8px;
}

:deep(.el-dropdown-menu__item .el-icon) {
  font-size: 14px;
}
</style> 