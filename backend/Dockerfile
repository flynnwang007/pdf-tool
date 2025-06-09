# 使用Java运行时镜像
FROM eclipse-temurin:17-jdk

# 创建应用目录
WORKDIR /app

# 创建上传和临时目录
RUN mkdir -p /app/uploads /tmp/pdf-processing

# 复制已构建的jar文件
COPY build/libs/*.jar app.jar

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
    CMD java -cp app.jar org.springframework.boot.loader.JarLauncher --help > /dev/null || exit 1

# 启动应用
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"] 