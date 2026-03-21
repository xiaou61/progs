# 比赛置顶行为修复 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 让比赛的推荐/置顶状态在主保存按钮下即可生效，并让主要比赛列表真正按置顶优先展示。

**Architecture:** 后端把 `recommended/pinned` 并入比赛创建与更新命令，同时保留独立的 `/feature` 接口；比赛管理列表、老师自管列表和公开比赛列表统一复用同一套排序规则；前端和小程序主保存请求补上展示状态字段，避免用户额外再点一次“保存推荐/置顶”。

**Tech Stack:** Spring Boot + MyBatis-Plus, Vue 3 + Vitest, 微信小程序原生 JS + Node test runner

---

### Task 1: 锁住后端行为

**Files:**
- Modify: `server/src/test/java/com/campus/competition/modules/competition/CompetitionManageApiTest.java`
- Modify: `server/src/test/java/com/campus/competition/modules/dashboard/DashboardOverviewApiTest.java`
- Modify: `server/src/main/java/com/campus/competition/modules/competition/model/PublishCompetitionCommand.java`
- Modify: `server/src/main/java/com/campus/competition/modules/competition/model/SaveCompetitionDraftCommand.java`
- Modify: `server/src/main/java/com/campus/competition/modules/competition/model/UpdateCompetitionCommand.java`
- Modify: `server/src/main/java/com/campus/competition/modules/competition/service/CompetitionService.java`
- Modify: `server/src/main/java/com/campus/competition/modules/dashboard/service/DashboardService.java`

**Step 1: 写失败测试**

- 在 `CompetitionManageApiTest` 里补一个场景：
  - 发布或更新比赛时直接传 `recommended/pinned: true`
  - 断言响应体和后续列表查询都返回 `true`
  - 再创建一个未置顶比赛，断言管理列表与公开列表里置顶比赛排在前面
- 在 `DashboardOverviewApiTest` 里补一个同指标场景：
  - 两个比赛报名量等指标相同
  - 置顶比赛应排在 `topCompetitions[0]`

**Step 2: 运行测试确认失败**

Run:
`mvn -f server/pom.xml "-Dtest=CompetitionManageApiTest,DashboardOverviewApiTest" test`

Expected:
测试因为命令对象缺少字段或排序逻辑不满足而失败。

**Step 3: 写最小实现**

- 给比赛创建/更新命令增加 `recommended/pinned`
- `publish`、`saveDraft`、`update` 在落库和内存模式下都写入这两个字段
- 抽一个统一排序比较器/查询顺序给管理列表、老师自管列表和公开列表复用
- 调整 `DashboardService`，让置顶/推荐在指标相同时仍然优先

**Step 4: 运行测试确认通过**

Run:
`mvn -f server/pom.xml "-Dtest=CompetitionManageApiTest,DashboardOverviewApiTest" test`

Expected:
相关后端测试全部通过。

### Task 2: 锁住后台主保存链路

**Files:**
- Modify: `admin-web/src/api/competition-manage.ts`
- Modify: `admin-web/src/views/competition/CompetitionEditorPage.vue`
- Create or Modify: `admin-web/src/__tests__/competition-editor-feature.spec.ts`

**Step 1: 写失败测试**

- 新增一个源代码/接口层测试，断言：
  - `CompetitionUpdatePayload` 与草稿/发布链路包含 `recommended/pinned`
  - `CompetitionEditorPage.vue` 的 `submitCompetition`/`submitDraft` 会把 `featureForm` 一起提交

**Step 2: 运行测试确认失败**

Run:
`pnpm --dir admin-web test -- --run competition-editor-feature.spec.ts`

Expected:
测试因为当前主保存没有带展示状态字段而失败。

**Step 3: 写最小实现**

- 更新前端类型定义
- 在主保存逻辑里把 `featureForm` 并入请求体
- 保留独立“保存推荐/置顶”按钮，不改交互入口

**Step 4: 运行测试确认通过**

Run:
`pnpm --dir admin-web test -- --run competition-editor-feature.spec.ts competition-manage.spec.ts`

Expected:
新增测试和既有比赛管理 API 测试通过。

### Task 3: 锁住老师端主保存链路

**Files:**
- Modify: `wechat-native/pages/teacher/competition-editor/index.js`
- Modify: `wechat-native/tests/teacher-competition-source.test.js`

**Step 1: 写失败测试**

- 补一个源码测试，断言老师端 `submitPublish` 和 `submitDraft` 也会带上 `featureForm.recommended/pinned`

**Step 2: 运行测试确认失败**

Run:
`node --test wechat-native/tests/teacher-competition-source.test.js`

Expected:
测试因为主保存没有提交展示状态而失败。

**Step 3: 写最小实现**

- 老师端 `buildPayload` 或提交逻辑并入 `featureForm`
- 不删除已有单独保存展示状态入口

**Step 4: 运行测试确认通过**

Run:
`node --test wechat-native/tests/teacher-competition-source.test.js`

Expected:
老师端相关测试通过。

### Task 4: 做回归验证

**Files:**
- No code changes expected

**Step 1: 运行后端相关测试**

Run:
`mvn -f server/pom.xml "-Dtest=CompetitionManageApiTest,DashboardOverviewApiTest" test`

**Step 2: 运行后台前端测试**

Run:
`pnpm --dir admin-web test`

**Step 3: 运行小程序测试**

Run:
`node --test wechat-native/tests/*.test.js`

**Step 4: 整理结果**

- 记录根因：
  - 主保存链路未保存推荐/置顶
  - 多个比赛列表未真正按置顶优先排序
- 汇总验证命令和结果
