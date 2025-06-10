# Supabase配置说明

## ⚠️ 当前问题

项目目前使用开发环境的Supabase配置在生产环境运行，这存在安全风险：

### 当前配置：
- **前端URL**: `https://d11558og91hm5619qfbg.baseapi.memfiredb.com`
- **前端KEY**: `eyJhbGciOiJIUzI1NiIs...` (硬编码)
- **后端认证**: 临时简化验证，未真正验证JWT

## 🔧 正确的生产环境配置

### 1. 创建生产环境Supabase项目

1. 访问 [Supabase Dashboard](https://app.supabase.com)
2. 创建新项目（独立于开发环境）
3. 获取生产环境的URL和密钥

### 2. 配置生产环境变量

#### 前端配置（在`frontend/deploy-simple.sh`中）：
```bash
# 替换为你的生产环境配置
export VITE_SUPABASE_URL="https://your-production-project.supabase.co"
export VITE_SUPABASE_ANON_KEY="your-production-anon-key"
```

#### 后端配置（在`backend/deploy-backend.sh`中）：
```bash
# 替换为你的生产环境JWT密钥
export SUPABASE_JWT_SECRET="your-production-jwt-secret"
```

### 3. 数据库迁移

如果有用户数据需要迁移：
```sql
-- 导出开发环境用户数据
-- 导入到生产环境
```

## 🚨 安全建议

1. **立即创建独立的生产Supabase项目**
2. **启用真正的JWT验证**（目前后端只做了简单验证）
3. **设置适当的RLS政策**
4. **定期轮换密钥**
5. **监控异常访问**

## 🔄 当前临时方案

为了保持功能正常，项目临时使用开发环境配置：
- 前端：在构建时通过环境变量注入
- 后端：简化的认证验证

**这只是临时方案，生产环境必须使用独立配置！** 