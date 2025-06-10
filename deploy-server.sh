#!/bin/bash

# PDF工具服务器部署脚本

set -e

echo "🚀 PDF工具服务器部署开始..."

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

# 清理函数
cleanup() {
    print_warning "清理现有的Docker资源..."
    
    # 停止容器
    docker-compose down 2>/dev/null || true
    docker stop pdf-frontend pdf-backend 2>/dev/null || true
    
    # 删除容器
    docker rm pdf-frontend pdf-backend 2>/dev/null || true
    
    # 删除镜像
    docker rmi $(docker images --format "table {{.Repository}}:{{.Tag}}" | grep -E "(pdf-tool|pdf_tool)" | tr -s ' ' | cut -d' ' -f1) 2>/dev/null || true
    
    # 清理未使用的镜像
    docker image prune -f
    
    print_status "清理完成"
}

# 构建函数
build() {
    print_warning "开始构建项目..."
    
    # 检查是否存在docker-compose.yml
    if [ ! -f "docker-compose.yml" ]; then
        print_error "docker-compose.yml 文件不存在"
        exit 1
    fi
    
    # 构建并启动
    docker-compose up -d --build
    
    print_status "构建完成"
}

# 检查状态函数
check_status() {
    print_warning "检查服务状态..."
    
    # 等待服务启动
    sleep 10
    
    # 检查容器状态
    echo "容器状态："
    docker-compose ps
    
    # 检查前端服务
    if curl -f -s http://localhost > /dev/null; then
        print_status "前端服务正常"
    else
        print_error "前端服务异常"
        docker logs pdf-frontend --tail 20
    fi
    
    # 检查后端服务
    if curl -f -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
        print_status "后端服务正常"
    else
        print_warning "后端服务可能还在启动中..."
        docker logs pdf-backend --tail 20
    fi
}

# 主程序
case "$1" in
    "clean")
        cleanup
        ;;
    "build")
        build
        ;;
    "status")
        check_status
        ;;
    "deploy")
        cleanup
        build
        check_status
        print_status "部署完成！访问 http://your-server-ip 查看应用"
        ;;
    *)
        echo "用法: $0 {clean|build|status|deploy}"
        echo "  clean  - 清理Docker资源"
        echo "  build  - 构建并启动服务"
        echo "  status - 检查服务状态"
        echo "  deploy - 完整部署（清理+构建+检查）"
        exit 1
        ;;
esac 