#!/bin/bash

# PDF前端停止脚本

echo "🛑 停止PDF前端服务..."

# 查找并杀掉npm run dev进程
PIDS=$(pgrep -f "npm run dev")

if [ -z "$PIDS" ]; then
    echo "⚠️  没有找到运行中的前端服务"
else
    echo "📋 找到进程: $PIDS"
    
    # 杀掉进程
    pkill -f "npm run dev"
    
    # 等待一下
    sleep 2
    
    # 检查是否停止成功
    if pgrep -f "npm run dev" > /dev/null; then
        echo "⚠️  进程可能还在运行，强制杀掉..."
        pkill -9 -f "npm run dev"
    fi
    
    echo "✅ 前端服务已停止"
fi

# 显示当前状态
echo "📊 当前Node.js进程："
ps aux | grep node | grep -v grep || echo "没有Node.js进程在运行" 