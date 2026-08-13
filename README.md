# 海洋鱼类智能化信息管理系统

这是一个面向海洋鱼类知识学习、智能识别和公益救助场景的前后端分离项目。系统支持鱼类资料管理、用户与权限管理、图片识别、资料纠错申请、志愿者申请、救助工单流转和后台统计。

当前已经完成可运行的全栈 MVP（最小可用版本），覆盖前端、后端、数据库、真实 AI 识别服务、自动化测试和容器化部署。

## 技术栈

- 后端：Java 17、Spring Boot 3、Spring Security、JWT、MyBatis-Plus
- 数据库：MySQL 8
- 测试：JUnit 5、Mockito、MockMvc
- 前端：Vue 3、TypeScript、Vite、Element Plus、Pinia、Axios
- AI：Python、FastAPI、Ultralytics YOLO（已接入训练好的 `best.pt`）
- 工程工具：Maven、Git、GitHub

## 已完成的后端模块

1. 用户注册、登录、JWT 身份认证、个人资料修改
2. 普通用户、志愿者、管理员三类角色与接口权限控制
3. 管理员查询用户、启用和停用账号
4. 鱼类资料的新增、查询、修改、逻辑删除、分页和条件搜索
5. 志愿者申请、管理员审核和角色授予
6. 救助工单上报、审核、公开、接单、完成反馈和管理员验收
7. 图片文件私有存储、识别记录、识别结果和失败原因保存
8. 未收录鱼类自动生成待处理任务，管理员确认后关联鱼类资料
9. 用户提交鱼类新增或纠错申请，管理员审核后写入资料库
10. 管理后台数据概览和状态统计

## 已完成的前端模块

1. 海洋主题公共首页、响应式布局和公共导航
2. 用户注册、登录、JWT 保存、退出和个人资料修改
3. 鱼类图鉴搜索、分页和鱼类详情
4. 图片上传识别、识别结果和个人识别记录
5. 救助公告、问题上报和个人工单状态查询
6. 志愿者申请、公开工单接单和完成反馈
7. 鱼类资料新增/纠错申请及申请记录
8. 管理员统计、用户管理、志愿者审核和救助审核
9. 管理员鱼类资料维护、资料申请审核和未收录物种处理

## 重要业务流程

志愿者申请：

`PENDING -> APPROVED / REJECTED`

救助工单：

`PENDING_REVIEW -> OPEN -> ACCEPTED -> COMPLETION_PENDING -> COMPLETED`

资料申请：

`PENDING -> APPROVED / REJECTED`

## 目录说明

```text
SeaFishDetection-re/
├─ backend/                 Spring Boot 后端
│  ├─ src/main/java/        业务源代码
│  ├─ src/test/java/        自动化测试
│  └─ http/                 IDEA HTTP 接口调试文件
├─ frontend/                Vue 3 + TypeScript 前端
├─ ai-service/              FastAPI + YOLO 真实识别服务和模型
├─ database/migrations/     V001-V007 数据库升级脚本
└─ docs/                    需求、设计和学习笔记
```

## 本地运行

1. 安装 Java 17、MySQL 8 和 Maven（也可使用项目 Maven Wrapper）。
2. 创建数据库 `seafish_db`。
3. 按编号顺序执行 `database/migrations` 中的 V001 到 V007。
4. 在 `backend/src/main/resources/application-local.yml` 中填写本机数据库连接和 JWT 密钥。该文件已被 Git 忽略，不会上传密码。
5. 在 `backend` 目录运行（也可以直接在 IDEA 中运行 `SeaFishApplication`）：

```powershell
mvn spring-boot:run
```

服务默认地址为 `http://localhost:8081`。

## 运行测试

```powershell
cd backend
mvn test
```

当前完整测试结果：97 个测试全部通过。

## 图片识别说明

项目使用 `FishDetectionClient` 接口隔离具体 AI 实现。开发环境默认使用 `DemoFishDetectionClient`，无需安装 Python 也能调试业务；将 `SEAFISH_DETECTION_PROVIDER` 设置为 `yolo-service` 后，Spring Boot 会调用独立 FastAPI 服务和真实 `best.pt` 模型，并保存带检测框的结果图。

AI 服务代码位于 `ai-service`，接口文档地址为 `http://localhost:8000/docs`。

## 一键验证和启动

```powershell
.\scripts\verify.ps1
.\scripts\start-dev.ps1
```

启用真实 AI：

```powershell
.\scripts\start-dev.ps1 -UseRealAi
```

也可以复制 `.env.example` 为 `.env`，填写密码与 JWT 密钥后运行 `docker compose up --build`，一次启动 MySQL、AI、后端和前端。

## 接口调试

IDEA 可以直接运行 `backend/http` 目录中的 `.http` 文件。需要登录的接口先调用登录接口，再把返回的 token 放入请求头：

```http
Authorization: Bearer 你的token
```

## 项目文档

- [需求说明](docs/requirements.md)
- [系统设计](docs/system-design.md)
- [数据库设计](docs/database-design.md)
- [Spring Boot 启动学习笔记](docs/learning/01-spring-boot-bootstrap.md)
- [项目演示与答辩流程](docs/demo-guide.md)
