#!/bin/bash

# PDF工具原生部署启动脚本

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_status() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

echo "🚀 PDF工具原生部署启动..."

# 加载环境变量
if [ -f ".env" ]; then
    print_info "加载环境变量..."
    export $(grep -v '^#' .env | xargs)
    print_status "环境变量已加载"
    print_info "JWT密钥长度: $(echo -n "$MEMFIRE_JWT_SECRET" | wc -c) 字符"
else
    print_warning ".env文件不存在，使用默认配置"
fi

# 检查环境
print_info "检查运行环境..."

# 检查Java
if ! command -v java &> /dev/null; then
    print_error "Java未安装，请先安装Java 17"
    exit 1
fi

# 检查Node.js
if ! command -v node &> /dev/null; then
    print_error "Node.js未安装"
    exit 1
fi

# 检查项目目录
PROJECT_ROOT="/root/pdf-tool"
if [ ! -d "$PROJECT_ROOT" ]; then
    print_error "项目目录不存在: $PROJECT_ROOT"
    exit 1
fi

# 停止现有服务
print_info "停止现有服务..."
pkill -f "pdf-tool" 2>/dev/null || true
pkill -f "npm run" 2>/dev/null || true

# 创建日志目录
mkdir -p $PROJECT_ROOT/logs

# 构建后端
print_info "构建后端项目..."
cd $PROJECT_ROOT/backend

print_info "使用系统 Gradle 构建后端..."
gradle clean build -x test

# 检查jar文件
JAR_FILE=$(find build/libs -name "*.jar" -not -name "*-plain.jar" | head -1)
if [ -z "$JAR_FILE" ]; then
    print_error "未找到构建的jar文件"
    exit 1
fi

print_status "找到jar文件: $JAR_FILE"

# 启动后端
print_info "启动后端服务..."
cd $PROJECT_ROOT

# 关键：设置 so 路径和Tesseract语言包路径
export LD_LIBRARY_PATH=/usr/lib/x86_64-linux-gnu:$LD_LIBRARY_PATH
export TESSDATA_PREFIX=/usr/share/tesseract-ocr/5/

nohup java -Djava.library.path=/usr/lib/x86_64-linux-gnu -jar backend/$JAR_FILE \
    --server.port=8080 \
    --spring.profiles.active=prod \
    > logs/backend.log 2>&1 &

BACKEND_PID=$!
echo "后端PID: $BACKEND_PID"

# 等待后端启动
print_info "等待后端服务启动..."
for i in {1..30}; do
    if curl -f -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
        print_status "后端服务启动成功"
        break
    fi
    if [ $i -eq 30 ]; then
        print_error "后端服务启动失败"
        cat logs/backend.log | tail -20
        exit 1
    fi
    echo "等待中... ($i/30)"
    sleep 2
done

# 构建前端
print_info "构建前端项目..."
cd $PROJECT_ROOT/frontend

print_info "清理前端依赖和缓存..."
rm -rf node_modules package-lock.json
npm cache clean --force

print_info "安装前端依赖..."
unset NODE_ENV
npm install
npm install vite --save-dev

print_info "开始前端构建..."
npm run build

# 部署前端静态文件到 nginx 目录
NGINX_STATIC_DIR="/var/www/pdf-tool"

# 备份手动上传的文件
print_info "备份手动上传的文件..."
BACKUP_DIR="/tmp/manual_files_backup"
mkdir -p $BACKUP_DIR
if [ -f "$NGINX_STATIC_DIR/privacy-policy.html" ]; then
    cp "$NGINX_STATIC_DIR/privacy-policy.html" "$BACKUP_DIR/"
fi
if [ -f "$NGINX_STATIC_DIR/user-agreement.html" ]; then
    cp "$NGINX_STATIC_DIR/user-agreement.html" "$BACKUP_DIR/"
fi
if [ -f "$NGINX_STATIC_DIR/apple-app-site-association" ]; then
    cp "$NGINX_STATIC_DIR/apple-app-site-association" "$BACKUP_DIR/"
fi

print_info "清理 nginx 静态目录旧产物..."
rm -rf $NGINX_STATIC_DIR/*

print_info "拷贝新前端产物到 nginx 静态目录..."
cp -r dist/* $NGINX_STATIC_DIR/

# 恢复手动上传的文件
print_info "恢复手动上传的文件..."
if [ -f "$BACKUP_DIR/privacy-policy.html" ]; then
    cp "$BACKUP_DIR/privacy-policy.html" "$NGINX_STATIC_DIR/"
fi
if [ -f "$BACKUP_DIR/user-agreement.html" ]; then
    cp "$BACKUP_DIR/user-agreement.html" "$NGINX_STATIC_DIR/"
fi
if [ -f "$BACKUP_DIR/apple-app-site-association" ]; then
    cp "$BACKUP_DIR/apple-app-site-association" "$NGINX_STATIC_DIR/"
fi

# 清理备份目录
rm -rf $BACKUP_DIR

print_status "前端构建和部署完成"

# 配置nginx提供前端静态文件
print_info "配置前端服务..."

# 创建nginx配置
NGINX_CONFIG="/etc/nginx/sites-available/pdf-tool"
sudo tee $NGINX_CONFIG > /dev/null <<EOF
server {
    listen 80;
    server_name _;
    
    root $PROJECT_ROOT/frontend/dist;
    index index.html;
    
    # 静态文件缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
    
    # API代理到后端
    location /api/ {
        proxy_pass http://localhost:8080/;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        
        # 增加超时时间
        proxy_connect_timeout 300;
        proxy_send_timeout 300;
        proxy_read_timeout 300;
    }
    
    # Vue Router history模式支持
    location / {
        try_files \$uri \$uri/ /index.html;
    }
    
    # 错误页面
    error_page 500 502 503 504 /50x.html;
    location = /50x.html {
        root /usr/share/nginx/html;
    }
}
EOF

# 启用站点
sudo ln -sf $NGINX_CONFIG /etc/nginx/sites-enabled/pdf-tool
sudo rm -f /etc/nginx/sites-enabled/default

# 测试nginx配置
if sudo nginx -t; then
    print_status "Nginx配置验证成功"
    sudo systemctl reload nginx
else
    print_error "Nginx配置验证失败"
    exit 1
fi

# 检查nginx服务
if ! systemctl is-active --quiet nginx; then
    print_info "启动Nginx服务..."
    sudo systemctl start nginx
    sudo systemctl enable nginx
fi

# 等待前端服务就绪
sleep 3

# 验证服务
print_info "验证服务状态..."

# 检查后端
if curl -f -s http://localhost:8080/actuator/health > /dev/null; then
    print_status "后端服务正常 (http://localhost:8080)"
else
    print_error "后端服务异常"
fi

# 检查前端
if curl -f -s http://localhost > /dev/null; then
    print_status "前端服务正常 (http://localhost)"
else
    print_error "前端服务异常"
fi

# 获取服务器IP
SERVER_IP=$(curl -s ifconfig.me 2>/dev/null || echo "unknown")

print_status "部署完成！"
echo ""
echo "📱 访问地址："
echo "   前端应用: http://$SERVER_IP"
echo "   后端API:  http://$SERVER_IP:8080"
echo ""
echo "📋 管理命令："
echo "   查看后端日志: tail -f $PROJECT_ROOT/logs/backend.log"
echo "   查看nginx日志: sudo tail -f /var/log/nginx/error.log"
echo "   重启后端: pkill -f pdf-tool && ./start-native.sh"
echo "   停止服务: ./stop-native.sh"
echo ""
echo "📊 进程状态："
ps aux | grep -E "(java.*pdf-tool|nginx)" | grep -v grep || echo "无相关进程" 
