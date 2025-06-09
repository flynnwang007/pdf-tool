#!/bin/bash

# PDF工具Docker部署脚本
# 作者: Assistant
# 日期: $(date +%Y-%m-%d)

set -e

echo "🚀 开始部署PDF工具..."

# 检查Docker是否运行
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker未运行，请先启动Docker Desktop"
    exit 1
fi

# 检查是否在项目根目录
if [ ! -f "docker-compose.local.yml" ]; then
    echo "❌ 请在项目根目录运行此脚本"
    exit 1
fi

# 构建后端应用
echo "📦 构建后端应用..."
cd backend
./gradlew bootJar
cd ..

# 停止现有容器（如果存在）
echo "🛑 停止现有容器..."
docker-compose -f docker-compose.local.yml down

# 构建并启动容器
echo "🔨 构建并启动Docker容器..."
docker-compose -f docker-compose.local.yml up --build -d

# 等待服务启动
echo "⏳ 等待服务启动..."
sleep 10

# 检查服务状态
echo "🔍 检查服务状态..."
docker ps --filter "name=pdf-" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

# 测试服务
echo "🧪 测试服务..."
if curl -s http://localhost:8080/actuator/health > /dev/null; then
    echo "✅ 后端服务正常运行"
else
    echo "❌ 后端服务启动失败"
    docker logs pdf-backend-local --tail 20
    exit 1
fi

if curl -s http://localhost:3000 > /dev/null; then
    echo "✅ 前端服务正常运行"
else
    echo "❌ 前端服务启动失败"
    docker logs pdf-frontend-local --tail 20
    exit 1
fi

echo ""
echo "🎉 部署成功！"
echo ""
echo "📱 访问地址："
echo "   前端应用: http://localhost:3000"
echo "   后端API:  http://localhost:8080"
echo "   API文档:  http://localhost:8080/swagger-ui.html"
echo ""
echo "🗄️ 数据库连接："
echo "   PostgreSQL: localhost:5432"
echo "   用户名: pdfuser"
echo "   密码: pdfpassword"
echo "   数据库: pdfapp"
echo ""
echo "📊 Redis缓存："
echo "   地址: localhost:6379"
echo ""
echo "🔧 管理命令："
echo "   查看日志: docker logs <容器名>"
echo "   停止服务: docker-compose -f docker-compose.local.yml down"
echo "   重启服务: docker-compose -f docker-compose.local.yml restart"
echo "" 