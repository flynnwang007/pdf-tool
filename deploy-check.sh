#!/bin/bash

# 部署验证脚本
set -e

echo "🔍 开始部署验证..."

# 检查环境变量
echo "📝 检查环境变量..."
if [ -f ".env" ]; then
    source .env
    echo "✅ .env文件已加载"
    
    # 验证关键配置
    if [ -n "$MEMFIRE_JWT_SECRET" ]; then
        echo "✅ MemFireDB JWT密钥已配置 (长度: $(echo -n "$MEMFIRE_JWT_SECRET" | wc -c) 字符)"
    else
        echo "❌ MEMFIRE_JWT_SECRET 未配置"
    fi
    
    if [ -n "$VITE_SUPABASE_URL" ]; then
        echo "✅ MemFireDB URL已配置: $VITE_SUPABASE_URL"
    else
        echo "❌ VITE_SUPABASE_URL 未配置"
    fi
else
    echo "❌ .env文件不存在"
fi

# 检查后端服务
echo "🔧 检查后端服务..."
BACKEND_URL="http://14.103.200.105:8080"
if curl -f "$BACKEND_URL/actuator/health" > /dev/null 2>&1; then
    echo "✅ 后端服务正常运行"
    
    # 测试JWT验证
    if [ -f "backend/test-jwt-simple.js" ] && command -v node > /dev/null 2>&1; then
        echo "🔐 测试JWT验证..."
        cd backend
        if node test-jwt-simple.js | grep -q "服务器响应.*success.*true"; then
            echo "✅ JWT验证功能正常"
        else
            echo "❌ JWT验证功能异常"
        fi
        cd ..
    else
        echo "⚠️  无法测试JWT验证（缺少依赖）"
    fi
else
    echo "❌ 后端服务无法访问"
fi

# 检查前端服务
echo "🌐 检查前端服务..."
FRONTEND_URL="http://14.103.200.105:8081"
if curl -f "$FRONTEND_URL" > /dev/null 2>&1; then
    echo "✅ 前端服务正常运行"
    
    # 检查是否有HMR变量残留
    if curl -s "$FRONTEND_URL" | grep -q "__DEFINES__\|__HMR_"; then
        echo "⚠️  前端可能存在HMR变量残留"
    else
        echo "✅ 前端构建正常，无HMR变量残留"
    fi
else
    echo "❌ 前端服务无法访问"
fi

# 检查API连通性
echo "🔗 检查API连通性..."
if curl -f "$BACKEND_URL/api/files" > /dev/null 2>&1; then
    echo "⚠️  API无认证访问（应该返回403）"
else
    echo "✅ API正确要求认证"
fi

echo "✅ 验证完成！"
echo "🌐 前端地址: $FRONTEND_URL"
echo "🔧 后端地址: $BACKEND_URL"
echo "📊 健康检查: $BACKEND_URL/actuator/health" 