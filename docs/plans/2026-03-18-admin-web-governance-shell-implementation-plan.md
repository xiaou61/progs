# 后台管理端补齐实施计划

## Task 1：登录态与路由测试

- 新增后台认证 store 测试
- 新增菜单结构测试
- 新增路由 meta 和守卫测试

## Task 2：后台公共壳

- 新增 `AdminLayout.vue`
- 调整 `router/index.ts` 为嵌套路由
- 在 `App.vue` 中承载布局
- 新增统一菜单定义和标题解析工具

## Task 3：后台登录闭环

- 重写 `LoginPage.vue`
- 新增管理员登录 store
- 登录成功后跳转工作台
- 顶部支持退出登录
- 启动时自动恢复会话

## Task 4：请求鉴权

- 改造 `api/http.ts`
- 自动附带 `Authorization`
- 保留现有 `request` / `requestText` API 形态不变

## Task 5：页面接线补齐

- 校区管理页补齐 loading / error / empty
- 轮播图管理页改为真实 API
- 系统配置页改为真实 API
- 将各业务页纳入后台菜单体系

## Task 6：验证

- 运行 `admin-web` 单测
- 运行 `admin-web` 构建
- 检查路由、登录、菜单和 API 层是否有类型错误
