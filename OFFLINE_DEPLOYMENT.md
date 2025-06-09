# PDF 工具离线部署指南

本指南介绍如何通过本地构建镜像并上传到服务器的方式进行离线部署，解决服务器网络访问 Docker Hub 的问题。

## 部署概述

1. **本地构建**: 在本地机器构建所有 Docker 镜像
2. **镜像导出**: 将镜像导出为 tar 文件
3. **上传传输**: 将镜像文件上传到服务器
4. **服务器导入**: 在服务器上导入镜像
5. **启动服务**: 使用预构建镜像启动服务

## 文件说明

- `upload-images.sh`: 上传镜像到服务器的脚本
- `load-images.sh`: 在服务器上导入镜像的脚本
- `docker-compose.offline.yml`: 使用预构建镜像的 compose 文件
- `docker-images/`: 存储导出镜像的目录

## 步骤 1: 本地构建和导出（已完成）

在本地机器上已经完成了以下操作：

```bash
# 构建项目镜像
docker-compose -f docker-compose.server.yml build

# 拉取基础镜像
docker pull postgres:15
docker pull redis:alpine

# 导出所有镜像
docker save -o docker-images/pdf-backend.tar pdf_tool-pdf-backend:latest
docker save -o docker-images/pdf-frontend.tar pdf_tool-pdf-frontend:latest
docker save -o docker-images/postgres.tar postgres:15
docker save -o docker-images/redis.tar redis:alpine
```

## 步骤 2: 上传镜像到服务器

### 方法 1: 使用上传脚本

```bash
# 修改脚本中的服务器信息，然后执行
./upload-images.sh [服务器IP] [用户名]

# 例如:
./upload-images.sh 123.456.789.0 root
```

### 方法 2: 手动上传

```bash
# 创建远程目录
ssh root@your-server-ip "mkdir -p ~/pdf-tool/docker-images"

# 上传镜像文件
scp docker-images/*.tar root@your-server-ip:~/pdf-tool/docker-images/

# 上传部署文件
scp load-images.sh docker-compose.offline.yml init.sql root@your-server-ip:~/pdf-tool/
```

## 步骤 3: 服务器端操作

### 3.1 连接到服务器

```bash
ssh root@your-server-ip
cd ~/pdf-tool
```

### 3.2 导入镜像

```bash
# 使用脚本导入
chmod +x load-images.sh
./load-images.sh

# 或手动导入
docker load -i docker-images/pdf-backend.tar
docker load -i docker-images/pdf-frontend.tar
docker load -i docker-images/postgres.tar
docker load -i docker-images/redis.tar
```

### 3.3 验证镜像

```bash
# 查看导入的镜像
docker images | grep -E "(pdf_tool|postgres|redis)"
```

应该看到类似输出：
```
pdf_tool-pdf-backend    latest    da29420048fa   2 hours ago   299MB
pdf_tool-pdf-frontend   latest    69d44a1f1e07   2 hours ago   21MB
postgres                15        f57a3bdbf044   2 weeks ago   144MB
redis                   alpine    48501c5ad00d   3 weeks ago   23MB
```

### 3.4 启动服务

```bash
# 停止现有服务（如果有）
docker-compose down

# 使用离线配置启动
docker-compose -f docker-compose.offline.yml up -d

# 查看服务状态
docker-compose -f docker-compose.offline.yml ps
```

### 3.5 查看日志

```bash
# 查看所有服务日志
docker-compose -f docker-compose.offline.yml logs

# 查看特定服务日志
docker-compose -f docker-compose.offline.yml logs pdf-backend
docker-compose -f docker-compose.offline.yml logs pdf-frontend
```

## 步骤 4: 验证部署

### 4.1 检查服务状态

```bash
# 检查容器状态
docker ps

# 检查健康状态
docker-compose -f docker-compose.offline.yml ps
```

### 4.2 测试访问

```bash
# 测试后端 API
curl http://localhost:8080/api/health

# 测试前端
curl http://localhost:3001
```

### 4.3 浏览器访问

打开浏览器访问：`http://your-server-ip:3001`

## 镜像文件大小

- `pdf-backend.tar`: ~299MB
- `pdf-frontend.tar`: ~21MB  
- `postgres.tar`: ~144MB
- `redis.tar`: ~23MB
- **总计**: ~487MB

## 故障排除

### 1. 镜像导入失败

```bash
# 检查文件完整性
ls -lh docker-images/
md5sum docker-images/*.tar

# 重新导入特定镜像
docker load -i docker-images/pdf-backend.tar
```

### 2. 容器启动失败

```bash
# 查看详细日志
docker-compose -f docker-compose.offline.yml logs [service-name]

# 检查镜像是否存在
docker images | grep pdf_tool
```

### 3. 网络连接问题

```bash
# 检查网络
docker network ls
docker network inspect pdf_tool_pdf-network
```

### 4. 数据持久化问题

```bash
# 检查数据卷
docker volume ls
docker volume inspect pdf_tool_postgres_data
```

## 更新部署

当需要更新应用时：

1. 在本地重新构建镜像
2. 导出新镜像
3. 上传到服务器
4. 停止服务，导入新镜像，重启服务

```bash
# 服务器端更新流程
docker-compose -f docker-compose.offline.yml down
docker load -i docker-images/pdf-backend.tar  # 新镜像
docker load -i docker-images/pdf-frontend.tar # 新镜像
docker-compose -f docker-compose.offline.yml up -d
```

## 注意事项

1. **镜像版本**: 确保本地构建的镜像版本与 compose 文件中的版本一致
2. **网络安全**: 确保服务器防火墙正确配置端口 3001 和 8080
3. **数据备份**: 定期备份 PostgreSQL 数据
4. **资源监控**: 监控服务器资源使用情况
5. **日志管理**: 定期清理 Docker 日志文件

## 环境变量配置

如需修改配置，编辑 `docker-compose.offline.yml` 中的环境变量：

- 数据库密码: `POSTGRES_PASSWORD`
- Redis 密码: `SPRING_REDIS_PASSWORD`
- JVM 内存: `JAVA_OPTS`
- 日志级别: `LOGGING_LEVEL_ROOT` 