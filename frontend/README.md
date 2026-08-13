# 蓝境前端

海洋鱼类智能化信息管理系统的 Vue 3 前端。

## 技术栈

- Vue 3 + TypeScript + Vite
- Vue Router：页面路由和登录权限拦截
- Pinia：保存当前用户、角色和 token
- Axios：调用 Spring Boot 接口
- Element Plus：表单、提示、分页等基础组件

## 本地启动

先运行端口为 `8081` 的 Spring Boot 后端，再进入本目录执行：

```powershell
npm.cmd install
npm.cmd run dev
```

浏览器访问 `http://localhost:5173`。开发环境会把 `/api` 请求代理到 `http://localhost:8081`，因此不需要单独处理跨域。

## 生产编译

```powershell
npm.cmd run build
```

编译结果位于 `dist`，该目录不会提交到 Git。
