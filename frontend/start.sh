#!/bin/bash

# PDF前端启动脚本（使用nohup）

cd /root/pdf-tool/frontend

# 创建日志目录
mkdir -p logs

# 检查是否已经在运行
if pgrep -f "npm run dev" > /dev/null; then
    echo "⚠️  前端服务已在运行中"
    echo "进程信息："
    ps aux | grep "npm run dev" | grep -v grep
    exit 1
fi

# 启动服务
echo "🚀 启动PDF前端服务..."
nohup npm run dev > logs/frontend.log 2>&1 &

# 获取进程ID
PID=$!
echo "✅ 前端服务已启动"
echo "📋 进程ID: $PID"
echo "📝 日志文件: logs/frontend.log"
echo "🌐 访问地址: http://your-server-ip:3000"

# 等待一下然后检查是否启动成功
sleep 3
if pgrep -f "npm run dev" > /dev/null; then
    echo "✅ 服务启动成功！"
    echo "💡 查看日志: tail -f logs/frontend.log"
    echo "💡 停止服务: ./stop.sh"
else
    echo "❌ 服务启动失败，请查看日志："
    cat logs/frontend.log
fi 