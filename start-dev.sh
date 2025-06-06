#!/bin/bash

# PDF工具应用 - 开发环境启动脚本

set -e

echo "🚀 启动PDF工具应用开发环境..."

# 检查Docker是否运行
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker未运行，请先启动Docker"
    exit 1
fi

# 启动基础服务
echo "📦 启动基础服务 (PostgreSQL, Redis, MinIO)..."
docker-compose -f docker-compose.dev.yml up -d

# 等待数据库启动
echo "⏳ 等待数据库启动..."
sleep 10

# 检查服务状态
echo "🔍 检查服务状态..."
docker-compose -f docker-compose.dev.yml ps

echo "✅ 基础服务启动完成！"
echo ""
echo "📊 服务访问地址："
echo "  PostgreSQL: localhost:5432"
echo "  Redis: localhost:6379"
echo "  MinIO Console: http://localhost:9001"
echo "    用户名: admin"
echo "    密码: admin123"
echo ""
echo "🛠️  接下来可以启动后端和前端服务："
echo "  后端: cd backend && ./gradlew bootRun"
echo "  前端: cd frontend && npm run dev"
echo ""
echo "🛑 停止服务: ./stop-dev.sh" 