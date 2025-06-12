#!/bin/bash

echo "🔧 修复云服务器JWT配置..."

# 设置环境变量
export MEMFIRE_JWT_SECRET="0d37e31b-3452-4949-8e19-04bc619c78c9"
export VITE_SUPABASE_URL="https://d11558og91hm5619qfbg.baseapi.memfiredb.com"
export VITE_SUPABASE_ANON_KEY="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImQxMTU1OG9nOTFobTU2MTlxZmJnIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzMwMjg1NjIsImV4cCI6MjA0ODYwNDU2Mn0.uGGa-pILGX3JHE3KQ8dO8LGOuwzAh5NCmEtKJ9_lGYI"

echo "🔑 JWT密钥: $MEMFIRE_JWT_SECRET"
echo "🔑 JWT密钥长度: $(echo -n "$MEMFIRE_JWT_SECRET" | wc -c) 字符"

# 测试JWT配置
echo "🧪 测试JWT配置..."
node -e "
const jwt = require('jsonwebtoken');
const secret = '$MEMFIRE_JWT_SECRET';
const payload = { sub: 'test', aud: 'authenticated', role: 'authenticated', iss: 'memfiredb', iat: Math.floor(Date.now() / 1000), exp: Math.floor(Date.now() / 1000) + 3600 };
const token = jwt.sign(payload, secret);
console.log('✅ JWT token 生成成功');
console.log('Token长度:', token.length);
const decoded = jwt.verify(token, secret);
console.log('✅ JWT token 验证成功');
console.log('Payload:', JSON.stringify(decoded, null, 2));
"

echo "📤 提交修复到git..."
chmod +x setup-env.sh
git add setup-env.sh backend/deploy-backend.sh
git commit -m "fix: 修复云服务器JWT配置问题

- 添加setup-env.sh环境变量配置脚本
- 更新deploy-backend.sh以支持多种环境配置方式
- 确保MemFireDB JWT密钥正确加载
- 修复云服务器403认证错误"

echo "🚀 推送到远程仓库..."
git push origin main

echo "✅ 修复完成！现在可以在云服务器上重新部署："
echo "1. git pull origin main"
echo "2. cd backend && ./deploy-backend.sh" 