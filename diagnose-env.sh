#!/bin/bash

echo "🔍 诊断 .env 文件问题..."

echo "📍 当前工作目录: $(pwd)"
echo "📍 脚本所在目录: $(dirname $0)"

echo -e "\n🔍 检查文件存在性..."

# 检查各种可能的路径
paths=(
    ".env"
    "../.env" 
    "/root/pdf-tool/.env"
    "/root/pdf-tool/backend/.env"
    "setup-env.sh"
    "../setup-env.sh"
    "/root/pdf-tool/setup-env.sh"
)

for path in "${paths[@]}"; do
    if [ -f "$path" ]; then
        echo "✅ 找到文件: $path"
        echo "   权限: $(ls -la "$path" | awk '{print $1, $3, $4}')"
        echo "   大小: $(ls -lh "$path" | awk '{print $5}')"
        echo "   内容预览:"
        head -3 "$path" | sed 's/^/   /'
    else
        echo "❌ 未找到: $path"
    fi
done

echo -e "\n🔍 检查环境变量..."
if [ -n "$MEMFIRE_JWT_SECRET" ]; then
    echo "✅ MEMFIRE_JWT_SECRET 已设置 (长度: ${#MEMFIRE_JWT_SECRET})"
else
    echo "❌ MEMFIRE_JWT_SECRET 未设置"
fi

echo -e "\n🔍 手动测试 .env 加载..."
test_files=("../.env" ".env" "/root/pdf-tool/.env")

for test_file in "${test_files[@]}"; do
    if [ -f "$test_file" ]; then
        echo "📝 测试加载: $test_file"
        echo "设置前 MEMFIRE_JWT_SECRET: ${MEMFIRE_JWT_SECRET:-'未设置'}"
        
        # 尝试加载
        set -a
        source "$test_file" 2>/dev/null && echo "✅ 加载成功" || echo "❌ 加载失败"
        set +a
        
        echo "设置后 MEMFIRE_JWT_SECRET: ${MEMFIRE_JWT_SECRET:-'未设置'}"
        echo "---"
    fi
done

echo -e "\n🔍 检查 .env 文件内容格式..."
for test_file in "${test_files[@]}"; do
    if [ -f "$test_file" ]; then
        echo "📄 检查文件: $test_file"
        echo "文件内容:"
        cat "$test_file" | sed 's/^/   /'
        echo "检查格式问题:"
        
        # 检查常见格式问题
        if grep -q $'\r' "$test_file"; then
            echo "⚠️  发现Windows换行符(\\r)，可能导致问题"
        fi
        
        if grep -q '=' "$test_file"; then
            echo "✅ 包含环境变量定义"
        else
            echo "❌ 未找到环境变量定义"
        fi
        echo "---"
    fi
done 