#!/bin/bash

# 前端部署脚本

echo "🚀 开始前端部署..."

# 检查Node.js版本
echo "📋 检查环境..."
node --version
npm --version

# 安装依赖
echo "📦 安装依赖..."
npm ci --production=false

# 构建项目
echo "🔨 构建项目..."
npm run build

# 检查构建结果
if [ -d "dist" ]; then
    echo "✅ 构建成功！"
    echo "📁 构建产物："
    ls -la dist/
else
    echo "❌ 构建失败！"
    exit 1
fi

# 如果是Docker环境，构建镜像
if [ "$1" == "docker" ]; then
    echo "🐳 构建Docker镜像..."
    docker build -t pdf-tool-frontend .
    echo "✅ Docker镜像构建完成！"
fi

echo "🎉 部署完成！" 