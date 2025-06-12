#!/bin/bash

# PDF工具应用 - 开发环境停止脚本

echo "🛑 停止PDF工具应用开发环境..."

# 停止原生服务进程
pkill -f 'npm run dev' 2>/dev/null || true
pkill -f 'gradlew bootRun' 2>/dev/null || true
pkill -f 'pdf-tool' 2>/dev/null || true

echo "✅ 开发环境已停止"
echo ""
echo "💡 如需完全清理（包括数据卷）："
echo "   docker-compose -f docker-compose.dev.yml down -v"
echo "   docker system prune -f" 