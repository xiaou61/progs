# 原生微信小程序迁移 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 新建一个微信开发者工具可直接打开运行的纯原生小程序端，并复刻现有 `miniapp/` 的主要业务能力。

**Architecture:** 保留 `miniapp/` 作为参考实现，新增 `wechat-native/` 目录作为标准原生小程序工程。公共请求层、登录态和导航工具先平移，页面按“比赛主链路 -> 用户高频页 -> 老师端页”的顺序重写。

**Tech Stack:** 微信原生小程序、WXML、WXSS、原生 `Page`/`App` API、`wx.request`、Node 内置 `node:test`

---

### Task 1: 落地原生端目录与基础配置

**Files:**
- Create: `wechat-native/app.js`
- Create: `wechat-native/app.json`
- Create: `wechat-native/app.wxss`
- Create: `wechat-native/project.config.json`
- Create: `wechat-native/sitemap.json`

**Step 1: 创建基础配置文件**

补齐标准原生小程序入口文件和页面注册表。

**Step 2: 验证目录结构**

检查 `wechat-native/` 可被微信开发者工具识别。

**Step 3: 提交基础骨架**

保留最小可运行空壳。

### Task 2: 迁移公共请求层与登录态

**Files:**
- Create: `wechat-native/services/http.js`
- Create: `wechat-native/services/auth.js`
- Create: `wechat-native/utils/session.js`
- Create: `wechat-native/utils/routes.js`
- Create: `wechat-native/tests/session.test.js`

**Step 1: 写登录态与路由工具失败测试**

覆盖会话存取、回跳路由拼装、默认首页回跳。

**Step 2: 运行测试确认失败**

Run: `node --test wechat-native/tests/session.test.js`

**Step 3: 实现最小公共层**

用原生 JS 实现 `session`、`routes` 和 `http`。

**Step 4: 运行测试确认通过**

Run: `node --test wechat-native/tests/session.test.js`

### Task 3: 迁移比赛与个人相关服务模块

**Files:**
- Create: `wechat-native/services/competition.js`
- Create: `wechat-native/services/registration.js`
- Create: `wechat-native/services/checkin.js`
- Create: `wechat-native/services/submission.js`
- Create: `wechat-native/services/result.js`
- Create: `wechat-native/services/daily-task.js`
- Create: `wechat-native/services/message.js`
- Create: `wechat-native/services/profile.js`
- Create: `wechat-native/services/dashboard.js`
- Create: `wechat-native/utils/competition.js`
- Create: `wechat-native/utils/home.js`
- Create: `wechat-native/utils/message.js`

**Step 1: 平移 API 协议层**

保持接口路径、字段命名和返回值一致。

**Step 2: 平移格式化与跳转工具**

保留比赛时间格式化、状态计算、聊天路由和首页菜单路由。

**Step 3: 进行模块级冒烟验证**

至少验证模块可被 Node 正常加载。

### Task 4: 迁移登录与首页

**Files:**
- Create: `wechat-native/pages/login/index.js`
- Create: `wechat-native/pages/login/index.json`
- Create: `wechat-native/pages/login/index.wxml`
- Create: `wechat-native/pages/login/index.wxss`
- Create: `wechat-native/pages/home/index.js`
- Create: `wechat-native/pages/home/index.json`
- Create: `wechat-native/pages/home/index.wxml`
- Create: `wechat-native/pages/home/index.wxss`

**Step 1: 先实现登录页**

支持演示账号切换、登录、回跳。

**Step 2: 再实现首页**

支持角色菜单、每日任务和退出登录。

**Step 3: 手工验证登录跳转**

验证未登录进入受保护页会跳转登录，登录后能返回目标页。

### Task 5: 迁移比赛主链路页面

**Files:**
- Create: `wechat-native/pages/competition/list/*`
- Create: `wechat-native/pages/competition/detail/*`
- Create: `wechat-native/pages/competition/register/*`
- Create: `wechat-native/pages/competition/checkin/*`
- Create: `wechat-native/pages/competition/submission/*`
- Create: `wechat-native/pages/competition/result/*`

**Step 1: 先迁比赛列表与详情**

打通“列表 -> 详情”。

**Step 2: 再迁报名、签到、上传、结果页**

打通主业务闭环。

**Step 3: 手工验证主链路**

验证登录后可完成比赛主链路中的关键动作。

### Task 6: 迁移消息、积分与个人中心

**Files:**
- Create: `wechat-native/pages/message/index/*`
- Create: `wechat-native/pages/message/chat/*`
- Create: `wechat-native/pages/points/index/*`
- Create: `wechat-native/pages/profile/index/*`

**Step 1: 迁移消息中心与会话页**

支持系统消息、会话列表、私信/群聊发送。

**Step 2: 迁移积分页与个人中心**

支持积分流水、个人资料、密码、反馈、注销。

**Step 3: 补齐必要的复用组件**

如空状态、面板、操作按钮。

### Task 7: 迁移老师端页面

**Files:**
- Create: `wechat-native/pages/teacher/dashboard/*`
- Create: `wechat-native/pages/teacher/competition-editor/*`

**Step 1: 迁移老师看板**

支持数据概览和导出入口。

**Step 2: 迁移比赛发布/管理页**

支持草稿、编辑、推荐、置顶、下架。

### Task 8: 验证与交付

**Files:**
- Modify: `docs/runbooks/local-setup.md`
- Modify: `task_plan.md`
- Modify: `findings.md`
- Modify: `progress.md`

**Step 1: 运行原生端最小自动化测试**

Run: `node --test wechat-native/tests/session.test.js`

**Step 2: 验证原生端目录结构**

检查 `wechat-native/app.json`、页面注册和公共服务模块。

**Step 3: 更新运行说明**

补充微信开发者工具导入方式与后端联调说明。
