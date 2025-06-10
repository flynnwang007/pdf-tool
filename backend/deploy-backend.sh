#!/bin/bash

# 后端部署脚本
set -e

echo "🚀 开始部署后端服务..."

# 停止现有服务
echo "⏹️ 停止现有服务..."
pkill -f "java.*pdf-tool" || true

# 创建必要的目录
echo "📁 创建必要目录..."
mkdir -p uploads
mkdir -p logs
chmod 755 uploads logs

# 清理旧构建
echo "🧹 清理旧构建..."
./gradlew clean

# 构建项目
echo "🔨 构建项目..."
./gradlew build -x test

# 检查JAR文件
JAR_FILE=$(find build/libs -name "*.jar" -not -name "*plain.jar" | head -1)
if [ ! -f "$JAR_FILE" ]; then
    echo "❌ 构建失败：找不到JAR文件"
    exit 1
fi

echo "✅ 构建成功：$JAR_FILE"

# 加载环境变量
if [ -f "../.env" ]; then
    echo "📝 加载.env文件..."
    set -a  # 自动export所有变量
    source ../.env
    set +a
    echo "✅ 环境变量已加载"
else
    echo "⚠️  警告: 未找到.env文件，使用默认配置"
fi

# 启动服务
echo "🚀 启动后端服务..."

# 验证必要的环境变量
if [ -n "$MEMFIRE_JWT_SECRET" ]; then
    export SUPABASE_JWT_SECRET="$MEMFIRE_JWT_SECRET"
    echo "✅ 使用MemFireDB JWT配置"
else
    echo "⚠️  警告: 未找到MemFireDB配置，使用默认值"
    export SUPABASE_JWT_SECRET="your-supabase-jwt-secret-here"
fi

nohup java -jar "$JAR_FILE" \
    --spring.profiles.active=prod \
    --server.port=8080 \
    --app.file.upload-path=/root/pdf-tool/backend/uploads \
    --logging.file.name=/root/pdf-tool/backend/logs/app.log \
    > logs/nohup.log 2>&1 &

# 等待服务启动
echo "⏳ 等待服务启动..."
sleep 10

# 检查服务状态
if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "✅ 后端服务启动成功！"
    echo "🌐 服务地址: http://14.103.200.105:8080"
    echo "📊 健康检查: http://14.103.200.105:8080/actuator/health"
else
    echo "❌ 后端服务启动失败，请检查日志："
    echo "📝 应用日志: tail -f logs/app.log"
    echo "📝 启动日志: tail -f logs/nohup.log"
    exit 1
fi 