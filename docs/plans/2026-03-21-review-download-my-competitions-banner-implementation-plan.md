# 评审下载、我的比赛筛选与首页轮播图 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 让评审老师能直接下载作品文件，让小程序“我的比赛”只显示已报名比赛，并让首页真正展示后台配置的轮播图。

**Architecture:** 后端补最小只读字段和接口，前端分别在管理端评审工作台与小程序首页/比赛列表页消费这些数据。优先复用现有比赛列表、报名、轮播图管理能力，不新建重型页面。

**Tech Stack:** Spring Boot + MyBatis Plus、Vue 3 + Vitest、微信小程序原生页面 + Node test

---

### Task 1: 评审任务返回作品下载信息

**Files:**
- Modify: `server/src/main/java/com/campus/competition/modules/review/model/ReviewTaskSummary.java`
- Modify: `server/src/main/java/com/campus/competition/modules/review/service/ReviewService.java`
- Modify: `admin-web/src/api/review.ts`
- Test: `admin-web/src/__tests__/api-clients.spec.ts`

**Step 1: Write the failing test**

- 在 `admin-web/src/__tests__/api-clients.spec.ts` 中扩展 `fetchReviewTasks` 断言，要求返回 `fileUrl`、`fileName`、`versionNo`。

**Step 2: Run test to verify it fails**

Run: `pnpm --dir admin-web test src/__tests__/api-clients.spec.ts`

**Step 3: Write minimal implementation**

- 扩展 `ReviewTaskSummary` 字段。
- `ReviewService.toSummary(...)` 从 `SubmissionSummary` 补齐文件字段。
- 更新管理端 `ReviewTaskItem` 类型。

**Step 4: Run test to verify it passes**

Run: `pnpm --dir admin-web test src/__tests__/api-clients.spec.ts`

**Step 5: Commit**

```bash
git add server/src/main/java/com/campus/competition/modules/review/model/ReviewTaskSummary.java server/src/main/java/com/campus/competition/modules/review/service/ReviewService.java admin-web/src/api/review.ts admin-web/src/__tests__/api-clients.spec.ts
git commit -m "feat: expose review submission file metadata"
```

### Task 2: 管理端评审工作台点击作品直接下载

**Files:**
- Modify: `admin-web/src/views/review/ReviewWorkbenchPage.vue`
- Test: `admin-web/src/__tests__/review-workbench-download.spec.ts`

**Step 1: Write the failing test**

- 新增源码测试，要求：
  - 作品卡片显示文件名/版本。
  - 存在下载函数。
  - 点击作品卡片绑定下载行为。

**Step 2: Run test to verify it fails**

Run: `pnpm --dir admin-web test src/__tests__/review-workbench-download.spec.ts`

**Step 3: Write minimal implementation**

- 添加 `resolveDownloadUrl()` 和 `downloadSubmission()`。
- 在作品卡片和下拉选择附近展示文件信息。
- 作品卡片点击直接下载，保留“带入评审表单”按钮。

**Step 4: Run test to verify it passes**

Run: `pnpm --dir admin-web test src/__tests__/review-workbench-download.spec.ts`

**Step 5: Commit**

```bash
git add admin-web/src/views/review/ReviewWorkbenchPage.vue admin-web/src/__tests__/review-workbench-download.spec.ts
git commit -m "feat: download review submissions from workbench"
```

### Task 3: 小程序报名接口补用户报名列表

**Files:**
- Modify: `server/src/main/java/com/campus/competition/modules/registration/controller/AppRegistrationController.java`
- Modify: `wechat-native/services/registration.js`
- Test: `server/src/test/java/com/campus/competition/modules/points/DailyTaskOverviewApiTest.java`

**Step 1: Write the failing test**

- 补一个轻量接口测试或复用已有测试，要求能按用户读取报名列表。

**Step 2: Run test to verify it fails**

Run: `mvn -f server/pom.xml "-Dtest=DailyTaskOverviewApiTest" test`

**Step 3: Write minimal implementation**

- 后端新增 `GET /api/app/registrations/user/{userId}`。
- 小程序补 `fetchUserRegistrations(userId)`。

**Step 4: Run test to verify it passes**

Run: `mvn -f server/pom.xml "-Dtest=DailyTaskOverviewApiTest" test`

**Step 5: Commit**

```bash
git add server/src/main/java/com/campus/competition/modules/registration/controller/AppRegistrationController.java wechat-native/services/registration.js server/src/test/java/com/campus/competition/modules/points/DailyTaskOverviewApiTest.java
git commit -m "feat: expose user registration list to miniapp"
```

### Task 4: 小程序比赛列表支持“我的比赛”

**Files:**
- Modify: `wechat-native/utils/home.js`
- Modify: `wechat-native/pages/competition/list/index.js`
- Modify: `wechat-native/pages/competition/list/index.wxml`
- Test: `wechat-native/tests/helpers.test.js`
- Test: `wechat-native/tests/competition-source.test.js`

**Step 1: Write the failing test**

- 断言首页“我的比赛”跳转带 `scope=my`。
- 断言比赛列表页读取 `scope`，并包含用户报名过滤逻辑。

**Step 2: Run test to verify it fails**

Run: `node --test wechat-native/tests/helpers.test.js wechat-native/tests/competition-source.test.js`

**Step 3: Write minimal implementation**

- 更新 `resolveOverviewRoute('my-competitions')`。
- 比赛列表页按 `scope` 决定是否拉报名记录并过滤。
- 增加“我的比赛”空状态文案。

**Step 4: Run test to verify it passes**

Run: `node --test wechat-native/tests/helpers.test.js wechat-native/tests/competition-source.test.js`

**Step 5: Commit**

```bash
git add wechat-native/utils/home.js wechat-native/pages/competition/list/index.js wechat-native/pages/competition/list/index.wxml wechat-native/tests/helpers.test.js wechat-native/tests/competition-source.test.js
git commit -m "fix: filter miniapp competitions to registered items"
```

### Task 5: 小程序首页轮播图

**Files:**
- Modify: `server/src/main/java/com/campus/competition/modules/system/controller/AdminBannerController.java`
- Create: `server/src/main/java/com/campus/competition/modules/system/controller/AppBannerController.java`
- Modify: `wechat-native/services/competition.js`
- Modify: `wechat-native/pages/home/index.js`
- Modify: `wechat-native/pages/home/index.wxml`
- Modify: `wechat-native/pages/home/index.wxss`
- Test: `admin-web/src/__tests__/api-clients.spec.ts`
- Test: `wechat-native/tests/helpers.test.js`

**Step 1: Write the failing test**

- 小程序端断言存在 banner 拉取与展示源码。
- 后台 API 客户端测试补小程序 banner 读取行为，或补后端轻量接口测试。

**Step 2: Run test to verify it fails**

Run: `node --test wechat-native/tests/helpers.test.js`

**Step 3: Write minimal implementation**

- 后端新增 app banner 列表接口，只返回启用项。
- 小程序首页增加 banner 数据、加载与点击跳转。
- 轮播项复用后台 `jumpPath`。

**Step 4: Run test to verify it passes**

Run: `node --test wechat-native/tests/helpers.test.js`

**Step 5: Commit**

```bash
git add server/src/main/java/com/campus/competition/modules/system/controller/AppBannerController.java server/src/main/java/com/campus/competition/modules/system/controller/AdminBannerController.java wechat-native/services/competition.js wechat-native/pages/home/index.js wechat-native/pages/home/index.wxml wechat-native/pages/home/index.wxss wechat-native/tests/helpers.test.js admin-web/src/__tests__/api-clients.spec.ts
git commit -m "feat: show configured banners on miniapp home"
```

### Task 6: 最小回归验证

**Files:**
- No code changes expected

**Step 1: Run focused admin tests**

Run: `pnpm --dir admin-web test`

**Step 2: Run focused miniapp tests**

Run: `node --test wechat-native/tests/*.test.js`

**Step 3: Run focused backend tests**

Run: `mvn -f server/pom.xml "-Dtest=DailyTaskOverviewApiTest" test`

**Step 4: Summarize verification**

- 记录通过的命令与仍然存在的历史测试问题（如有）。

**Step 5: Commit**

```bash
git status --short
```
