#!/bin/bash

# 上传 Docker 镜像到服务器的脚本
# 使用方法: ./upload-images.sh [服务器IP] [用户名]

SERVER_IP=${1:-"your-server-ip"}
USERNAME=${2:-"root"}

echo "开始上传 Docker 镜像到服务器..."
echo "服务器: $USERNAME@$SERVER_IP"

# 创建远程目录
ssh $USERNAME@$SERVER_IP "mkdir -p ~/pdf-tool/docker-images"

# 上传镜像文件
echo "上传后端镜像..."
scp docker-images/pdf-backend.tar $USERNAME@$SERVER_IP:~/pdf-tool/docker-images/

echo "上传前端镜像..."
scp docker-images/pdf-frontend.tar $USERNAME@$SERVER_IP:~/pdf-tool/docker-images/

echo "上传数据库镜像..."
scp docker-images/postgres.tar $USERNAME@$SERVER_IP:~/pdf-tool/docker-images/

echo "上传缓存镜像..."
scp docker-images/redis.tar $USERNAME@$SERVER_IP:~/pdf-tool/docker-images/

echo "所有镜像上传完成！"
echo ""
echo "接下来在服务器上执行以下命令导入镜像："
echo "cd ~/pdf-tool"
echo "./load-images.sh" 