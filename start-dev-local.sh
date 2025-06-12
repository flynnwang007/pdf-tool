#!/bin/bash

echo "🚀 启动本地开发环境..."

# 加载本地开发环境变量
source ./setup-env-dev.sh

# 检查端口是否被占用
if lsof -Pi :8080 -sTCP:LISTEN -t >/dev/null ; then
    echo "⚠️  端口8080已被占用，正在停止现有进程..."
    pkill -f "gradlew bootRun"
    sleep 2
fi

if lsof -Pi :3000 -sTCP:LISTEN -t >/dev/null ; then
    echo "⚠️  端口3000已被占用，正在停止现有进程..."
    pkill -f "npm run dev"
    sleep 2
fi

echo "🔧 启动后端服务器..."
cd backend
./gradlew bootRun > ../backend.log 2>&1 &
BACKEND_PID=$!
cd ..

echo "⏳ 等待后端启动..."
sleep 10

# 检查后端是否启动成功
if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "✅ 后端服务器启动成功"
else
    echo "❌ 后端服务器启动失败，请查看 backend.log"
    exit 1
fi

echo "🌐 启动前端服务器..."
cd frontend
npm run dev > ../frontend.log 2>&1 &
FRONTEND_PID=$!
cd ..

echo "📝 进程ID:"
echo "  后端: $BACKEND_PID"
echo "  前端: $FRONTEND_PID"

echo "🎉 开发环境启动完成！"
echo "📱 前端地址: http://localhost:3000"
echo "🔧 后端地址: http://localhost:8080"
echo "📊 后端健康检查: http://localhost:8080/actuator/health"
echo ""
echo "💡 使用 './stop-dev.sh' 停止所有服务"
echo "📋 日志文件: backend.log, frontend.log"

# 等待用户输入来停止服务
echo ""
echo "按 Ctrl+C 停止所有服务..."
wait 