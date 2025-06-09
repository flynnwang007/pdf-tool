#!/bin/bash

# PDF工具服务器部署脚本（不需要本地Java）
# 使用Docker多阶段构建

set -e

echo "🚀 开始服务器部署..."

# 检查Docker是否运行
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker未运行，请先安装并启动Docker"
    echo "安装命令："
    echo "curl -fsSL https://get.docker.com -o get-docker.sh"
    echo "sudo sh get-docker.sh"
    echo "sudo usermod -aG docker \$USER"
    echo "newgrp docker"
    exit 1
fi

# 检查是否在项目根目录
if [ ! -f "docker-compose.server.yml" ]; then
    echo "❌ 请在项目根目录运行此脚本"
    exit 1
fi

# 创建数据目录
echo "📁 创建数据目录..."
sudo mkdir -p /var/lib/pdf-app/{postgres,redis,uploads,temp}
sudo chown -R $USER:$USER /var/lib/pdf-app

# 创建环境变量文件（如果不存在）
if [ ! -f ".env" ]; then
    echo "⚙️ 创建环境变量配置..."
    cat > .env << 'EOF'
# 生产环境配置
DB_PASSWORD=your-strong-password-here
JWT_SECRET=your-super-secret-jwt-key-at-least-32-chars-long
API_BASE_URL=http://localhost:8080
FRONTEND_PORT=80
BACKEND_PORT=8080
EOF
    echo "⚠️  请编辑 .env 文件，修改密码和配置："
    echo "   nano .env"
    echo ""
fi

# 停止现有容器（如果存在）
echo "🛑 停止现有容器..."
docker-compose -f docker-compose.server.yml down

# 构建并启动容器
echo "🔨 构建并启动Docker容器（使用多阶段构建）..."
docker-compose -f docker-compose.server.yml up --build -d

# 等待服务启动
echo "⏳ 等待服务启动..."
sleep 30

# 检查服务状态
echo "🔍 检查服务状态..."
docker ps --filter "name=pdf-" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

# 测试服务
echo "🧪 测试服务..."
sleep 10

# 检查后端健康状态
for i in {1..10}; do
    if curl -s http://localhost:8080/actuator/health > /dev/null; then
        echo "✅ 后端服务正常运行"
        break
    else
        echo "⏳ 等待后端服务启动... ($i/10)"
        sleep 10
    fi
    
    if [ $i -eq 10 ]; then
        echo "❌ 后端服务启动失败"
        echo "查看日志："
        docker logs pdf-backend-prod --tail 20
        exit 1
    fi
done

# 检查前端服务
if curl -s http://localhost > /dev/null; then
    echo "✅ 前端服务正常运行"
else
    echo "❌ 前端服务启动失败"
    docker logs pdf-frontend-prod --tail 20
fi

echo ""
echo "🎉 部署成功！"
echo ""
echo "📱 访问地址："
echo "   前端应用: http://$(curl -s ifconfig.me)"
echo "   后端API:  http://$(curl -s ifconfig.me):8080"
echo "   API文档:  http://$(curl -s ifconfig.me):8080/swagger-ui.html"
echo ""
echo "🗄️ 数据库连接："
echo "   PostgreSQL: localhost:5432"
echo "   用户名: pdfuser"
echo "   密码: 见.env文件中的DB_PASSWORD"
echo "   数据库: pdfapp"
echo ""
echo "📊 Redis缓存："
echo "   地址: localhost:6379"
echo ""
echo "🔧 管理命令："
echo "   查看日志: docker logs <容器名>"
echo "   停止服务: docker-compose -f docker-compose.server.yml down"
echo "   重启服务: docker-compose -f docker-compose.server.yml restart"
echo ""
echo "📝 注意事项："
echo "   1. 请确保防火墙开放了80和8080端口"
echo "   2. 修改.env文件中的密码和配置"
echo "   3. 如果使用域名，请配置DNS解析"
echo "" 