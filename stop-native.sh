#!/bin/bash

# PDF工具原生部署停止脚本

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
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

echo "🛑 停止PDF工具服务..."

# 停止后端Java进程
print_warning "停止后端服务..."
JAVA_PIDS=$(pgrep -f "java.*pdf-tool")
if [ -n "$JAVA_PIDS" ]; then
    echo "找到Java进程: $JAVA_PIDS"
    pkill -f "java.*pdf-tool"
    sleep 3
    
    # 检查是否还在运行
    if pgrep -f "java.*pdf-tool" > /dev/null; then
        print_warning "强制终止Java进程..."
        pkill -9 -f "java.*pdf-tool"
    fi
    print_status "后端服务已停止"
else
    print_warning "没有找到运行中的后端服务"
fi

# 停止前端npm进程
print_warning "停止前端开发服务..."
NPM_PIDS=$(pgrep -f "npm run")
if [ -n "$NPM_PIDS" ]; then
    echo "找到npm进程: $NPM_PIDS"
    pkill -f "npm run"
    print_status "前端开发服务已停止"
else
    print_warning "没有找到运行中的前端开发服务"
fi

# Nginx继续运行（提供静态文件）
print_status "Nginx继续运行提供静态文件服务"

echo ""
echo "📊 当前进程状态："
ps aux | grep -E "(java|nginx|npm)" | grep -v grep || echo "没有相关进程在运行"

echo ""
echo "🌐 服务状态："
echo "   前端(Nginx): $(systemctl is-active nginx 2>/dev/null || echo 'unknown')"
echo "   后端(Java):  $(pgrep -f java > /dev/null && echo 'stopped' || echo 'stopped')"

print_status "服务停止完成" 