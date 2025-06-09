#!/bin/bash

# PDF 工具安全原生部署脚本 - 避免端口冲突

set -e

# 可配置的端口设置
NGINX_PORT=${NGINX_PORT:-80}
BACKEND_PORT=${BACKEND_PORT:-8080}
POSTGRES_PORT=${POSTGRES_PORT:-5432}
REDIS_PORT=${REDIS_PORT:-6379}

echo "🚀 开始 PDF 工具原生部署..."
echo "📊 使用端口配置:"
echo "  - Nginx (前端): $NGINX_PORT"
echo "  - 后端 API: $BACKEND_PORT" 
echo "  - PostgreSQL: $POSTGRES_PORT"
echo "  - Redis: $REDIS_PORT"
echo ""

# 检查端口是否被占用
echo "🔍 检查端口占用情况..."
for port in $NGINX_PORT $BACKEND_PORT $POSTGRES_PORT $REDIS_PORT; do
    if netstat -tuln | grep -q ":$port "; then
        echo "⚠️  警告: 端口 $port 已被占用"
        echo "当前占用情况:"
        netstat -tuln | grep ":$port "
        echo ""
        read -p "是否继续部署? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            echo "❌ 部署已取消"
            exit 1
        fi
    else
        echo "✅ 端口 $port 可用"
    fi
done

# 项目配置
PROJECT_DIR="/opt/pdf-tool"
BACKEND_DIR="$PROJECT_DIR/backend"
FRONTEND_DIR="$PROJECT_DIR/frontend"
NGINX_CONF="/etc/nginx/sites-available/pdf-tool"

echo ""
echo "📁 创建项目目录..."
sudo mkdir -p $PROJECT_DIR
sudo chown -R $USER:$USER $PROJECT_DIR

echo "📋 复制项目文件..."
cp -r ~/pdf-tool/* $PROJECT_DIR/

# 构建后端
echo "🔨 构建后端应用..."
cd $BACKEND_DIR
chmod +x gradlew
./gradlew bootJar --no-daemon

# 创建目录
sudo mkdir -p /var/log/pdf-tool
sudo mkdir -p /opt/pdf-tool/uploads
sudo chown -R $USER:$USER /var/log/pdf-tool
sudo chown -R $USER:$USER /opt/pdf-tool/uploads

# 构建前端
echo "🎨 构建前端应用..."
cd $FRONTEND_DIR
npm ci
npm run build

# 配置 Nginx (使用自定义端口)
echo "🌐 配置 Nginx..."
sudo tee $NGINX_CONF > /dev/null << EOF
server {
    listen $NGINX_PORT;
    server_name _;
    
    # 前端静态文件
    location / {
        root /opt/pdf-tool/frontend/dist;
        try_files \$uri \$uri/ /index.html;
        index index.html;
    }
    
    # 后端 API 代理
    location /api/ {
        proxy_pass http://localhost:$BACKEND_PORT/api/;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        
        # 文件上传大小限制
        client_max_body_size 100M;
    }
    
    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
        root /opt/pdf-tool/frontend/dist;
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}
EOF

# 检查 Nginx 配置是否已存在
if systemctl is-active --quiet nginx; then
    echo "📝 Nginx 已运行，添加新站点配置..."
    sudo ln -sf $NGINX_CONF /etc/nginx/sites-enabled/pdf-tool
else
    echo "🆕 安装并配置 Nginx..."
    # 如果需要安装 Nginx
    if ! command -v nginx &> /dev/null; then
        sudo apt update
        sudo apt install -y nginx
    fi
    sudo ln -sf $NGINX_CONF /etc/nginx/sites-enabled/pdf-tool
    sudo systemctl enable nginx
fi

# 测试 Nginx 配置
sudo nginx -t
sudo systemctl reload nginx

# 配置数据库 (如果需要自定义端口)
if [ "$POSTGRES_PORT" != "5432" ]; then
    echo "⚙️  配置 PostgreSQL 自定义端口..."
    sudo sed -i "s/#port = 5432/port = $POSTGRES_PORT/" /etc/postgresql/*/main/postgresql.conf
    sudo systemctl restart postgresql
fi

# 配置 Redis (如果需要自定义端口)  
if [ "$REDIS_PORT" != "6379" ]; then
    echo "⚙️  配置 Redis 自定义端口..."
    sudo sed -i "s/port 6379/port $REDIS_PORT/" /etc/redis/redis.conf
    sudo systemctl restart redis-server
fi

# 创建后端服务文件
echo "⚙️  创建系统服务..."
sudo tee /etc/systemd/system/pdf-tool-backend.service > /dev/null << EOF
[Unit]
Description=PDF Tool Backend Service
After=network.target postgresql.service redis-server.service

[Service]
Type=simple
User=root
WorkingDirectory=/opt/pdf-tool/backend
ExecStart=/usr/bin/java -jar -Xmx512m -Xms256m build/libs/pdf-tool-backend-1.0.0.jar
Restart=always
RestartSec=10

Environment=SPRING_PROFILES_ACTIVE=prod
Environment=SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:$POSTGRES_PORT/pdf_tool
Environment=SPRING_DATASOURCE_USERNAME=pdf_user
Environment=SPRING_DATASOURCE_PASSWORD=pdf_password_2024
Environment=SPRING_REDIS_HOST=localhost
Environment=SPRING_REDIS_PORT=$REDIS_PORT
Environment=SPRING_REDIS_PASSWORD=redis_password_2024
Environment=SERVER_PORT=$BACKEND_PORT
Environment=LOGGING_LEVEL_ROOT=INFO

StandardOutput=journal
StandardError=journal
SyslogIdentifier=pdf-tool-backend

[Install]
WantedBy=multi-user.target
EOF

# 重载并启动服务
sudo systemctl daemon-reload
sudo systemctl enable pdf-tool-backend
sudo systemctl start pdf-tool-backend

echo ""
echo "✅ 部署完成！"
echo ""
echo "📊 服务访问地址:"
echo "- 前端: http://localhost:$NGINX_PORT"
echo "- 后端 API: http://localhost:$BACKEND_PORT"
echo "- 数据库: localhost:$POSTGRES_PORT"
echo "- Redis: localhost:$REDIS_PORT"
echo ""
echo "🔍 检查服务状态:"
echo "sudo systemctl status pdf-tool-backend"
echo "sudo systemctl status nginx"
echo ""
echo "📝 查看日志:"
echo "sudo journalctl -u pdf-tool-backend -f" 