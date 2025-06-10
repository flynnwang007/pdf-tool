#!/bin/bash

# 服务器本地部署脚本
# 直接在服务器上运行，不需要SSH连接

set -euo pipefail

# 配置变量
readonly SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly PROJECT_ROOT="$SCRIPT_DIR"  # 脚本就在前端项目根目录中
readonly BUILD_DIR="$PROJECT_ROOT/dist"
readonly SERVER_PATH="/var/www/pdf-tool"
readonly TIMESTAMP=$(date '+%Y%m%d_%H%M%S')

# 环境变量配置
export NODE_ENV=production
export VITE_API_BASE_URL="http://14.103.200.105:8080/api"
export VITE_SUPABASE_URL="https://hfshqcftgclnpcfymdnn.supabase.co"
export VITE_SUPABASE_ANON_KEY="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imhmc2hxY2Z0Z2NsbnBjZnltZG5uIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzUwNzM3ODcsImV4cCI6MjA1MDY0OTc4N30.5l6B6T2CxoYCCFebpD1P0p-r8KRRGWqh_cKKLIXJB8I"

# 颜色定义
readonly RED='\033[0;31m'
readonly GREEN='\033[0;32m'
readonly YELLOW='\033[1;33m'
readonly BLUE='\033[0;34m'
readonly NC='\033[0m' # No Color

# 日志函数
print_status() {
    echo -e "${GREEN}✓${NC} $1"
}

print_info() {
    echo -e "${BLUE}ℹ${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

# 错误处理
error_exit() {
    print_error "$1"
    exit 1
}

# 验证环境
validate_environment() {
    print_info "验证部署环境..."
    
    # 检查 Node.js
    if ! command -v node &> /dev/null; then
        error_exit "Node.js 未安装或不在 PATH 中"
    fi
    
    # 检查 npm
    if ! command -v npm &> /dev/null; then
        error_exit "npm 未安装或不在 PATH 中"
    fi
    
    # 检查项目目录
    if [ ! -f "$PROJECT_ROOT/package.json" ]; then
        error_exit "项目根目录未找到 package.json"
    fi
    
    print_status "环境验证通过"
}

# 清理构建目录
clean_build() {
    print_info "清理构建目录..."
    
    if [ -d "$BUILD_DIR" ]; then
        rm -rf "$BUILD_DIR"
        print_status "构建目录已清理"
    fi
}

# 安装依赖
install_dependencies() {
    print_info "安装项目依赖..."
    
    cd "$PROJECT_ROOT"
    
    # 清理可能的缓存
    rm -rf node_modules package-lock.json
    
    # 安装完整依赖（包括dev依赖，因为需要vite构建工具）
    npm install --include=dev
    
    # 验证vite是否可用
    if ! npx vite --version > /dev/null 2>&1; then
        print_warning "vite未正确安装，尝试单独安装..."
        npm install vite@^5.0.8
    fi
    
    print_status "依赖安装完成"
}

# 构建项目
build_project() {
    print_info "构建生产版本..."
    print_info "使用的环境变量:"
    print_info "  - VITE_API_BASE_URL: $VITE_API_BASE_URL"
    print_info "  - VITE_SUPABASE_URL: $VITE_SUPABASE_URL"
    
    cd "$PROJECT_ROOT"
    
    # 使用npx确保能找到vite命令
    npx vite build
    
    if [ ! -d "$BUILD_DIR" ]; then
        error_exit "构建失败，未生成 dist 目录"
    fi
    
    print_status "项目构建完成"
}

# 修复 HMR 变量
fix_hmr_variables() {
    print_info "修复 Vite HMR 变量..."
    
    # 使用循环逐个文件处理，避免sed语法问题
    for js_file in $(find "$BUILD_DIR" -name "*.js" -type f); do
        # 使用简单的sed替换，避免复杂语法
        sed -i 's/__DEFINES__/{}/g' "$js_file" 2>/dev/null || true
        sed -i 's/__HMR_CONFIG_NAME__/null/g' "$js_file" 2>/dev/null || true
        sed -i 's/__HMR_PROTOCOL__/null/g' "$js_file" 2>/dev/null || true
        sed -i 's/__HMR_HOSTNAME__/null/g' "$js_file" 2>/dev/null || true
        sed -i 's/__HMR_PORT__/null/g' "$js_file" 2>/dev/null || true
        sed -i 's/__HMR_TIMEOUT__/null/g' "$js_file" 2>/dev/null || true
        sed -i 's/__HMR_ENABLE_OVERLAY__/false/g' "$js_file" 2>/dev/null || true
        sed -i 's/__HMR_FALLBACK__/false/g' "$js_file" 2>/dev/null || true
        sed -i 's/__SERVER_HOST__/null/g' "$js_file" 2>/dev/null || true
        sed -i 's/__HMR_DIRECT_TARGET__/null/g' "$js_file" 2>/dev/null || true
        sed -i 's/__HMR_BASE__/"\/"/g' "$js_file" 2>/dev/null || true
    done
    
    # 验证修复结果
    local remaining_vars=$(grep -r "__DEFINES__\|__HMR_" "$BUILD_DIR" --include="*.js" 2>/dev/null | wc -l)
    if [ "$remaining_vars" -gt 0 ]; then
        print_warning "仍有 $remaining_vars 个未修复的HMR变量，尝试强制修复..."
        
        # 如果仍有问题，使用perl替换（更可靠）
        for js_file in $(find "$BUILD_DIR" -name "*.js" -type f); do
            if command -v perl >/dev/null 2>&1; then
                perl -i -pe 's/__DEFINES__/{}/g' "$js_file" 2>/dev/null || true
                perl -i -pe 's/__HMR_CONFIG_NAME__/null/g' "$js_file" 2>/dev/null || true
                perl -i -pe 's/__HMR_PROTOCOL__/null/g' "$js_file" 2>/dev/null || true
                perl -i -pe 's/__HMR_HOSTNAME__/null/g' "$js_file" 2>/dev/null || true
                perl -i -pe 's/__HMR_PORT__/null/g' "$js_file" 2>/dev/null || true
                perl -i -pe 's/__HMR_TIMEOUT__/null/g' "$js_file" 2>/dev/null || true
                perl -i -pe 's/__HMR_ENABLE_OVERLAY__/false/g' "$js_file" 2>/dev/null || true
                perl -i -pe 's/__HMR_FALLBACK__/false/g' "$js_file" 2>/dev/null || true
                perl -i -pe 's/__SERVER_HOST__/null/g' "$js_file" 2>/dev/null || true
                perl -i -pe 's/__HMR_DIRECT_TARGET__/null/g' "$js_file" 2>/dev/null || true
                perl -i -pe 's/__HMR_BASE__/"\/"/g' "$js_file" 2>/dev/null || true
            fi
        done
        
        # 最终验证
        remaining_vars=$(grep -r "__DEFINES__\|__HMR_" "$BUILD_DIR" --include="*.js" 2>/dev/null | wc -l)
        if [ "$remaining_vars" -gt 0 ]; then
            print_error "无法完全修复HMR变量，请手动检查："
            grep -r "__DEFINES__\|__HMR_" "$BUILD_DIR" --include="*.js" | head -3
        else
            print_status "所有HMR变量已修复"
        fi
    else
        print_status "所有HMR变量已修复"
    fi
    
    print_status "HMR 变量修复完成"
}

# 验证构建结果
validate_build() {
    print_info "验证构建结果..."
    
    # 检查关键文件
    if [ ! -f "$BUILD_DIR/index.html" ]; then
        error_exit "关键文件缺失: index.html"
    fi
    
    # 检查资源文件
    if [ ! -d "$BUILD_DIR/assets" ]; then
        error_exit "资源目录缺失: assets"
    fi
    
    # 检查文件大小（基本验证）
    local index_size=$(wc -c < "$BUILD_DIR/index.html")
    if [ "$index_size" -lt 100 ]; then
        error_exit "index.html 文件异常小，可能构建失败"
    fi
    
    print_status "构建验证通过"
}

# 部署到nginx
deploy_to_nginx() {
    print_info "部署到nginx目录..."
    
    # 创建部署目录
    sudo mkdir -p "$SERVER_PATH"
    
    # 备份当前部署（如果存在）
    if [ -d "$SERVER_PATH" ] && [ "$(ls -A $SERVER_PATH)" ]; then
        sudo cp -r "$SERVER_PATH" "/tmp/pdf-tool-backup-$TIMESTAMP"
        print_info "当前部署已备份到: /tmp/pdf-tool-backup-$TIMESTAMP"
    fi
    
    # 替换文件
    sudo rm -rf "$SERVER_PATH"/*
    sudo cp -r "$BUILD_DIR"/* "$SERVER_PATH/"
    
    # 设置权限
    sudo chown -R www-data:www-data "$SERVER_PATH"
    sudo chmod -R 755 "$SERVER_PATH"
    
    # 重启nginx
    sudo systemctl reload nginx
    
    print_status "nginx部署完成"
}

# 部署后验证
post_deploy_verification() {
    print_info "验证部署结果..."
    
    # 等待服务启动
    sleep 3
    
    # 检查nginx服务
    if sudo systemctl is-active --quiet nginx; then
        print_status "nginx服务正常运行"
    else
        print_error "nginx服务异常"
        return 1
    fi
    
    # 检查前端服务
    if curl -f -s "http://localhost:8081" > /dev/null; then
        print_status "前端服务验证成功"
    else
        print_warning "前端服务验证失败"
    fi
    
    # 检查后端API
    if curl -f -s "http://localhost:8080/actuator/health" > /dev/null; then
        print_status "后端API验证成功"
    else
        print_warning "后端API验证失败，请检查后端服务状态"
    fi
    
    print_status "部署验证完成"
}

# 显示部署信息
show_deployment_info() {
    echo ""
    echo "=========================================="
    echo "           部署完成信息"
    echo "=========================================="
    echo ""
    echo "🌐 前端访问地址:"
    echo "   http://14.103.200.105:8081"
    echo ""
    echo "🔧 后端 API 地址:"
    echo "   http://14.103.200.105:8080"
    echo ""
    echo "📊 健康检查:"
    echo "   http://14.103.200.105:8080/actuator/health"
    echo ""
    echo "📁 服务器部署路径:"
    echo "   $SERVER_PATH"
    echo ""
    echo "⏰ 部署时间:"
    echo "   $(date '+%Y-%m-%d %H:%M:%S')"
    echo ""
    echo "=========================================="
}

# 主函数
main() {
    echo "🚀 PDF工具 - 服务器本地部署脚本"
    echo "========================================"
    echo ""
    
    # 捕获错误
    trap 'error_exit "部署过程中发生错误"' ERR
    
    # 执行部署步骤
    validate_environment
    clean_build
    install_dependencies
    build_project
    fix_hmr_variables
    validate_build
    deploy_to_nginx
    post_deploy_verification
    
    # 显示结果
    show_deployment_info
    
    print_status "🎉 服务器本地部署成功！"
}

# 脚本入口
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi 