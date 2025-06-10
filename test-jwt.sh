#!/bin/bash

echo "🔐 JWT验证本地测试"
echo "=================="

# 检查后端服务是否运行
echo "📡 检查后端服务状态..."
if ! curl -s http://localhost:8080/actuator/health > /dev/null; then
    echo "❌ 后端服务未运行，请先启动后端服务："
    echo "   cd backend && ./gradlew bootRun"
    exit 1
fi

echo "✅ 后端服务正在运行"

# 测试1: 没有JWT的请求（应该失败）
echo -e "\n🧪 测试1: 没有JWT的请求（应该返回401）"
curl -s -w "HTTP状态: %{http_code}\n" http://localhost:8080/api/files

# 测试2: 无效JWT的请求（应该失败）
echo -e "\n🧪 测试2: 无效JWT的请求（应该返回401）"
curl -s -w "HTTP状态: %{http_code}\n" \
  -H "Authorization: Bearer invalid.jwt.token" \
  http://localhost:8080/api/files

# 测试3: 使用你的真实MemFireDB JWT（需要从前端获取）
echo -e "\n🧪 测试3: 真实JWT验证"
echo "请从浏览器控制台获取真实JWT令牌，或者使用下面的示例："
echo ""
echo "1. 打开浏览器到你的前端应用"
echo "2. 登录你的MemFireDB账户"
echo "3. 在控制台运行: localStorage.getItem('supabase.auth.token')"
echo "4. 复制JWT令牌并运行:"
echo ""
echo "export JWT_TOKEN='你的JWT令牌'"
echo "curl -H \"Authorization: Bearer \$JWT_TOKEN\" http://localhost:8080/api/files"
echo ""

# 如果有JWT_TOKEN环境变量，则测试
if [ -n "$JWT_TOKEN" ]; then
    echo "🔑 使用提供的JWT令牌测试..."
    curl -s -w "HTTP状态: %{http_code}\n" \
      -H "Authorization: Bearer $JWT_TOKEN" \
      -H "Content-Type: application/json" \
      http://localhost:8080/api/files
else
    echo "💡 提示: 设置 JWT_TOKEN 环境变量来自动测试真实JWT"
fi

echo -e "\n📋 检查后端日志以查看JWT验证详情:"
echo "   tail -f backend/logs/app.log" 