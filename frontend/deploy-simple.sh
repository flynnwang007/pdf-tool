#!/bin/bash

# 简单可靠的服务器部署脚本
set -e

echo "🚀 开始部署..."

# 环境变量
export NODE_ENV=production
export VITE_API_BASE_URL="http://14.103.200.105:8080/api"

# 清理
echo "📦 清理旧文件..."
rm -rf dist/ node_modules/

# 安装依赖（包括devDependencies，因为构建需要vite）
echo "📦 安装依赖..."
npm install --include=dev

# 构建
echo "🔨 构建项目..."
npx vite build

# 修复HMR变量 - 使用最简单的方法
echo "🔧 修复HMR变量..."
cd dist/assets/
for file in *.js; do
    if [ -f "$file" ]; then
        # 逐行替换，最可靠的方法
        python3 -c "
import re
with open('$file', 'r', encoding='utf-8') as f:
    content = f.read()

# 替换所有HMR变量（完整列表）
content = re.sub(r'__DEFINES__', '{}', content)
content = re.sub(r'__HMR_CONFIG_NAME__', 'null', content)
content = re.sub(r'__HMR_PROTOCOL__', 'null', content)
content = re.sub(r'__HMR_HOSTNAME__', 'null', content)
content = re.sub(r'__HMR_PORT__', 'null', content)
content = re.sub(r'__HMR_TIMEOUT__', 'null', content)
content = re.sub(r'__HMR_ENABLE_OVERLAY__', 'false', content)
content = re.sub(r'__HMR_FALLBACK__', 'false', content)
content = re.sub(r'__HMR_DIRECT_TARGET__', 'null', content)
content = re.sub(r'__HMR_BASE__', '\"/\"', content)
content = re.sub(r'__SERVER_HOST__', 'null', content)
content = re.sub(r'__BASE__', '\"/\"', content)
content = re.sub(r'__VITE_BASE__', '\"/\"', content)
content = re.sub(r'__VITE_IS_MODERN__', 'true', content)
content = re.sub(r'__VITE_LEGACY__', 'false', content)

with open('$file', 'w', encoding='utf-8') as f:
    f.write(content)
print(f'Fixed: $file')
" 2>/dev/null || {
            # 如果没有python3，使用sed备用方案
            sed -i.bak 's/__DEFINES__/{}/g; s/__HMR_CONFIG_NAME__/null/g; s/__HMR_PROTOCOL__/null/g; s/__HMR_HOSTNAME__/null/g; s/__HMR_PORT__/null/g; s/__HMR_TIMEOUT__/null/g; s/__HMR_ENABLE_OVERLAY__/false/g; s/__HMR_FALLBACK__/false/g; s/__HMR_DIRECT_TARGET__/null/g; s|__HMR_BASE__|"/"|g; s/__SERVER_HOST__/null/g; s|__BASE__|"/"|g; s|__VITE_BASE__|"/"|g; s/__VITE_IS_MODERN__/true/g; s/__VITE_LEGACY__/false/g' "$file" 2>/dev/null || true
            rm -f "$file.bak" 2>/dev/null || true
        }
    fi
done
cd ../..

# 验证修复
echo "✅ 验证修复结果..."
if grep -r "__DEFINES__\|__HMR_" dist/ 2>/dev/null; then
    echo "⚠️  仍有未修复的变量，但继续部署"
else
    echo "✅ 所有HMR变量已修复"
fi

# 部署
echo "🚀 部署到nginx..."
sudo mkdir -p /var/www/pdf-tool
sudo rm -rf /var/www/pdf-tool/*
sudo cp -r dist/* /var/www/pdf-tool/
sudo chown -R www-data:www-data /var/www/pdf-tool
sudo chmod -R 755 /var/www/pdf-tool
sudo systemctl reload nginx

echo "✅ 部署完成！"
echo "🌐 访问地址: http://14.103.200.105:8081" 