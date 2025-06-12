#!/bin/bash

# 快速安装Gradle脚本
echo "🚀 快速安装Gradle..."

# 检测系统类型
if [ -f /etc/os-release ]; then
    . /etc/os-release
    OS=$NAME
fi

# Ubuntu/Debian快速安装
if [[ "$OS" == *"Ubuntu"* ]] || [[ "$OS" == *"Debian"* ]]; then
    echo "📦 使用APT安装Gradle..."
    
    # 更新包管理器
    sudo apt update
    
    # 安装Gradle（通常是较旧版本但可用）
    sudo apt install -y gradle
    
    # 或者安装SDKMAN来管理Gradle版本
    if ! command -v sdk &> /dev/null; then
        echo "📦 安装SDKMAN..."
        curl -s "https://get.sdkman.io" | bash
        source "$HOME/.sdkman/bin/sdkman-init.sh"
        sdk install gradle 8.14.1
    fi

# CentOS/RHEL快速安装
elif [[ "$OS" == *"CentOS"* ]] || [[ "$OS" == *"Red Hat"* ]]; then
    echo "📦 使用YUM/DNF安装Gradle..."
    
    # 尝试使用dnf（较新的系统）
    if command -v dnf &> /dev/null; then
        sudo dnf install -y gradle
    else
        sudo yum install -y gradle
    fi
fi

# 验证安装
if command -v gradle &> /dev/null; then
    echo "✅ Gradle安装成功！"
    gradle --version
else
    echo "❌ Gradle安装失败，尝试手动安装..."
    
    # 手动下载和安装（使用国内镜像）
    echo "📥 从腾讯云镜像下载Gradle..."
    cd /tmp
    wget https://mirrors.cloud.tencent.com/gradle/gradle-8.14.1-bin.zip
    
    if [ $? -eq 0 ]; then
        echo "📦 解压Gradle..."
        unzip gradle-8.14.1-bin.zip
        sudo mv gradle-8.14.1 /opt/
        
        # 创建符号链接
        sudo ln -sf /opt/gradle-8.14.1/bin/gradle /usr/local/bin/gradle
        
        echo "✅ Gradle手动安装完成！"
        gradle --version
    else
        echo "❌ 下载失败，请检查网络连接"
    fi
fi 