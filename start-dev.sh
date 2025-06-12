#!/bin/bash

# PDF工具应用 - 开发环境启动脚本

set -e

echo "🚀 启动本地开发环境..."

# 加载.env文件到环境变量
if [ -f .env ]; then
    echo "📝 加载 .env 文件..."
    export $(grep -v '^#' .env | xargs)
    echo "✅ 环境变量已加载"
    echo "🔑 MEMFIRE_JWT_SECRET: ${MEMFIRE_JWT_SECRET:0:8}..."
    echo "🌐 API地址: $VITE_API_BASE_URL"
else
    echo "❌ .env 文件不存在"
    exit 1
fi

# 停止可能运行的进程
echo "🛑 停止现有进程..."
pkill -f "gradlew bootRun" 2>/dev/null || true
pkill -f "npm run dev" 2>/dev/null || true
sleep 2

# 启动后端
echo "🔧 启动后端服务器..."
cd backend
./gradlew bootRun &
BACKEND_PID=$!
cd ..

# 等待后端启动
echo "⏳ 等待后端启动..."
for i in {1..30}; do
    if curl -f http://localhost:8080/actuator/health >/dev/null 2>&1; then
        echo "✅ 后端服务器启动成功 (${i}s)"
        break
    fi
    if [ $i -eq 30 ]; then
        echo "❌ 后端启动超时"
        kill $BACKEND_PID 2>/dev/null || true
        exit 1
    fi
    sleep 1
done

# 启动前端
echo "🌐 启动前端服务器..."
cd frontend
npm run dev &
FRONTEND_PID=$!
cd ..

echo ""
echo "🎉 开发环境启动完成！"
echo "📱 前端地址: http://localhost:3000"
echo "🔧 后端地址: http://localhost:8080"
echo "📊 健康检查: http://localhost:8080/actuator/health"
echo ""
echo "📝 进程PID:"
echo "  后端: $BACKEND_PID"
echo "  前端: $FRONTEND_PID"
echo ""
echo "💡 按 Ctrl+C 停止服务"

# 创建停止函数
cleanup() {
    echo ""
    echo "🛑 正在停止服务..."
    kill $BACKEND_PID $FRONTEND_PID 2>/dev/null || true
    exit 0
}

# 捕获中断信号
trap cleanup INT TERM

# 等待进程
wait 