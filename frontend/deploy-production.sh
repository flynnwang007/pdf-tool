#!/bin/bash

# 生产环境部署脚本
# 适用于服务器 14.103.200.105
# 作者：Deploy Script Generator
# 日期：$(date '+%Y-%m-%d %H:%M:%S')

set -euo pipefail

# 配置变量
readonly SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly PROJECT_ROOT="$SCRIPT_DIR"  # 脚本就在前端项目根目录中
readonly BUILD_DIR="$PROJECT_ROOT/dist"
readonly SERVER_IP="14.103.200.105"
readonly SERVER_USER="root"
readonly SERVER_PATH="/var/www/pdf-tool"
readonly BACKUP_DIR="$HOME/pdf-tool-backups"
readonly LOG_FILE="$PROJECT_ROOT/deploy.log"
readonly TIMESTAMP=$(date '+%Y%m%d_%H%M%S')

# 环境变量配置
export NODE_ENV=production
export VITE_API_BASE_URL="http://${SERVER_IP}:8080/api"
export VITE_SUPABASE_URL="https://hfshqcftgclnpcfymdnn.supabase.co"
export VITE_SUPABASE_ANON_KEY="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imhmc2hxY2Z0Z2NsbnBjZnltZG5uIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzUwNzM3ODcsImV4cCI6MjA1MDY0OTc4N30.5l6B6T2CxoYCCFebpD1P0p-r8KRRGWqh_cKKLIXJB8I"

# 颜色定义
readonly RED='\033[0;31m'
readonly GREEN='\033[0;32m'
readonly YELLOW='\033[1;33m'
readonly BLUE='\033[0;34m'
readonly NC='\033[0m' # No Color

# 日志函数
log() {
    echo "$(date '+%Y-%m-%d %H:%M:%S') - $1" | tee -a "$LOG_FILE"
}

print_status() {
    echo -e "${GREEN}✓${NC} $1" | tee -a "$LOG_FILE"
}

print_info() {
    echo -e "${BLUE}ℹ${NC} $1" | tee -a "$LOG_FILE"
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1" | tee -a "$LOG_FILE"
}

print_error() {
    echo -e "${RED}✗${NC} $1" | tee -a "$LOG_FILE"
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
    
    local node_version=$(node --version | cut -d'.' -f1 | cut -d'v' -f2)
    if [ "$node_version" -lt 16 ]; then
        error_exit "Node.js 版本过低，需要 v16 或更高版本"
    fi
    
    # 检查 npm
    if ! command -v npm &> /dev/null; then
        error_exit "npm 未安装或不在 PATH 中"
    fi
    
    # 检查 scp
    if ! command -v scp &> /dev/null; then
        error_exit "scp 未安装，无法上传文件到服务器"
    fi
    
    # 检查项目目录
    if [ ! -f "$PROJECT_ROOT/package.json" ]; then
        error_exit "项目根目录未找到 package.json"
    fi
    
    print_status "环境验证通过"
}

# 创建备份
create_backup() {
    print_info "创建当前部署备份..."
    
    mkdir -p "$BACKUP_DIR"
    
    # 备份当前 dist 目录（如果存在）
    if [ -d "$BUILD_DIR" ]; then
        local backup_name="dist_backup_$TIMESTAMP.tar.gz"
        tar -czf "$BACKUP_DIR/$backup_name" -C "$PROJECT_ROOT" dist
        print_status "本地备份已创建: $backup_name"
    fi
    
    # 备份服务器上的当前部署
    print_info "备份服务器上的当前部署..."
    if ssh "$SERVER_USER@$SERVER_IP" "[ -d '$SERVER_PATH' ]"; then
        ssh "$SERVER_USER@$SERVER_IP" "tar -czf /tmp/pdf-tool_backup_$TIMESTAMP.tar.gz -C /var/www pdf-tool"
        print_status "服务器备份已创建: /tmp/pdf-tool_backup_$TIMESTAMP.tar.gz"
    else
        print_warning "服务器上未找到现有部署，跳过备份"
    fi
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
    
    # 检查是否有 package-lock.json，优先使用 npm ci
    if [ -f "package-lock.json" ]; then
        npm ci
    else
        npm install
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
    npm run build
    
    if [ ! -d "$BUILD_DIR" ]; then
        error_exit "构建失败，未生成 dist 目录"
    fi
    
    print_status "项目构建完成"
}

# 修复 HMR 变量
fix_hmr_variables() {
    print_info "修复 Vite HMR 变量..."
    
    # 查找所有 JS 文件并替换 HMR 变量
    find "$BUILD_DIR" -name "*.js" -type f -exec sed -i.bak \
        -e 's/__HMR_BASE__/"\/"/g' \
        -e 's/__HMR_CONFIG_NAME__/null/g' \
        -e 's/__HMR_PROTOCOL__/null/g' \
        -e 's/__HMR_HOSTNAME__/null/g' \
        -e 's/__HMR_PORT__/null/g' \
        -e 's/__HMR_TIMEOUT__/null/g' \
        -e 's/__HMR_ENABLE_OVERLAY__/false/g' \
        -e 's/__HMR_FALLBACK__/false/g' \
        -e 's/__SERVER_HOST__/null/g' \
        -e 's/__HMR_DIRECT_TARGET__/null/g' \
        -e 's/__DEFINES__/{}/g' \
        {} \;
    
    # 清理备份文件
    find "$BUILD_DIR" -name "*.bak" -delete
    
    print_status "HMR 变量修复完成"
}

# 验证构建结果
validate_build() {
    print_info "验证构建结果..."
    
    # 检查关键文件
    local critical_files=("index.html")
    for file in "${critical_files[@]}"; do
        if [ ! -f "$BUILD_DIR/$file" ]; then
            error_exit "关键文件缺失: $file"
        fi
    done
    
    # 检查资源文件
    if [ ! -d "$BUILD_DIR/assets" ]; then
        error_exit "资源目录缺失: assets"
    fi
    
    # 检查文件大小（基本验证）
    local index_size=$(wc -c < "$BUILD_DIR/index.html")
    if [ "$index_size" -lt 100 ]; then
        error_exit "index.html 文件异常小，可能构建失败"
    fi
    
    # 验证是否还有未处理的 HMR 变量
    if grep -r "__HMR_" "$BUILD_DIR" --include="*.js" --include="*.html"; then
        print_warning "发现未处理的 HMR 变量，但将继续部署"
    fi
    
    print_status "构建验证通过"
}

# 打包构建结果
package_build() {
    print_info "打包构建结果..."
    
    cd "$PROJECT_ROOT"
    tar -czf "dist_$TIMESTAMP.tar.gz" dist/
    
    print_status "构建结果已打包: dist_$TIMESTAMP.tar.gz"
}

# 上传到服务器
upload_to_server() {
    print_info "上传构建结果到服务器..."
    
    # 上传压缩包
    scp "dist_$TIMESTAMP.tar.gz" "$SERVER_USER@$SERVER_IP:/tmp/"
    
    print_status "文件上传完成"
}

# 服务器部署
deploy_on_server() {
    print_info "在服务器上部署..."
    
    ssh "$SERVER_USER@$SERVER_IP" << EOF
        set -e
        
        # 创建部署目录
        sudo mkdir -p "$SERVER_PATH"
        
        # 解压新版本
        cd /tmp
        tar -xzf "dist_$TIMESTAMP.tar.gz"
        
        # 停止服务（如果需要）
        # sudo systemctl stop nginx 2>/dev/null || true
        
        # 替换文件
        sudo rm -rf "$SERVER_PATH"/*
        sudo cp -r dist/* "$SERVER_PATH/"
        
        # 设置权限
        sudo chown -R www-data:www-data "$SERVER_PATH"
        sudo chmod -R 755 "$SERVER_PATH"
        
        # 重启服务
        sudo systemctl reload nginx
        
        # 清理临时文件
        rm -f "/tmp/dist_$TIMESTAMP.tar.gz"
        rm -rf "/tmp/dist"
        
        echo "服务器部署完成"
EOF
    
    print_status "服务器部署完成"
}

# 部署后验证
post_deploy_verification() {
    print_info "验证部署结果..."
    
    # 等待服务启动
    sleep 5
    
    # 检查前端服务
    if curl -f -s "http://$SERVER_IP:8081" > /dev/null; then
        print_status "前端服务验证成功"
    else
        print_error "前端服务验证失败"
        return 1
    fi
    
    # 检查 API 连接
    if curl -f -s "http://$SERVER_IP:8080/actuator/health" > /dev/null; then
        print_status "后端 API 验证成功"
    else
        print_warning "后端 API 验证失败，请检查后端服务状态"
    fi
    
    print_status "部署验证完成"
}

# 清理本地文件
cleanup_local() {
    print_info "清理本地临时文件..."
    
    # 删除打包文件
    if [ -f "dist_$TIMESTAMP.tar.gz" ]; then
        rm "dist_$TIMESTAMP.tar.gz"
    fi
    
    print_status "本地清理完成"
}

# 显示部署信息
show_deployment_info() {
    echo ""
    echo "=========================================="
    echo "           部署完成信息"
    echo "=========================================="
    echo ""
    echo "🌐 前端访问地址:"
    echo "   http://$SERVER_IP:8081"
    echo ""
    echo "🔧 后端 API 地址:"
    echo "   http://$SERVER_IP:8080"
    echo ""
    echo "📊 健康检查:"
    echo "   http://$SERVER_IP:8080/actuator/health"
    echo ""
    echo "📁 服务器部署路径:"
    echo "   $SERVER_PATH"
    echo ""
    echo "📝 部署日志:"
    echo "   $LOG_FILE"
    echo ""
    echo "⏰ 部署时间:"
    echo "   $(date '+%Y-%m-%d %H:%M:%S')"
    echo ""
    echo "=========================================="
}

# 主函数
main() {
    log "开始生产环境部署 - $TIMESTAMP"
    
    # 捕获错误
    trap 'error_exit "部署过程中发生错误，请检查日志: $LOG_FILE"' ERR
    
    # 执行部署步骤
    validate_environment
    create_backup
    clean_build
    install_dependencies
    build_project
    fix_hmr_variables
    validate_build
    package_build
    upload_to_server
    deploy_on_server
    post_deploy_verification
    cleanup_local
    
    # 显示结果
    show_deployment_info
    
    log "部署完成 - $TIMESTAMP"
    print_status "🎉 生产环境部署成功！"
}

# 脚本入口
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi 