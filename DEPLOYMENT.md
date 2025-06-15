# PDF工具云服务器部署指南

## 🚀 快速部署

### 1. 服务器环境准备

```bash
# 更新系统
sudo apt update && sudo apt upgrade -y

# 安装必要软件
sudo apt install -y git curl nginx openjdk-17-jdk nodejs npm

# 克隆项目
git clone https://github.com/flynnwang007/pdf-tool.git
cd pdf-tool
```

### 2. 配置环境变量

创建 `.env` 文件：

```bash
cp .env.example .env  # 如果有示例文件
# 或者手动创建
nano .env
```

`.env` 文件内容（**必须配置**）：

```env
# MemFireDB 配置（必须）
VITE_SUPABASE_URL=https://d11558og91hm5619qfbg.baseapi.memfiredb.com
VITE_SUPABASE_ANON_KEY=your-anon-key-here
MEMFIRE_JWT_SECRET=0d37e31b-3452-4949-8e19-04bc619c78c9

# 其他配置
MEMFIRE_DB_URL=jdbc:postgresql://db.d11558og91hm5619qfbg.supabase.co:5432/postgres
SPRING_PROFILES_ACTIVE=memfire
```

### 3. 部署后端

```bash
cd backend
./deploy-backend.sh
```

### 4. 部署前端

```bash
cd frontend
./deploy-simple.sh
```

### 5. 验证部署

```bash
cd ..
./deploy-check.sh
```

## 🔧 详细配置

### Nginx 配置

创建 `/etc/nginx/sites-available/pdf-tool`：

```nginx
# 前端服务 (端口 8081)
server {
    listen 8081;
    server_name www.aibase123.cn;
    
    root /var/www/pdf-tool;
    index index.html;
    
    location / {
        try_files $uri $uri/ /index.html;
    }
    
    # API代理到后端
    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}

# 后端服务 (端口 8080) - 可选的反向代理
server {
    listen 8080;
    server_name www.aibase123.cn;
    
    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

启用配置：

```bash
sudo ln -s /etc/nginx/sites-available/pdf-tool /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

### 防火墙配置

```bash
sudo ufw allow 8080/tcp
sudo ufw allow 8081/tcp
sudo ufw allow 22/tcp
sudo ufw --force enable
```

## 📋 部署检查清单

### ✅ 部署前检查

- [ ] 服务器环境准备完成
- [ ] `.env` 文件配置正确
- [ ] MemFireDB JWT密钥已获取
- [ ] Nginx配置已更新
- [ ] 防火墙端口已开放

### ✅ 部署后验证

- [ ] 后端服务启动成功 (`curl http://www.aibase123.cn/actuator/health`)
- [ ] 前端服务访问正常 (`curl http://www.aibase123.cn`)
- [ ] JWT验证功能正常
- [ ] API认证工作正常
- [ ] 无HMR变量残留

## 🐛 故障排除

### 后端问题

```bash
# 查看应用日志
tail -f backend/logs/app.log

# 查看启动日志
tail -f backend/logs/nohup.log

# 检查端口占用
netstat -tulpn | grep :8080

# 手动测试JWT
cd backend && node test-jwt-simple.js
```

### 前端问题

```bash
# 检查构建产物
ls -la frontend/dist/

# 检查nginx错误日志
sudo tail -f /var/log/nginx/error.log

# 检查HMR变量残留
grep -r "__DEFINES__\|__HMR_" frontend/dist/
```

### 常见错误解决

1. **JWT验证失败**
   - 检查 `MEMFIRE_JWT_SECRET` 是否正确设置
   - 确认密钥长度为36字符
   - 重启后端服务

2. **前端白屏/紫屏**
   - 检查HMR变量是否完全替换
   - 重新运行前端部署脚本
   - 检查控制台错误信息

3. **API 403错误**
   - 确认JWT令牌正确传递
   - 检查CORS配置
   - 验证认证过滤器配置

## 🔄 更新部署

```bash
# 拉取最新代码
git pull origin main

# 重新部署后端
cd backend && ./deploy-backend.sh

# 重新部署前端
cd ../frontend && ./deploy-simple.sh

# 验证部署
cd .. && ./deploy-check.sh
```

## 📊 监控

### 服务状态监控

   ```bash
# 检查Java进程
ps aux | grep java

# 检查端口监听
netstat -tulpn | grep -E ":8080|:8081"

# 检查nginx状态
sudo systemctl status nginx
```

### 日志监控

```bash
# 实时查看应用日志
tail -f backend/logs/app.log

# 查看nginx访问日志
sudo tail -f /var/log/nginx/access.log
```

## 🔐 安全建议

1. 定期更新JWT密钥
2. 使用HTTPS（可配置SSL证书）
3. 限制API访问频率
4. 定期备份数据库
5. 监控异常访问

## 📞 技术支持

- 项目地址: https://github.com/flynnwang007/pdf-tool
- 前端访问: http://www.aibase123.cn
- 后端API: http://www.aibase123.cn
- 健康检查: http://www.aibase123.cn/actuator/health 