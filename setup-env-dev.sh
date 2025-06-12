#!/bin/bash

# 本地开发环境变量设置脚本
echo "📝 设置本地开发环境变量..."

# 导出MemFireDB配置（保持不变，用于用户认证）
export VITE_SUPABASE_URL="https://d11558og91hm5619qfbg.baseapi.memfiredb.com"
export VITE_SUPABASE_ANON_KEY="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiYW5vbiIsImV4cCI6MzMyNTk3Njk5NSwiaWF0IjoxNzQ5MTc2OTk1LCJpc3MiOiJzdXBhYmFzZSJ9.fqRSc8fZxx5V8SgCWZME-eTuhc2A3bOIOE9iDympXWo"

# 导出JWT密钥 (MemFireDB的真实JWT密钥)
export MEMFIRE_JWT_SECRET="0d37e31b-3452-4949-8e19-04bc619c78c9"

# 本地开发配置
export NODE_ENV="development"
export VITE_API_BASE_URL="http://localhost:8080/api"
export SPRING_PROFILES_ACTIVE="dev"

echo "✅ 本地开发环境变量设置完成"
echo "🔑 JWT密钥长度: $(echo -n "$MEMFIRE_JWT_SECRET" | wc -c) 字符"
echo "🌐 MemFireDB URL: $VITE_SUPABASE_URL"
echo "🔧 开发模式: $NODE_ENV"
echo "🌐 API地址: $VITE_API_BASE_URL"

# 验证环境变量
if [ ${#MEMFIRE_JWT_SECRET} -eq 36 ]; then
    echo "✅ JWT密钥长度正确"
else
    echo "❌ JWT密钥长度错误，应为36字符，当前为: ${#MEMFIRE_JWT_SECRET}"
fi

echo "🚀 现在可以启动本地开发服务器了" 