#!/bin/bash

# PDF工具应用 - 开发环境停止脚本

echo "🛑 停止PDF工具应用开发环境..."

# 停止并删除容器
docker-compose -f docker-compose.dev.yml down

echo "✅ 开发环境已停止"
echo ""
echo "💡 如需完全清理（包括数据卷）："
echo "   docker-compose -f docker-compose.dev.yml down -v"
echo "   docker system prune -f" 