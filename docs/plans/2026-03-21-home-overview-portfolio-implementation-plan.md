# 首页进度卡片与我的成果页 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 让首页中间四张进度卡片都可点击，并新增“我的成果”页承接作品与获奖信息。

**Architecture:** 后端新增一个最小只读接口暴露当前用户全部作品记录；小程序新增 `portfolio` 页面，聚合作品列表、获奖列表和对应比赛标题；首页进度卡片通过统一路由映射跳转到比赛列表、成果页和积分页。

**Tech Stack:** Spring Boot + Java, 微信小程序原生 JS/WXML/WXSS, Node test runner

---

### Task 1: 锁住首页卡片路由与成果页数据整理

**Files:**
- Modify: `wechat-native/tests/helpers.test.js`
- Create: `wechat-native/tests/portfolio-source.test.js`
- Create: `wechat-native/utils/portfolio.js`

**Step 1: 写失败测试**

- 在 `helpers.test.js` 中补充断言：
  - 首页进度卡片包含稳定 key
  - `我的比赛 / 已交作品 / 获奖次数 / 总积分` 能解析到正确跳转路由
- 新增 `portfolio-source.test.js`：
  - 锁定新页面存在
  - 使用作品服务、结果服务和比赛服务
  - 支持 `tab=works` 与 `tab=awards`
- 为新工具函数 `buildPortfolioState(...)` 写最小行为测试：
  - 可生成作品卡
  - 可生成获奖卡
  - 可根据 `tab` 返回高亮分区

**Step 2: 运行测试确认失败**

Run:
`node --test wechat-native/tests/helpers.test.js wechat-native/tests/portfolio-source.test.js`

Expected:
由于新页面和新路由工具尚未实现而失败。

**Step 3: 写最小实现**

- 新增 `wechat-native/utils/portfolio.js`
- 调整 `wechat-native/utils/task.js` 让首页卡片带上 `key`
- 调整 `wechat-native/utils/home.js` 增加进度卡片跳转解析函数

**Step 4: 运行测试确认通过**

Run:
`node --test wechat-native/tests/helpers.test.js wechat-native/tests/portfolio-source.test.js`

Expected:
测试通过。

### Task 2: 补后端按用户查询作品接口

**Files:**
- Modify: `server/src/main/java/com/campus/competition/modules/submission/controller/AppSubmissionController.java`
- Modify: `server/src/test/java/com/campus/competition/modules/submission/SubmissionServiceTest.java`

**Step 1: 写失败测试**

- 在 `SubmissionServiceTest` 或相邻服务层测试里补一个用户作品列表场景：
  - 同一用户提交两个比赛作品
  - 另一用户也提交作品
  - 断言 `listByUser(userId)` 仅返回当前用户记录，且按提交时间倒序

**Step 2: 运行测试确认失败**

Run:
`mvn -f server/pom.xml "-Dtest=SubmissionServiceTest" test`

Expected:
如果当前行为不满足排序或过滤规则则失败；若已满足，则继续把控制器最小实现落地并保留测试作为回归锁。

**Step 3: 写最小实现**

- 在 `AppSubmissionController` 新增 `GET /api/app/submissions/user/{userId}`
- 调用 `AuthContext.requireUser(userId)` 和 `submissionService.listByUser(userId)`

**Step 4: 运行测试确认通过**

Run:
`mvn -f server/pom.xml "-Dtest=SubmissionServiceTest" test`

Expected:
服务层测试通过。

### Task 3: 实现我的成果页

**Files:**
- Create: `wechat-native/pages/competition/portfolio/index.js`
- Create: `wechat-native/pages/competition/portfolio/index.wxml`
- Create: `wechat-native/pages/competition/portfolio/index.wxss`
- Modify: `wechat-native/app.json`
- Modify: `wechat-native/services/submission.js`

**Step 1: 写失败测试**

- 在 `portfolio-source.test.js` 中补断言：
  - 新页面已注册到 `app.json`
  - 页面调用 `fetchUserSubmissions(userId)`、`fetchStudentOverview(userId)`、`fetchCompetitions()`
  - 页面支持跳到比赛作品页和结果页

**Step 2: 运行测试确认失败**

Run:
`node --test wechat-native/tests/portfolio-source.test.js`

Expected:
页面或服务尚未实现而失败。

**Step 3: 写最小实现**

- 在 `submission.js` 新增 `fetchUserSubmissions(userId)`
- 新建成果页，加载作品、获奖和比赛标题映射
- 作品卡支持跳到 `/pages/competition/submission/index?competitionId=...`
- 获奖卡支持跳到 `/pages/competition/result/index?competitionId=...`

**Step 4: 运行测试确认通过**

Run:
`node --test wechat-native/tests/portfolio-source.test.js`

Expected:
成果页相关测试通过。

### Task 4: 接通首页卡片点击

**Files:**
- Modify: `wechat-native/pages/home/index.js`
- Modify: `wechat-native/pages/home/index.wxml`

**Step 1: 写失败测试**

- 在 `portfolio-source.test.js` 或独立源码测试中断言：
  - 首页“我的进度”卡片绑定点击事件
  - 点击使用新的进度卡片路由解析函数

**Step 2: 运行测试确认失败**

Run:
`node --test wechat-native/tests/portfolio-source.test.js`

Expected:
首页尚未接上点击事件而失败。

**Step 3: 写最小实现**

- 首页 overview 卡片增加 `data-key`
- 新增 `openOverviewCard`，按 key 跳转
- 保持 points/profile 等其它页面的 overview 卡片只做展示，不引入副作用

**Step 4: 运行测试确认通过**

Run:
`node --test wechat-native/tests/portfolio-source.test.js`

Expected:
首页卡片跳转测试通过。

### Task 5: 做回归验证

**Files:**
- No code changes expected

**Step 1: 跑小程序全量测试**

Run:
`node --test wechat-native/tests/*.test.js`

**Step 2: 跑后端相关测试**

Run:
`mvn -f server/pom.xml "-Dtest=SubmissionServiceTest" test`

**Step 3: 记录已知限制**

- 这版成果页基于现有数据能力展示“我的作品记录 + 我的获奖结果”
- 若后续需要作品预览、筛选、分页，再单独扩展
