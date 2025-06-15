#!/bin/bash

# 后端部署脚本
set -e

echo "🚀 开始部署后端服务..."

# 停止现有服务
echo "⏹️ 停止现有服务..."
pkill -f "java.*pdf-tool" || true

# 创建必要的目录
echo "📁 创建必要目录..."
mkdir -p uploads
mkdir -p logs
chmod 755 uploads logs

# 检查gradle wrapper
echo "🔧 检查gradle wrapper..."
if [ ! -f "gradle/wrapper/gradle-wrapper.jar" ]; then
    echo "⚠️  gradle-wrapper.jar缺失，正在修复..."
    # 下载gradle wrapper
    if command -v gradle > /dev/null 2>&1; then
        gradle wrapper --gradle-version=8.14.1
        echo "✅ gradle wrapper已修复"
    else
        echo "❌ 错误: gradle命令不可用，无法修复wrapper"
        echo "请手动运行: gradle wrapper --gradle-version=8.14.1"
        exit 1
    fi
fi

# 清理旧构建
echo "🧹 清理旧构建..."
./gradlew clean

# 构建项目
echo "🔨 构建项目..."
./gradlew build -x test

# 检查JAR文件
JAR_FILE=$(find build/libs -name "*.jar" -not -name "*plain.jar" | head -1)
if [ ! -f "$JAR_FILE" ]; then
    echo "❌ 构建失败：找不到JAR文件"
    exit 1
fi

echo "✅ 构建成功：$JAR_FILE"

# 加载环境变量
if [ -f "../setup-env.sh" ]; then
    echo "📝 加载环境变量..."
    source ../setup-env.sh
    echo "✅ 环境变量已加载"
elif [ -f "../.env" ]; then
    echo "📝 加载.env文件..."
    set -a  # 自动export所有变量
    source ../.env
    set +a
    echo "✅ 环境变量已加载"
else
    echo "⚠️  警告: 未找到环境配置文件，手动设置环境变量..."
    # 手动设置MemFireDB配置
    export MEMFIRE_JWT_SECRET="0d37e31b-3452-4949-8e19-04bc619c78c9"
    export VITE_SUPABASE_URL="https://d11558og91hm5619qfbg.baseapi.memfiredb.com"
    export VITE_SUPABASE_ANON_KEY="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImQxMTU1OG9nOTFobTU2MTlxZmJnIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzMwMjg1NjIsImV4cCI6MjA0ODYwNDU2Mn0.uGGa-pILGX3JHE3KQ8dO8LGOuwzAh5NCmEtKJ9_lGYI"
    echo "✅ 默认环境变量已设置"
fi

# 启动服务
echo "🚀 启动后端服务..."

# 验证必要的环境变量
if [ -n "$MEMFIRE_JWT_SECRET" ]; then
    echo "✅ 使用MemFireDB JWT配置"
    echo "🔑 JWT密钥长度: $(echo -n "$MEMFIRE_JWT_SECRET" | wc -c) 字符"
else
    echo "❌ 错误: MEMFIRE_JWT_SECRET 环境变量未设置"
    echo "请在.env文件中设置 MEMFIRE_JWT_SECRET=your-memfire-jwt-secret"
    exit 1
fi

# 验证其他MemFireDB配置
if [ -z "$VITE_SUPABASE_URL" ] || [ -z "$VITE_SUPABASE_ANON_KEY" ]; then
    echo "⚠️  警告: MemFireDB URL或ANON_KEY未设置，某些功能可能不可用"
fi

nohup java -jar "$JAR_FILE" \
    --spring.profiles.active=prod \
    --server.port=8080 \
    --app.file.upload-path=/root/pdf-tool/backend/uploads \
    --logging.file.name=/root/pdf-tool/backend/logs/app.log \
    > logs/nohup.log 2>&1 &

# 等待服务启动
echo "⏳ 等待服务启动..."
sleep 10

# 检查服务状态
if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "✅ 后端服务启动成功！"
    echo "🌐 服务地址: http://www.aibase123.cn"
    echo "📊 健康检查: http://www.aibase123.cn/actuator/health"
    
    # 测试JWT验证功能
    echo "🔐 测试JWT验证功能..."
    if [ -f "test-jwt-simple.js" ] && command -v node > /dev/null 2>&1; then
        echo "⏳ 运行JWT测试..."
        if node test-jwt-simple.js | grep -q "JWT验证成功"; then
            echo "✅ JWT验证功能正常"
        else
            echo "⚠️  JWT验证功能异常，请检查配置"
        fi
    else
        echo "⚠️  跳过JWT测试（缺少测试文件或Node.js）"
    fi
else
    echo "❌ 后端服务启动失败，请检查日志："
    echo "📝 应用日志: tail -f logs/app.log"
    echo "📝 启动日志: tail -f logs/nohup.log"
    exit 1
fi 