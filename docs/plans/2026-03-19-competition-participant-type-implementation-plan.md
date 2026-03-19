# Competition Participant Type Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 为比赛增加“仅学生参加 / 仅老师参加”两种参赛类型，并在学生赛中强制绑定一名指导老师，同时让报名链路按比赛类型校验用户角色。

**Architecture:** 保持现有个人报名模型不变，只在比赛模型上新增 `participantType` 与 `advisorTeacherId`，通过比赛派生出指导老师归属信息。后端扩展比赛保存与报名校验，后台和小程序同步补上配置与展示入口，避免引入独立队伍或多老师关系模型。

**Tech Stack:** Spring Boot 3、Flyway、MyBatis-Plus、Vue 3、Vitest、微信小程序原生 JS、Node `node:test`

---

### Task 1: 为比赛参赛类型与指导老师规则写后端失败测试

**Files:**
- Create: `server/src/test/java/com/campus/competition/modules/competition/CompetitionParticipantTypeApiTest.java`

**Step 1: Write the failing test**

- 覆盖以下场景：
  - 学生赛未指定指导老师时保存失败
  - 学生赛指定非老师账号时保存失败
  - 老师赛携带指导老师时保存失败
  - 学生赛保存成功后返回 `participantType` 与 `advisorTeacherId`

**Step 2: Run test to verify it fails**

Run: `mvn -f server/pom.xml "-Dtest=CompetitionParticipantTypeApiTest" test`

Expected: FAIL，因为比赛模型和校验尚未支持新字段。

**Step 3: Write minimal implementation**

- 新增数据库迁移字段
- 扩展比赛实体、模型、服务与控制器
- 增加指导老师与参赛类型校验

**Step 4: Run test to verify it passes**

Run: `mvn -f server/pom.xml "-Dtest=CompetitionParticipantTypeApiTest" test`

Expected: PASS。

**Step 5: Commit**

```bash
git add server/src/main/resources/db/migration server/src/main/java/com/campus/competition/modules/competition server/src/test/java/com/campus/competition/modules/competition/CompetitionParticipantTypeApiTest.java
git commit -m "feat: add competition participant type rules"
```

### Task 2: 为报名角色限制写后端失败测试

**Files:**
- Create: `server/src/test/java/com/campus/competition/modules/registration/RegistrationParticipantTypeApiTest.java`

**Step 1: Write the failing test**

- 覆盖以下场景：
  - 学生不能报名 `TEACHER_ONLY` 比赛
  - 老师不能报名 `STUDENT_ONLY` 比赛
  - 匹配角色的用户可以正常报名

**Step 2: Run test to verify it fails**

Run: `mvn -f server/pom.xml "-Dtest=RegistrationParticipantTypeApiTest" test`

Expected: FAIL，因为报名服务尚未按比赛类型校验角色。

**Step 3: Write minimal implementation**

- 在报名服务中读取比赛 `participantType`
- 对当前用户角色做匹配校验

**Step 4: Run test to verify it passes**

Run: `mvn -f server/pom.xml "-Dtest=RegistrationParticipantTypeApiTest" test`

Expected: PASS。

**Step 5: Commit**

```bash
git add server/src/main/java/com/campus/competition/modules/registration server/src/test/java/com/campus/competition/modules/registration/RegistrationParticipantTypeApiTest.java
git commit -m "feat: enforce registration participant type"
```

### Task 3: 为后台比赛发布页写失败测试

**Files:**
- Create: `admin-web/src/__tests__/competition-participant-type.spec.ts`
- Modify: `admin-web/src/views/competition/CompetitionEditorPage.vue`
- Modify: `admin-web/src/api/competition.ts`
- Modify: `admin-web/src/api/competition-manage.ts`
- Modify: `admin-web/src/utils/competition-form.ts`

**Step 1: Write the failing test**

- 断言比赛发布页源码中存在：
  - `participantType`
  - `advisorTeacherId`
  - 学生赛时显示指导老师选择逻辑

**Step 2: Run test to verify it fails**

Run: `pnpm --dir admin-web test -- --run competition-participant-type`

Expected: FAIL，因为页面与表单工具尚未支持新字段。

**Step 3: Write minimal implementation**

- 扩展后台比赛表单模型
- 增加参赛类型选择
- 增加指导老师下拉与联动校验
- 接口 payload 补充新字段

**Step 4: Run test to verify it passes**

Run: `pnpm --dir admin-web test -- --run competition-participant-type`

Expected: PASS。

**Step 5: Commit**

```bash
git add admin-web/src/views/competition/CompetitionEditorPage.vue admin-web/src/api/competition.ts admin-web/src/api/competition-manage.ts admin-web/src/utils/competition-form.ts admin-web/src/__tests__/competition-participant-type.spec.ts
git commit -m "feat: support participant type in admin competition editor"
```

### Task 4: 为小程序报名页与比赛展示写失败测试

**Files:**
- Create: `wechat-native/tests/competition-participant-type.test.js`
- Modify: `wechat-native/pages/competition/register/index.js`
- Modify: `wechat-native/pages/competition/register/index.wxml`
- Modify: `wechat-native/pages/competition/detail/index.js`
- Modify: `wechat-native/pages/competition/detail/index.wxml`
- Modify: `wechat-native/services/competition.js`

**Step 1: Write the failing test**

- 断言小程序源码或工具逻辑中存在：
  - `participantType` 展示
  - `advisorTeacherName` 展示
  - 角色不匹配时阻止报名

**Step 2: Run test to verify it fails**

Run: `node --test wechat-native/tests/competition-participant-type.test.js`

Expected: FAIL，因为小程序页面尚未识别新字段和新校验。

**Step 3: Write minimal implementation**

- 比赛详情页展示参赛类型和指导老师
- 报名页展示参赛限制
- 角色不匹配时禁用或阻止报名提交

**Step 4: Run test to verify it passes**

Run: `node --test wechat-native/tests/competition-participant-type.test.js`

Expected: PASS。

**Step 5: Commit**

```bash
git add wechat-native/pages/competition/register wechat-native/pages/competition/detail wechat-native/services/competition.js wechat-native/tests/competition-participant-type.test.js
git commit -m "feat: show participant type in miniapp competition pages"
```

### Task 5: 为后台报名管理和关联展示补充实现与回归

**Files:**
- Modify: `admin-web/src/views/competition/RegistrationManagePage.vue`
- Modify: `admin-web/src/views/review/ReviewWorkbenchPage.vue`

**Step 1: Write the failing test**

- 若需要，新增源码断言测试，要求页面展示比赛参赛类型和指导老师信息。

**Step 2: Run test to verify it fails**

Run: `pnpm --dir admin-web test -- --run registration-manage`

Expected: 若增加了对应断言，应先 FAIL。

**Step 3: Write minimal implementation**

- 在报名管理页展示参赛类型和指导老师
- 在评审工作台比赛摘要区补充该信息

**Step 4: Run test to verify it passes**

Run: `pnpm --dir admin-web test -- --run registration-manage`

Expected: PASS。

**Step 5: Commit**

```bash
git add admin-web/src/views/competition/RegistrationManagePage.vue admin-web/src/views/review/ReviewWorkbenchPage.vue
git commit -m "feat: surface advisor teacher in admin competition views"
```

### Task 6: 全量验证与整理

**Files:**
- Modify: `docs/plans/2026-03-19-competition-participant-type-design.md`
- Modify: `docs/plans/2026-03-19-competition-participant-type-implementation-plan.md`

**Step 1: Run focused backend tests**

Run: `mvn -f server/pom.xml "-Dtest=CompetitionParticipantTypeApiTest,RegistrationParticipantTypeApiTest,CompetitionManageApiTest" test`

Expected: PASS。

**Step 2: Run miniapp tests**

Run: `node --test wechat-native/tests/*.test.js`

Expected: PASS。

**Step 3: Run admin-web tests**

Run: `pnpm --dir admin-web test`

Expected: PASS。

**Step 4: Run admin-web build**

Run: `pnpm --dir admin-web build`

Expected: PASS。

**Step 5: Commit**

```bash
git add docs/plans/2026-03-19-competition-participant-type-design.md docs/plans/2026-03-19-competition-participant-type-implementation-plan.md
git commit -m "docs: record competition participant type plan"
```
