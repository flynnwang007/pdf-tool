# PDF工具 Docker 部署指南

## 📋 概述

本文档介绍如何使用 Docker 在本地和云服务器上部署 PDF 工具应用。

## 🏗️ 架构说明

### 服务组件
- **前端 (pdf-frontend)**: Vue.js + Nginx，端口 3000
- **后端 (pdf-backend)**: Spring Boot，端口 8080
- **数据库 (postgres)**: PostgreSQL 15，端口 5432
- **缓存 (redis)**: Redis 7，端口 6379

### 网络架构
```
用户 → Nginx (前端) → Spring Boot (后端) → PostgreSQL + Redis
```

## 🚀 本地部署

### 前置要求
- Docker Desktop (推荐最新版本)
- 至少 4GB 可用内存
- 至少 10GB 可用磁盘空间

### 快速部署
```bash
# 1. 克隆项目（如果还没有）
git clone <your-repo-url>
cd pdf_tool

# 2. 运行部署脚本
./docker-deploy.sh
```

### 手动部署
```bash
# 1. 构建后端应用
cd backend
./gradlew bootJar
cd ..

# 2. 启动 Docker 服务
docker-compose -f docker-compose.local.yml up --build -d

# 3. 检查服务状态
docker ps
```

### 访问应用
- **前端应用**: http://localhost:3000
- **后端API**: http://localhost:8080
- **API文档**: http://localhost:8080/swagger-ui.html
- **健康检查**: http://localhost:8080/actuator/health

## 🌐 云服务器部署

### 服务器要求
- Ubuntu 20.04+ / CentOS 8+ / Debian 11+
- 2核CPU，4GB内存（最低配置）
- 20GB 可用磁盘空间
- Docker 和 Docker Compose

### 安装 Docker
```bash
# Ubuntu/Debian
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER

# 安装 Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
```

### 部署步骤
```bash
# 1. 上传项目文件到服务器
scp -r pdf_tool user@your-server:/home/user/

# 2. 登录服务器
ssh user@your-server

# 3. 进入项目目录
cd pdf_tool

# 4. 修改生产环境配置
cp docker-compose.local.yml docker-compose.prod.yml

# 5. 编辑生产配置（见下方配置说明）
nano docker-compose.prod.yml

# 6. 部署应用
docker-compose -f docker-compose.prod.yml up --build -d
```

### 生产环境配置修改

#### 1. 安全配置
```yaml
# 修改数据库密码
environment:
  - POSTGRES_PASSWORD=your-strong-password-here
  - DB_PASSWORD=your-strong-password-here
  - JWT_SECRET=your-super-secret-jwt-key-at-least-32-chars

# 修改端口映射（可选）
ports:
  - "80:80"    # 前端
  - "8080:8080" # 后端（建议通过反向代理访问）
```

#### 2. 域名和SSL配置
```yaml
# 前端环境变量
environment:
  - VITE_API_BASE_URL=https://your-domain.com/api
```

#### 3. 数据持久化
```yaml
volumes:
  - /var/lib/pdf-app/postgres:/var/lib/postgresql/data
  - /var/lib/pdf-app/redis:/data
  - /var/lib/pdf-app/uploads:/app/uploads
```

## 🔧 管理命令

### 查看服务状态
```bash
docker ps
docker-compose -f docker-compose.local.yml ps
```

### 查看日志
```bash
# 查看所有服务日志
docker-compose -f docker-compose.local.yml logs

# 查看特定服务日志
docker logs pdf-backend-local
docker logs pdf-frontend-local
docker logs pdf-postgres-local
docker logs pdf-redis-local
```

### 重启服务
```bash
# 重启所有服务
docker-compose -f docker-compose.local.yml restart

# 重启特定服务
docker-compose -f docker-compose.local.yml restart pdf-backend
```

### 停止服务
```bash
# 停止所有服务
docker-compose -f docker-compose.local.yml down

# 停止并删除数据卷（谨慎使用）
docker-compose -f docker-compose.local.yml down -v
```

### 更新应用
```bash
# 1. 拉取最新代码
git pull

# 2. 重新构建后端
cd backend && ./gradlew bootJar && cd ..

# 3. 重新构建并启动
docker-compose -f docker-compose.local.yml up --build -d
```

## 🔍 故障排除

### 常见问题

#### 1. 端口冲突
```bash
# 检查端口占用
lsof -i :3000
lsof -i :8080
lsof -i :5432

# 修改 docker-compose.yml 中的端口映射
ports:
  - "3001:80"   # 前端改为3001
  - "8081:8080" # 后端改为8081
```

#### 2. 内存不足
```bash
# 检查内存使用
docker stats

# 增加 Docker 内存限制或清理不用的镜像
docker system prune -a
```

#### 3. 数据库连接失败
```bash
# 检查数据库日志
docker logs pdf-postgres-local

# 检查网络连接
docker network ls
docker network inspect pdf_tool_pdf-network
```

#### 4. 前端无法访问后端
```bash
# 检查 nginx 配置
docker exec pdf-frontend-local cat /etc/nginx/conf.d/default.conf

# 检查后端健康状态
curl http://localhost:8080/actuator/health
```

### 性能优化

#### 1. 数据库优化
```yaml
# 在 docker-compose.yml 中添加
postgres:
  command: postgres -c shared_preload_libraries=pg_stat_statements -c pg_stat_statements.track=all
  environment:
    - POSTGRES_SHARED_PRELOAD_LIBRARIES=pg_stat_statements
```

#### 2. Redis 优化
```yaml
redis:
  command: redis-server --maxmemory 256mb --maxmemory-policy allkeys-lru
```

#### 3. 应用优化
```yaml
pdf-backend:
  environment:
    - JAVA_OPTS=-Xmx1g -Xms512m
```

## 📊 监控和日志

### 日志管理
```bash
# 配置日志轮转
docker-compose -f docker-compose.local.yml logs --tail=100 -f

# 导出日志
docker logs pdf-backend-local > backend.log 2>&1
```

### 健康检查
```bash
# 检查所有服务健康状态
curl http://localhost:8080/actuator/health
curl http://localhost:3000
```

## 🔐 安全建议

1. **更改默认密码**: 修改数据库和应用的默认密码
2. **使用HTTPS**: 在生产环境中配置SSL证书
3. **防火墙配置**: 只开放必要的端口
4. **定期更新**: 保持Docker镜像和依赖的最新版本
5. **备份数据**: 定期备份数据库和上传文件

## 📝 备份和恢复

### 数据备份
```bash
# 备份数据库
docker exec pdf-postgres-local pg_dump -U pdfuser pdfapp > backup.sql

# 备份上传文件
docker cp pdf-backend-local:/app/uploads ./uploads-backup
```

### 数据恢复
```bash
# 恢复数据库
docker exec -i pdf-postgres-local psql -U pdfuser pdfapp < backup.sql

# 恢复上传文件
docker cp ./uploads-backup pdf-backend-local:/app/uploads
```

## 📞 支持

如果遇到问题，请：
1. 查看日志文件
2. 检查 GitHub Issues
3. 联系技术支持

---

**注意**: 本文档基于当前版本编写，部署前请确认版本兼容性。 