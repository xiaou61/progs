# Full Mock Removal Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 清理仓库中显式 mock / demo 入口，并补齐后台关键页面的真实选择链路与后端空值返回逻辑。

**Architecture:** 以现有真实接口为核心，优先通过前端消费层和最小后端字段扩展完成改造。测试先行，分批清理小程序登录预设、后台评审默认值、后台手输内部 ID 交互和登录展示占位问题。

**Tech Stack:** Vue 3、Pinia、Vitest、微信小程序原生 JS、Spring Boot 3、MyBatis-Plus、MockMvc

---

### Task 1: 落盘设计与计划文档

**Files:**
- Create: `docs/plans/2026-03-18-full-mock-removal-design.md`
- Create: `docs/plans/2026-03-18-full-mock-removal-implementation-plan.md`

**Step 1: 写入设计文档**

记录范围、数据流、风险与测试策略。

**Step 2: 写入实现计划**

拆分为小步 TDD 任务。

**Step 3: 验证文档存在**

Run: `Get-ChildItem docs/plans`
Expected: 可见本次设计文档与实现计划。

### Task 2: 为演示入口清理写失败测试

**Files:**
- Modify: `admin-web/src/__tests__/admin-login-copy.spec.ts`
- Create: `admin-web/src/__tests__/review-workbench-copy.spec.ts`

**Step 1: 写失败测试**

- 断言小程序登录页源码中不应再出现“学生演示账号”“老师演示账号”。
- 断言后台评审页源码中不应再出现 `competitionId = ref(1)`、`reviewerName: '王老师'`。

**Step 2: 运行测试确认失败**

Run: `pnpm --dir admin-web test -- --run admin-login-copy review-workbench-copy`
Expected: 因源码中仍有演示入口与默认值而失败。

**Step 3: 最小实现**

- 清理小程序登录预设。
- 清理后台评审默认值。

**Step 4: 重新运行测试确认通过**

Run: `pnpm --dir admin-web test -- --run admin-login-copy review-workbench-copy`
Expected: PASS

### Task 3: 为后端评审占位值清理写失败测试

**Files:**
- Create: `server/src/test/java/com/campus/competition/modules/review/ReviewTaskNullReviewerApiTest.java`
- Modify: `server/src/main/java/com/campus/competition/modules/review/service/ReviewService.java`

**Step 1: 写失败测试**

构造未评审提交记录，断言 `/api/admin/reviews/tasks` 返回的 `reviewerName` 为空或缺失，不允许返回 `"默认评委组"`。

**Step 2: 运行测试确认失败**

Run: `mvn -f server/pom.xml -Dtest=ReviewTaskNullReviewerApiTest test`
Expected: FAIL，当前实现仍返回 `"默认评委组"`。

**Step 3: 最小实现**

将 `ReviewService` 的未评审任务 `reviewerName` 改为 `null`。

**Step 4: 重新运行测试确认通过**

Run: `mvn -f server/pom.xml -Dtest=ReviewTaskNullReviewerApiTest test`
Expected: PASS

### Task 4: 为登录真实姓名返回写失败测试

**Files:**
- Modify: `server/src/test/java/com/campus/competition/modules/auth/AuthGuardApiTest.java`
- Modify: `server/src/main/java/com/campus/competition/modules/auth/model/LoginResult.java`
- Modify: `server/src/main/java/com/campus/competition/modules/auth/service/AuthService.java`
- Modify: `admin-web/src/api/auth.ts`
- Modify: `admin-web/src/views/login/LoginPage.vue`

**Step 1: 写失败测试**

在登录接口测试中断言返回包含真实姓名与学号字段。

**Step 2: 运行测试确认失败**

Run: `mvn -f server/pom.xml -Dtest=AuthGuardApiTest test`
Expected: FAIL，当前 `LoginResult` 不包含展示字段。

**Step 3: 最小实现**

- 扩展后端登录返回。
- 管理端登录消费真实姓名，更新会话显示。

**Step 4: 重新运行测试确认通过**

Run: `mvn -f server/pom.xml -Dtest=AuthGuardApiTest test`
Expected: PASS

### Task 5: 为后台比赛发布真实发起人选择写失败测试

**Files:**
- Create: `admin-web/src/__tests__/competition-editor-copy.spec.ts`
- Modify: `admin-web/src/views/competition/CompetitionEditorPage.vue`

**Step 1: 写失败测试**

断言页面源码不再包含默认 `organizerId: 1001`，且不再使用“请输入发起人编号”文案。

**Step 2: 运行测试确认失败**

Run: `pnpm --dir admin-web test -- --run competition-editor-copy`
Expected: FAIL

**Step 3: 最小实现**

- 载入用户列表。
- 筛选老师候选。
- 将发起人输入框改为选择器。

**Step 4: 重新运行测试确认通过**

Run: `pnpm --dir admin-web test -- --run competition-editor-copy`
Expected: PASS

### Task 6: 为后台报名补录真实学生选择写失败测试

**Files:**
- Create: `admin-web/src/__tests__/registration-manage-copy.spec.ts`
- Modify: `admin-web/src/views/competition/RegistrationManagePage.vue`

**Step 1: 写失败测试**

断言页面源码不再使用“请输入学生用户编号”文案。

**Step 2: 运行测试确认失败**

Run: `pnpm --dir admin-web test -- --run registration-manage-copy`
Expected: FAIL

**Step 3: 最小实现**

- 载入用户列表。
- 筛选学生候选。
- 将补录表单改为选择器。

**Step 4: 重新运行测试确认通过**

Run: `pnpm --dir admin-web test -- --run registration-manage-copy`
Expected: PASS

### Task 7: 补齐后台评审工作台真实选择交互

**Files:**
- Modify: `admin-web/src/views/review/ReviewWorkbenchPage.vue`
- Modify: `admin-web/src/__tests__/review-workbench-copy.spec.ts`

**Step 1: 写失败测试**

断言页面源码包含比赛选择逻辑，不再依赖默认比赛 ID。

**Step 2: 运行测试确认失败**

Run: `pnpm --dir admin-web test -- --run review-workbench-copy`
Expected: FAIL

**Step 3: 最小实现**

- 加载比赛列表。
- 选择比赛后刷新任务和成绩。
- 对空评委名展示业务空态而非占位姓名。

**Step 4: 重新运行测试确认通过**

Run: `pnpm --dir admin-web test -- --run review-workbench-copy`
Expected: PASS

### Task 8: 小程序登录页清理与静态验证

**Files:**
- Modify: `wechat-native/pages/login/index.js`
- Modify: `wechat-native/pages/login/index.wxml`

**Step 1: 写失败测试或静态断言**

复用源码级断言，确保登录页不再有演示入口和默认密码。

**Step 2: 运行验证确认失败**

Run: `pnpm --dir admin-web test -- --run admin-login-copy`
Expected: FAIL

**Step 3: 最小实现**

- 删除 `applyPreset`
- 删除演示按钮
- 清空默认学号和密码

**Step 4: 重新运行验证确认通过**

Run: `pnpm --dir admin-web test -- --run admin-login-copy`
Expected: PASS

### Task 9: 全量验证

**Files:**
- Modify: `admin-web/src/views/login/LoginPage.vue`
- Modify: `admin-web/src/views/review/ReviewWorkbenchPage.vue`
- Modify: `admin-web/src/views/competition/CompetitionEditorPage.vue`
- Modify: `admin-web/src/views/competition/RegistrationManagePage.vue`
- Modify: `wechat-native/pages/login/index.js`
- Modify: `wechat-native/pages/login/index.wxml`
- Modify: `server/src/main/java/com/campus/competition/modules/review/service/ReviewService.java`
- Modify: `server/src/main/java/com/campus/competition/modules/auth/model/LoginResult.java`
- Modify: `server/src/main/java/com/campus/competition/modules/auth/service/AuthService.java`

**Step 1: 运行前端相关测试**

Run: `pnpm --dir admin-web test -- --run admin-login-copy competition-editor-copy registration-manage-copy review-workbench-copy admin-auth`
Expected: 全部 PASS

**Step 2: 运行前端构建**

Run: `pnpm --dir admin-web build`
Expected: 构建成功

**Step 3: 运行后端相关测试**

Run: `mvn -f server/pom.xml -Dtest=AuthGuardApiTest,ReviewTaskNullReviewerApiTest test`
Expected: PASS

**Step 4: grep 回归检查**

Run: `rg -n -S "学生演示账号|老师演示账号|默认评委组|competitionId = ref\\(1\\)|reviewerName: '王老师'|password: 'Abcd1234'|studentNo: 'S20260001'" admin-web/src wechat-native server/src/main/java`
Expected: 不再命中生产代码。
