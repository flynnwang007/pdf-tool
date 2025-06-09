#!/bin/bash

# 在服务器上导入 Docker 镜像的脚本

echo "开始导入 Docker 镜像..."

# 检查镜像文件是否存在
if [ ! -d "docker-images" ]; then
    echo "错误: docker-images 目录不存在"
    exit 1
fi

# 导入镜像
echo "导入后端镜像..."
docker load -i docker-images/pdf-backend.tar

echo "导入前端镜像..."
docker load -i docker-images/pdf-frontend.tar

echo "导入数据库镜像..."
docker load -i docker-images/postgres.tar

echo "导入缓存镜像..."
docker load -i docker-images/redis.tar

echo "所有镜像导入完成！"
echo ""
echo "查看导入的镜像:"
docker images | grep -E "(pdf_tool|postgres|redis)"

echo ""
echo "现在可以启动服务:"
echo "docker-compose -f docker-compose.server.yml up -d" 