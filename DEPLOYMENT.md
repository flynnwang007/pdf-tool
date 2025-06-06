# PDF工具项目部署指南

## 🚀 环境要求

### 基础依赖
- Java 17+
- Tesseract OCR 5.x
- 中文字体包

## 📦 Tesseract OCR 安装

### Ubuntu/Debian
```bash
# 安装Tesseract OCR
sudo apt-get update
sudo apt-get install tesseract-ocr

# 安装中文语言包
sudo apt-get install tesseract-ocr-chi-sim tesseract-ocr-chi-tra

# 安装其他语言包（可选）
sudo apt-get install tesseract-ocr-jpn tesseract-ocr-kor
```

### CentOS/RHEL
```bash
# 安装EPEL源
sudo yum install epel-release

# 安装Tesseract OCR
sudo yum install tesseract tesseract-langpack-chi_sim tesseract-langpack-chi_tra

# 或者使用dnf（较新版本）
sudo dnf install tesseract tesseract-langpack-chi_sim tesseract-langpack-chi_tra
```

### macOS（开发环境）
```bash
# 使用Homebrew安装
brew install tesseract tesseract-lang
```

## 🖋️ 中文字体安装

### Ubuntu/Debian
```bash
# 安装WenQuanYi字体
sudo apt-get install fonts-wqy-microhei fonts-wqy-zenhei

# 安装Noto字体
sudo apt-get install fonts-noto-cjk

# 安装文泉驿字体
sudo apt-get install fonts-arphic-uming
```

### CentOS/RHEL
```bash
# 安装中文字体
sudo yum install wqy-microhei-fonts wqy-zenhei-fonts

# 或者使用dnf
sudo dnf install google-noto-cjk-fonts
```

## ⚙️ 应用配置

### 开发环境 (macOS)
使用 `application-dev.yml`，自动配置为：
- Tesseract路径: `/opt/homebrew/bin/tesseract`
- 数据路径: `/opt/homebrew/share/tessdata`
- 字体路径: macOS系统字体

启动命令：
```bash
java -jar pdf-tool-app.jar --spring.profiles.active=dev
```

### 生产环境 (Linux)
使用 `application-prod.yml`，配置为：
- Tesseract路径: `/usr/bin/tesseract`
- 数据路径: `/usr/share/tesseract-ocr/4.00/tessdata`
- 字体路径: Linux系统字体

启动命令：
```bash
java -jar pdf-tool-app.jar --spring.profiles.active=prod
```

## 🐳 Docker部署

### 使用Docker Compose
```bash
# 修改docker-compose.yml中的环境变量
# 确保以下环境变量正确设置：
# - TESSERACT_PATH=/usr/bin/tesseract
# - OCR_LANGUAGES=eng,chi_sim

docker-compose up -d
```

### Dockerfile示例
```dockerfile
FROM openjdk:17-jdk-slim

# 安装Tesseract OCR和中文字体
RUN apt-get update && \
    apt-get install -y tesseract-ocr \
                       tesseract-ocr-chi-sim \
                       tesseract-ocr-chi-tra \
                       fonts-wqy-microhei \
                       fonts-noto-cjk && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# 复制应用程序
COPY target/pdf-tool-app.jar app.jar

# 设置环境变量
ENV SPRING_PROFILES_ACTIVE=prod
ENV TESSDATA_PREFIX=/usr/share/tesseract-ocr/4.00/tessdata

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 🔧 环境变量配置

以下环境变量可用于覆盖配置文件设置：

```bash
# 数据库配置
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=pdfapp
export DB_USER=pdfuser
export DB_PASSWORD=pdfpassword

# Tesseract配置
export TESSERACT_PATH=/usr/bin/tesseract
export TESSDATA_PREFIX=/usr/share/tesseract-ocr/4.00/tessdata
export OCR_LANGUAGES=eng,chi_sim

# 应用配置
export UPLOAD_PATH=/app/uploads
export MAX_FILE_SIZE=100MB
```

## 📋 部署检查清单

### 部署前检查
- [ ] Tesseract OCR已安装
- [ ] 中文语言包已安装
- [ ] 中文字体已安装
- [ ] 数据库已配置
- [ ] 上传目录权限正确

### 验证安装
```bash
# 检查Tesseract安装
tesseract --version

# 检查支持的语言
tesseract --list-langs

# 检查字体
fc-list :lang=zh

# 测试OCR功能
curl -X POST http://localhost:8080/api/pdf-tools/ocr/test
```

## ⚠️ 常见问题

### 1. Tesseract库加载失败
**问题**: `UnsatisfiedLinkError: Unable to load library 'tesseract'`

**解决方案**:
```bash
# 检查Tesseract是否正确安装
which tesseract
tesseract --version

# 设置库路径（如果需要）
export LD_LIBRARY_PATH=/usr/lib:/usr/local/lib:$LD_LIBRARY_PATH
```

### 2. 中文字体不支持
**问题**: 水印中文字符显示为方块或X

**解决方案**:
```bash
# 安装中文字体
sudo apt-get install fonts-wqy-microhei

# 检查字体是否安装
fc-list :lang=zh
```

### 3. tessdata路径错误
**问题**: `Error opening data file`

**解决方案**:
- 检查 `TESSDATA_PREFIX` 环境变量
- 确认tessdata目录存在且可读
- 检查应用配置文件中的路径设置

## 🔄 不同系统的路径差异

| 系统 | Tesseract路径 | tessdata路径 | 中文字体路径示例 |
|------|---------------|--------------|------------------|
| Ubuntu/Debian | `/usr/bin/tesseract` | `/usr/share/tesseract-ocr/4.00/tessdata` | `/usr/share/fonts/truetype/wqy/` |
| CentOS/RHEL | `/usr/bin/tesseract` | `/usr/share/tesseract/tessdata` | `/usr/share/fonts/chinese/` |
| macOS (Homebrew) | `/opt/homebrew/bin/tesseract` | `/opt/homebrew/share/tessdata` | `/System/Library/Fonts/` |
| Docker | `/usr/bin/tesseract` | `/usr/share/tesseract-ocr/4.00/tessdata` | `/usr/share/fonts/` |

## 🎯 性能优化建议

1. **内存配置**: 为OCR操作分配足够内存
   ```bash
   export JAVA_OPTS="-Xmx2g -Xms1g"
   ```

2. **线程池配置**: 根据服务器性能调整PDF处理线程数
   ```yml
   app.pdf.processing.max-pool-size: 8
   ```

3. **文件存储**: 使用SSD存储临时文件以提高处理速度 