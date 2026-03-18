# 原生微信小程序迁移设计

## 背景

当前用户端位于 `miniapp/`，基于 `uni-app + Vue 3 + Pinia + Vite` 实现，已经完成登录、比赛主链路、消息中心、积分页、个人中心和老师端管理能力。

本次目标不是继续维护 `uni-app` 版本，而是新增一个可被微信开发者工具直接打开运行的纯原生微信小程序端，采用标准的 `app.json / app.js / app.wxss / pages/**` 结构，不依赖 `uni-app`、Vue、Pinia 等运行时。

## 目标

- 新建原生微信小程序目录，保留 `miniapp/` 作为参考实现。
- 在不改变后端接口与业务流程的前提下，复刻当前用户端主要能力。
- 优先保证登录与比赛主链路完整可用。
- 最终产物可由微信开发者工具直接导入运行。

## 非目标

- 不在本轮重写后台 `admin-web/`。
- 不对后端接口进行协议层重构。
- 不引入第三方跨端框架，不做“原生外壳 + WebView”折中方案。
- 不追求页面视觉再设计，优先保持与现有 `miniapp/` 业务一致。

## 目录设计

建议新增目录 `wechat-native/`，采用标准原生小程序结构：

```text
wechat-native/
├─ app.js
├─ app.json
├─ app.wxss
├─ project.config.json
├─ sitemap.json
├─ pages/
│  ├─ login/
│  ├─ home/
│  ├─ teacher/dashboard/
│  ├─ teacher/competition-editor/
│  ├─ competition/list/
│  ├─ competition/detail/
│  ├─ competition/register/
│  ├─ competition/checkin/
│  ├─ competition/submission/
│  ├─ competition/result/
│  ├─ message/
│  ├─ message/chat/
│  ├─ points/
│  └─ profile/
├─ services/
├─ utils/
├─ components/
└─ tests/
```

## 迁移策略

### 1. 保留旧端，新增原生端

`miniapp/` 继续保留，作为页面文案、接口调用和业务行为参考源。原生端单独建设，避免原地重构导致现有可运行版本被破坏。

### 2. 公共逻辑优先平移

以下内容可以优先迁移为原生 JS 模块：

- `src/api/*` -> `wechat-native/services/*`
- `src/utils/auth-session.ts` -> `wechat-native/utils/session.js`
- `src/utils/auth-navigation.ts` -> `wechat-native/utils/routes.js`
- `src/utils/competition-navigation.ts` -> `wechat-native/utils/competition.js`
- `src/utils/home-navigation.ts` -> `wechat-native/utils/home.js`
- `src/utils/message-navigation.ts` -> `wechat-native/utils/message.js`

### 3. 状态管理替换策略

原 `Pinia` 登录态改为：

- `App.globalData.session`
- `wx.getStorageSync` / `wx.setStorageSync`
- 页面进入时通过公共 `ensureLogin`、`getSession` 进行校验

不再模拟 `Pinia` 或引入新的状态管理运行时。

### 4. 页面实现策略

每个页面拆成：

- `index.json`
- `index.wxml`
- `index.wxss`
- `index.js`

页面逻辑使用 `Page({ data, onLoad, onShow, methods })` 组织。
原 `ref / reactive / computed` 状态转换为：

- 可直接存入 `data` 的基础字段
- 需要重新计算的派生数据通过公共格式化函数 + `setData` 更新
- 原 Vue `computed` 不做运行时映射，统一改为“加载后计算并写入 data”

## 数据流设计

### 请求层

统一使用 `wx.request` 封装在 `services/http.js` 中：

- 自动拼接基础地址
- 自动带 `Authorization`
- 统一解包 `{ code, message, data }`
- 统一抛出错误文案

### 登录态

在 `utils/session.js` 中维护：

- `loadSession`
- `saveSession`
- `clearSession`
- `getAccessToken`
- `isLoggedIn`

### 页面守卫

在 `utils/routes.js` 中维护：

- 登录页路由常量
- 首页路由常量
- 构造登录回跳地址
- 未登录自动跳转登录页
- 登录后根据回跳地址或默认首页重定向

## 页面迁移优先级

### 第一优先级：比赛主链路

- 登录页
- 首页
- 比赛列表
- 比赛详情
- 比赛报名
- 现场签到
- 作品上传
- 比赛结果

### 第二优先级：用户高频能力

- 消息中心
- 会话详情
- 我的积分
- 个人中心

### 第三优先级：老师端能力

- 老师看板
- 比赛发布/管理

## 风险与处理

### 风险 1：页面迁移量大

当前用户端页面较多，且个人中心、老师发布页、消息中心逻辑较重。

处理：

- 先完成主链路闭环
- 重页面采用“先数据、后样式、再交互细节”的三段式迁移

### 风险 2：原 Vue 模板与原生 WXML 差异较大

处理：

- 不做机械式逐行转换
- 优先提取“页面状态 + 用户动作 + 接口调用”三部分
- 重写模板结构，保留原业务语义

### 风险 3：回跳和登录态行为容易回归

处理：

- 先为 `session` 与 `routes` 写最小测试
- 页面迁移时统一调用公共守卫工具

## 验收标准

- 微信开发者工具可直接导入 `wechat-native/`
- 存在标准原生产物：`app.json`、`pages/**`
- 登录成功后可进入首页
- 可完成比赛列表 -> 详情 -> 报名/签到/上传/结果查看主链路
- 消息、积分、个人中心、老师端页面可打开并调用真实接口
- 公共请求层和登录态工具具备最小自动化验证
