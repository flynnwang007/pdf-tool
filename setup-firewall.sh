#!/bin/bash

# 防火墙配置脚本

echo "🔧 配置防火墙..."

# 检查ufw是否安装
if ! command -v ufw &> /dev/null; then
    echo "安装ufw..."
    sudo apt-get update
    sudo apt-get install ufw -y
fi

# 配置防火墙规则
echo "配置防火墙规则..."

# 重置防火墙规则
sudo ufw --force reset

# 设置默认策略
sudo ufw default deny incoming
sudo ufw default allow outgoing

# 允许SSH连接
sudo ufw allow ssh
sudo ufw allow 22

# 允许HTTP和HTTPS
sudo ufw allow 80
sudo ufw allow 443

# 允许后端API端口（可选，如果需要直接访问）
sudo ufw allow 8080

# 启用防火墙
sudo ufw --force enable

# 显示状态
echo "防火墙状态："
sudo ufw status numbered

echo "✅ 防火墙配置完成" 