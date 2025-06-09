#!/bin/bash

# PDF 工具部署脚本 - 使用现有 Nginx

set -e

echo "🚀 开始 PDF 工具部署 (使用现有 Nginx)..."

# 端口配置
BACKEND_PORT=8080
POSTGRES_PORT=5432
REDIS_PORT=6379
PDF_TOOL_PATH="/pdf-tool"  # 在现有网站上的路径

echo "📊 配置信息:"
echo "  - 后端端口: $BACKEND_PORT"
echo "  - 访问路径: http://your-server$PDF_TOOL_PATH"
echo "  - PostgreSQL: $POSTGRES_PORT"
echo "  - Redis: $REDIS_PORT"
echo ""

# 项目配置
PROJECT_DIR="/opt/pdf-tool"
BACKEND_DIR="$PROJECT_DIR/backend"
FRONTEND_DIR="$PROJECT_DIR/frontend"

echo "📁 创建项目目录..."
sudo mkdir -p $PROJECT_DIR
sudo chown -R $USER:$USER $PROJECT_DIR

echo "📋 复制项目文件..."
cp -r ~/pdf-tool/* $PROJECT_DIR/

# 安装必要的依赖
echo "📦 检查并安装依赖..."

# 检查 Java
if ! command -v java &> /dev/null; then
    echo "安装 Java 17..."
    sudo apt update
    sudo apt install -y openjdk-17-jdk
fi

# 检查 Node.js
if ! command -v node &> /dev/null; then
    echo "安装 Node.js..."
    curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
    sudo apt-get install -y nodejs
fi

# 检查 PostgreSQL
if ! command -v psql &> /dev/null; then
    echo "安装 PostgreSQL..."
    sudo apt install -y postgresql postgresql-contrib
    sudo systemctl start postgresql
    sudo systemctl enable postgresql
    
    # 创建数据库和用户
    sudo -u postgres psql << 'EOF'
CREATE DATABASE pdf_tool;
CREATE USER pdf_user WITH PASSWORD 'pdf_password_2024';
GRANT ALL PRIVILEGES ON DATABASE pdf_tool TO pdf_user;
ALTER USER pdf_user CREATEDB;
\q
EOF
fi

# 检查 Redis
if ! command -v redis-server &> /dev/null; then
    echo "安装 Redis..."
    sudo apt install -y redis-server
    sudo sed -i 's/# requirepass foobared/requirepass redis_password_2024/' /etc/redis/redis.conf
    sudo systemctl restart redis-server
    sudo systemctl enable redis-server
fi

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

# 修改前端的 API 基础路径
echo "⚙️ 配置前端 API 路径..."
# 在构建前修改 vite.config.ts 中的 base 路径
sed -i "s|base: '/'|base: '$PDF_TOOL_PATH/'|g" vite.config.ts || true
npm run build

# 复制前端文件到 Nginx 目录
echo "📂 部署前端文件..."
sudo mkdir -p /var/www/html$PDF_TOOL_PATH
sudo cp -r dist/* /var/www/html$PDF_TOOL_PATH/
sudo chown -R www-data:www-data /var/www/html$PDF_TOOL_PATH

# 配置 Nginx - 添加 PDF 工具的配置
echo "🌐 配置 Nginx..."
sudo tee /etc/nginx/sites-available/pdf-tool-addition << EOF
# PDF 工具配置 - 添加到现有站点
location $PDF_TOOL_PATH/ {
    alias /var/www/html$PDF_TOOL_PATH/;
    try_files \$uri \$uri/ $PDF_TOOL_PATH/index.html;
    index index.html;
}

# PDF 工具 API 代理
location /api/pdf/ {
    proxy_pass http://localhost:$BACKEND_PORT/api/;
    proxy_set_header Host \$host;
    proxy_set_header X-Real-IP \$remote_addr;
    proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto \$scheme;
    
    # 文件上传大小限制
    client_max_body_size 100M;
}

# PDF 工具静态资源
location $PDF_TOOL_PATH/assets/ {
    alias /var/www/html$PDF_TOOL_PATH/assets/;
    expires 1y;
    add_header Cache-Control "public, immutable";
}
EOF

# 将配置添加到默认站点
echo "📝 更新 Nginx 配置..."
if grep -q "location $PDF_TOOL_PATH/" /etc/nginx/sites-available/default; then
    echo "PDF 工具配置已存在，跳过..."
else
    echo "添加 PDF 工具配置到默认站点..."
    # 在 server 块的最后添加配置
    sudo sed -i '/^[[:space:]]*}[[:space:]]*$/i\    # PDF Tool Configuration\n    include /etc/nginx/sites-available/pdf-tool-addition;' /etc/nginx/sites-available/default
fi

# 测试 Nginx 配置
sudo nginx -t
sudo systemctl reload nginx

# 创建后端服务
echo "⚙️ 创建后端服务..."
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

# 启动服务
sudo systemctl daemon-reload
sudo systemctl enable pdf-tool-backend
sudo systemctl start pdf-tool-backend

echo ""
echo "✅ 部署完成！"
echo ""
echo "📊 服务访问地址:"
echo "  - PDF 工具: http://your-server-ip$PDF_TOOL_PATH"
echo "  - 后端 API: http://your-server-ip/api/pdf/"
echo "  - 现有服务 (ai-tools): http://your-server-ip:3001"
echo ""
echo "🔍 服务状态检查:"
echo "  sudo systemctl status pdf-tool-backend"
echo "  sudo systemctl status nginx"
echo "  sudo systemctl status postgresql"
echo "  sudo systemctl status redis-server"
echo ""
echo "📝 查看日志:"
echo "  sudo journalctl -u pdf-tool-backend -f"
echo ""
echo "🌍 两个应用现在可以同时访问:"
echo "  - AI Tools Directory: http://your-server-ip:3001"
echo "  - PDF Tool: http://your-server-ip$PDF_TOOL_PATH" 