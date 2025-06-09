#!/bin/bash

# PDF 工具原生部署脚本

set -e

echo "🚀 开始 PDF 工具原生部署..."

# 项目配置
PROJECT_DIR="/opt/pdf-tool"
BACKEND_DIR="$PROJECT_DIR/backend"
FRONTEND_DIR="$PROJECT_DIR/frontend"
NGINX_CONF="/etc/nginx/sites-available/pdf-tool"

# 创建项目目录
echo "📁 创建项目目录..."
sudo mkdir -p $PROJECT_DIR
sudo chown -R $USER:$USER $PROJECT_DIR

# 复制项目文件
echo "📋 复制项目文件..."
cp -r ~/pdf-tool/* $PROJECT_DIR/

# 构建后端
echo "🔨 构建后端应用..."
cd $BACKEND_DIR
chmod +x gradlew
./gradlew bootJar --no-daemon

# 创建后端服务目录
sudo mkdir -p /var/log/pdf-tool
sudo mkdir -p /opt/pdf-tool/uploads
sudo chown -R $USER:$USER /var/log/pdf-tool
sudo chown -R $USER:$USER /opt/pdf-tool/uploads

# 构建前端
echo "🎨 构建前端应用..."
cd $FRONTEND_DIR
npm ci
npm run build

# 配置 Nginx
echo "🌐 配置 Nginx..."
sudo tee $NGINX_CONF > /dev/null << 'EOF'
server {
    listen 80;
    server_name _;
    
    # 前端静态文件
    location / {
        root /opt/pdf-tool/frontend/dist;
        try_files $uri $uri/ /index.html;
        index index.html;
    }
    
    # 后端 API 代理
    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
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

# 启用站点
sudo ln -sf $NGINX_CONF /etc/nginx/sites-enabled/
sudo rm -f /etc/nginx/sites-enabled/default
sudo nginx -t
sudo systemctl reload nginx

# 创建后端服务文件
echo "⚙️  创建系统服务..."
sudo tee /etc/systemd/system/pdf-tool-backend.service > /dev/null << 'EOF'
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
Environment=SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/pdf_tool
Environment=SPRING_DATASOURCE_USERNAME=pdf_user
Environment=SPRING_DATASOURCE_PASSWORD=pdf_password_2024
Environment=SPRING_REDIS_HOST=localhost
Environment=SPRING_REDIS_PORT=6379
Environment=SPRING_REDIS_PASSWORD=redis_password_2024
Environment=SERVER_PORT=8080
Environment=LOGGING_LEVEL_ROOT=INFO

StandardOutput=journal
StandardError=journal
SyslogIdentifier=pdf-tool-backend

[Install]
WantedBy=multi-user.target
EOF

# 重载 systemd 并启动服务
sudo systemctl daemon-reload
sudo systemctl enable pdf-tool-backend
sudo systemctl start pdf-tool-backend

echo "✅ 部署完成！"
echo ""
echo "📊 服务状态:"
echo "- 后端服务: http://localhost:8080"
echo "- 前端服务: http://localhost (通过 Nginx)"
echo "- 数据库: PostgreSQL (localhost:5432)"
echo "- 缓存: Redis (localhost:6379)"
echo ""
echo "🔍 检查服务状态:"
echo "sudo systemctl status pdf-tool-backend"
echo "sudo systemctl status nginx"
echo "sudo systemctl status postgresql"
echo "sudo systemctl status redis-server"
echo ""
echo "📝 查看日志:"
echo "sudo journalctl -u pdf-tool-backend -f" 