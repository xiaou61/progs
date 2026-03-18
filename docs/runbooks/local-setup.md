# 本地开发环境说明

## 目录说明

- `admin-web`：后台管理端
- `miniapp`：微信小程序端
- `server`：后端服务
- `infra`：本地依赖与部署占位文件

## 基础依赖

- Node.js 22+
- pnpm 10+
- Maven 3.9+
- JDK 17
- Docker Desktop

## 本地依赖启动

```bash
docker compose -f infra/docker-compose.yml up -d
```

启动后默认端口：

- MySQL：`3306`
- Redis：`6379`
- MinIO API：`9000`
- MinIO Console：`9001`

## 后续应用启动顺序

1. 启动基础依赖
2. 启动 `server`
3. 启动 `admin-web`
4. 启动 `miniapp`
