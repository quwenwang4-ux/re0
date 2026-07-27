# Spring Boot 后端启动学习笔记

## 1. 本节目标

本节只完成一个最小目标：启动 Java 后端，并通过浏览器访问健康检查接口。

请求地址：

```text
GET http://localhost:8080/api/health
```

预期响应：

```text
SeaFish backend is running
```

## 2. 当前目录

```text
backend
├── pom.xml
└── src
    ├── main
    │   ├── java
    │   │   └── com/seafish
    │   │       ├── SeaFishApplication.java
    │   │       └── controller
    │   │           └── HealthController.java
    │   └── resources
    │       └── application.yml
    └── test
        └── java
            └── com/seafish
                └── SeaFishApplicationTests.java
```

## 3. 各文件职责

- `pom.xml`：Maven 项目说明，管理项目坐标、Java 版本、依赖和构建插件。
- `SeaFishApplication.java`：程序入口，启动 Spring 容器和内置 Web 服务器。
- `HealthController.java`：接收 `/api/health` 请求并返回文本。
- `application.yml`：保存应用名称和端口等外部配置。
- `SeaFishApplicationTests.java`：验证 Spring 应用上下文能否正常启动。

## 4. 请求经过的路径

```text
浏览器
→ http://localhost:8080/api/health
→ Spring 内置 Tomcat
→ HealthController.health()
→ 返回字符串
→ 浏览器显示响应
```

## 5. 本节暂不包含

- 数据库连接。
- MyBatis-Plus。
- 用户注册和登录。
- JWT 和角色权限。
- 统一响应格式。
- 全局异常处理。

这些能力将在最小服务成功运行后逐步加入。
