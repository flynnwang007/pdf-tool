-- PostgreSQL 数据库初始化脚本
-- 此脚本会在数据库容器首次启动时执行

-- 创建uuid扩展，用于生成UUID
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 数据库注释
-- 实际的表结构将由Spring Boot JPA自动创建和管理 