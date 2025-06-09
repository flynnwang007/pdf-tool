# 🚀 服务器部署快速指南

## 📋 部署前准备

### 1. 服务器要求
- Ubuntu 20.04+ / CentOS 8+ / Debian 11+
- 最低配置：2核CPU，4GB内存，20GB磁盘
- 推荐配置：4核CPU，8GB内存，50GB磁盘

### 2. 必要端口
确保以下端口开放：
- 80 (HTTP)
- 443 (HTTPS)
- 22 (SSH)

## 🔧 快速部署命令

### 1. 安装Docker和Docker Compose
```bash
# 更新系统
sudo apt update && sudo apt upgrade -y

# 安装Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER

# 安装Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# 重新登录以应用docker用户组
newgrp docker
```

### 2. 克隆项目
```bash
# 克隆项目
git clone https://github.com/flynnwang007/pdf-tool.git
cd pdf-tool
```

### 3. 配置生产环境
```bash
# 复制本地配置作为生产配置模板
cp docker-compose.local.yml docker-compose.prod.yml

# 编辑生产配置
nano docker-compose.prod.yml
```

### 4. 生产环境配置修改
在 `docker-compose.prod.yml` 中修改以下内容：

```yaml
services:
  postgres:
    environment:
      POSTGRES_PASSWORD: "YOUR_STRONG_PASSWORD_HERE"  # 修改数据库密码
    volumes:
      - /var/lib/pdf-app/postgres:/var/lib/postgresql/data  # 持久化存储

  redis:
    volumes:
      - /var/lib/pdf-app/redis:/data  # 持久化存储

  pdf-backend:
    environment:
      - DB_PASSWORD=YOUR_STRONG_PASSWORD_HERE  # 与数据库密码一致
      - JWT_SECRET=your-super-secret-jwt-key-at-least-32-chars-long
      - SPRING_PROFILES_ACTIVE=prod
    volumes:
      - /var/lib/pdf-app/uploads:/app/uploads  # 持久化文件存储

  pdf-frontend:
    environment:
      - VITE_API_BASE_URL=https://your-domain.com/api  # 修改为你的域名
    ports:
      - "80:80"  # 如果要直接暴露80端口
```

### 5. 创建数据目录
```bash
sudo mkdir -p /var/lib/pdf-app/{postgres,redis,uploads}
sudo chown -R $USER:$USER /var/lib/pdf-app
```

### 6. 部署应用
```bash
# 给部署脚本执行权限
chmod +x docker-deploy.sh

# 构建后端
cd backend && ./gradlew bootJar && cd ..

# 启动生产环境
docker-compose -f docker-compose.prod.yml up --build -d
```

### 7. 验证部署
```bash
# 检查容器状态
docker ps

# 检查服务健康
curl http://localhost:8080/actuator/health
curl http://localhost:3000

# 查看日志
docker logs pdf-backend-local
docker logs pdf-frontend-local
```

## 🔒 SSL证书配置（可选）

### 使用Let's Encrypt
```bash
# 安装certbot
sudo apt install certbot

# 获取SSL证书
sudo certbot certonly --standalone -d your-domain.com

# 配置nginx SSL
# 修改 frontend/nginx.conf 添加SSL配置
```

### SSL配置示例
```nginx
server {
    listen 80;
    server_name your-domain.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl;
    server_name your-domain.com;
    
    ssl_certificate /etc/letsencrypt/live/your-domain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/your-domain.com/privkey.pem;
    
    # 其他配置...
}
```

## 🔧 生产环境管理

### 常用命令
```bash
# 查看状态
docker-compose -f docker-compose.prod.yml ps

# 查看日志
docker-compose -f docker-compose.prod.yml logs -f

# 重启服务
docker-compose -f docker-compose.prod.yml restart

# 停止服务
docker-compose -f docker-compose.prod.yml down

# 更新应用
git pull
cd backend && ./gradlew bootJar && cd ..
docker-compose -f docker-compose.prod.yml up --build -d
```

### 备份脚本
```bash
#!/bin/bash
# 创建 backup.sh

DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/var/backups/pdf-app"

mkdir -p $BACKUP_DIR

# 备份数据库
docker exec pdf-postgres-local pg_dump -U pdfuser pdfapp > $BACKUP_DIR/db_$DATE.sql

# 备份上传文件
tar -czf $BACKUP_DIR/uploads_$DATE.tar.gz -C /var/lib/pdf-app uploads

echo "备份完成: $DATE"
```

## 🚨 故障排除

### 常见问题
1. **端口占用**: 检查并修改端口映射
2. **内存不足**: 增加服务器内存或优化配置
3. **磁盘空间**: 清理Docker镜像和日志文件
4. **网络问题**: 检查防火墙和安全组设置

### 监控命令
```bash
# 系统资源
htop
df -h
free -m

# Docker资源
docker stats
docker system df
```

## 📱 访问应用

部署成功后，你可以通过以下地址访问应用：
- **前端**: http://your-server-ip:3000 或 https://your-domain.com
- **后端API**: http://your-server-ip:8080 或 https://your-domain.com/api
- **API文档**: http://your-server-ip:8080/swagger-ui.html

## 🎯 下一步

1. 配置域名DNS解析
2. 设置SSL证书
3. 配置监控和日志
4. 设置定期备份
5. 优化性能配置

祝你部署顺利！🚀 