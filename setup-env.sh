#!/bin/bash

# 环境变量设置脚本
echo "📝 设置环境变量..."

# 导出MemFireDB配置
export VITE_SUPABASE_URL="https://d11558og91hm5619qfbg.baseapi.memfiredb.com"
export VITE_SUPABASE_ANON_KEY="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiYW5vbiIsImV4cCI6MzMyNTk3Njk5NSwiaWF0IjoxNzQ5MTc2OTk1LCJpc3MiOiJzdXBhYmFzZSJ9.fqRSc8fZxx5V8SgCWZME-eTuhc2A3bOIOE9iDympXWo"

# 导出JWT密钥 (MemFireDB的真实JWT密钥)
export MEMFIRE_JWT_SECRET="0d37e31b-3452-4949-8e19-04bc619c78c9"

# 应用配置
export NODE_ENV="production"
export VITE_API_BASE_URL="http://www.aibase123.cn:8080/api"

echo "✅ 环境变量设置完成"
echo "🔑 JWT密钥长度: $(echo -n "$MEMFIRE_JWT_SECRET" | wc -c) 字符"
echo "🌐 MemFireDB URL: $VITE_SUPABASE_URL"

# 验证环境变量
if [ ${#MEMFIRE_JWT_SECRET} -eq 36 ]; then
    echo "✅ JWT密钥长度正确"
else
    echo "❌ JWT密钥长度错误，应为36字符，当前为: ${#MEMFIRE_JWT_SECRET}"
fi 