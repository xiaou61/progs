# 后台创建用户与双端界面收敛 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 为后台补齐创建用户闭环，并将 `wechat-native` 明确收口为“只登录不注册”，同时提升后台与用户端关键页面的成品感。

**Architecture:** 继续复用现有 Spring Boot + Vue 3 + 微信原生小程序结构，不重做整体架构。后端新增管理员创建用户接口，后台用户治理页接入创建与违规治理，`wechat-native` 只调整现有高频页面的信息架构和样式。

**Tech Stack:** Spring Boot 3、MyBatis-Plus、Vue 3、Pinia、Vue Router、Vitest、微信原生小程序

---

### Task 1: 文档与上下文对齐

**Files:**
- Create: `docs/plans/2026-03-18-admin-user-provisioning-and-ui-refresh-design.md`
- Create: `docs/plans/2026-03-18-admin-user-provisioning-and-ui-refresh-implementation-plan.md`

**Step 1: 保存确认后的设计文档**

将账号流程、后台能力和双端视觉收敛方案写入设计文档。

**Step 2: 保存实施计划**

把后续执行拆成可验证的任务，避免边做边扩。

### Task 2: 后端管理员创建用户接口

**Files:**
- Modify: `server/src/test/java/com/campus/competition/modules/user/AdminUserGovernanceApiTest.java`
- Modify: `server/src/test/java/com/campus/competition/modules/user/AdminGovernanceDeepeningApiTest.java`
- Modify: `server/src/main/java/com/campus/competition/modules/user/controller/AdminUserController.java`
- Modify: `server/src/main/java/com/campus/competition/modules/user/service/AdminUserService.java`
- Modify: `server/src/main/java/com/campus/competition/modules/user/service/AdminUserServiceImpl.java`
- Create: `server/src/main/java/com/campus/competition/modules/user/model/CreateUserCommand.java`

**Step 1: 写创建用户失败测试**

补一个后台创建用户 API 测试，断言调用 `POST /api/admin/users` 前接口不存在或行为不满足预期。

**Step 2: 运行单测确认红灯**

Run: `mvn -f server/pom.xml -Dtest=AdminUserGovernanceApiTest,AdminGovernanceDeepeningApiTest test`

Expected: 出现创建用户相关断言失败。

**Step 3: 写最小实现**

- 新增 `CreateUserCommand`
- 在 `AdminUserService` 增加 `createUser`
- 在 `AdminUserServiceImpl` 中完成校验、角色检查、密码加密、用户写库、日志记录
- 在 `AdminUserController` 暴露 `POST /api/admin/users`

**Step 4: 运行单测确认绿灯**

Run: `mvn -f server/pom.xml -Dtest=AdminUserGovernanceApiTest,AdminGovernanceDeepeningApiTest test`

Expected: 相关测试通过。

### Task 3: 后台 API client 与用户治理交互

**Files:**
- Modify: `admin-web/src/__tests__/api-clients.spec.ts`
- Modify: `admin-web/src/api/users.ts`
- Modify: `admin-web/src/views/system/users/UserListPage.vue`

**Step 1: 先写前端 API 测试**

为 `createUser` API client 补失败前测试，断言请求路径、方法和请求体。

**Step 2: 运行测试确认红灯**

Run: `pnpm --dir admin-web vitest run src/__tests__/api-clients.spec.ts`

Expected: 因 `createUser` 未实现而失败。

**Step 3: 写最小实现**

- 在 `admin-web/src/api/users.ts` 增加 `createUser`
- 在用户治理页增加新建用户表单
- 接入违规标记/解除按钮
- 操作后刷新列表并更新提示信息

**Step 4: 运行测试确认绿灯**

Run: `pnpm --dir admin-web vitest run src/__tests__/api-clients.spec.ts`

Expected: API client 测试通过。

### Task 4: 后台用户治理页和壳层美化

**Files:**
- Modify: `admin-web/src/views/system/users/UserListPage.vue`
- Modify: `admin-web/src/layouts/AdminLayout.vue`

**Step 1: 写交互层验证测试或补现有测试覆盖**

优先在现有测试文件中补菜单/治理能力相关断言，至少覆盖用户治理新增能力的可见性和表单状态。

**Step 2: 运行测试确认红灯**

Run: `pnpm --dir admin-web vitest run src/__tests__/admin-shell.spec.ts src/__tests__/admin-auth.spec.ts src/__tests__/permission-menu.spec.ts`

Expected: 新断言失败。

**Step 3: 写最小实现**

- 优化后台壳层品牌区、选中态、顶部信息卡和内容区容器
- 用户治理页改成更清晰的双栏/分区布局

**Step 4: 运行测试确认绿灯**

Run: `pnpm --dir admin-web vitest run src/__tests__/admin-shell.spec.ts src/__tests__/admin-auth.spec.ts src/__tests__/permission-menu.spec.ts`

Expected: 测试通过。

### Task 5: wechat-native 登录页、首页、个人中心收敛

**Files:**
- Modify: `wechat-native/pages/login/index.wxml`
- Modify: `wechat-native/pages/login/index.wxss`
- Modify: `wechat-native/pages/home/index.wxml`
- Modify: `wechat-native/pages/home/index.wxss`
- Modify: `wechat-native/pages/profile/index.wxml`
- Modify: `wechat-native/pages/profile/index.wxss`
- Modify: `wechat-native/app.wxss`

**Step 1: 先做最小结构调整**

不改已有业务调用，先删减多余说明、重组卡片顺序、统一公共视觉变量和卡片语义。

**Step 2: 运行人工核查**

检查页面是否仍只依赖现有 JS 数据结构，不引入不存在的字段或事件。

**Step 3: 写样式实现**

- 登录页突出“账号由管理员开通”
- 首页改成更聚焦的欢迎区、任务区、入口区
- 个人中心压缩说明文案、收敛分组层级

**Step 4: 本地回归检查**

检查 WXML/WXSS 与现有 JS 字段保持一致，避免绑定失效。

### Task 6: 全量验证

**Files:**
- Modify: `admin-web/src/__tests__/api-clients.spec.ts`
- Modify: `server/src/test/java/com/campus/competition/modules/user/AdminUserGovernanceApiTest.java`
- Modify: `server/src/test/java/com/campus/competition/modules/user/AdminGovernanceDeepeningApiTest.java`

**Step 1: 运行后台前端测试**

Run: `pnpm --dir admin-web test`

Expected: `vitest` 全通过。

**Step 2: 运行后台前端构建**

Run: `pnpm --dir admin-web build`

Expected: 构建成功，无类型错误。

**Step 3: 运行后端针对性测试**

Run: `mvn -f server/pom.xml -Dtest=AdminUserGovernanceApiTest,AdminGovernanceDeepeningApiTest test`

Expected: 管理员创建用户相关测试通过。

**Step 4: 汇总账号流程说明**

最终对用户说明：

- 后台管理员创建账号
- 用户端只登录不注册
- 首次登录后在个人中心修改密码
