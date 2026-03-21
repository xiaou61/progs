# 签到申请制与老师确认流 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 把比赛签到改成“提交申请后等待老师确认”，并打通小程序、后台和后端三端状态。

**Architecture:** 后端为签到记录增加审核状态，并提供按报名记录审核签到申请的接口。后台报名管理页改成展示报名状态和签到申请状态两层信息，小程序签到页则按申请状态显示“待确认/已驳回/已到场”。

**Tech Stack:** Spring Boot、MyBatis-Plus、Vue 3、Vitest、微信小程序原生页面、Node `node:test`

---

### Task 1: 先为签到申请制写失败测试

**Files:**
- Create: `server/src/test/java/com/campus/competition/modules/checkin/CheckinApprovalApiTest.java`
- Modify: `admin-web/src/__tests__/registration-manage.spec.ts`
- Create: `wechat-native/tests/checkin-source.test.js`

**Step 1: Write the failing test**

- 后端测试覆盖：
  - 小程序签到后返回 `PENDING`
  - 后台报名列表能看到签到申请状态
  - 老师确认后报名记录变为 `PRESENT`
- 后台测试覆盖新的签到审核接口调用
- 小程序测试覆盖“提交签到申请”“待老师确认”“申请已驳回，可重新提交”等文案

**Step 2: Run test to verify it fails**

Run: `mvn -f server/pom.xml -Dtest=CheckinApprovalApiTest test`
Expected: FAIL，因为签到审核状态和审核接口尚不存在。

Run: `pnpm --dir admin-web test -- registration-manage.spec.ts`
Expected: FAIL，因为后台 API 客户端还没有签到审核方法。

Run: `node --test wechat-native/tests/checkin-source.test.js`
Expected: FAIL，因为小程序签到页还是“立即签到”直达成功。

**Step 3: Write minimal implementation**

- 新增后端签到审核模型与接口
- 更新后台 API 客户端
- 更新小程序签到页文案和状态展示

**Step 4: Run test to verify it passes**

Run: `mvn -f server/pom.xml -Dtest=CheckinApprovalApiTest test`
Expected: PASS

Run: `pnpm --dir admin-web test -- registration-manage.spec.ts`
Expected: PASS

Run: `node --test wechat-native/tests/checkin-source.test.js`
Expected: PASS

### Task 2: 实现后端签到申请状态流

**Files:**
- Create: `server/src/main/resources/db/migration/V16__checkin_approval_schema.sql`
- Create: `server/src/main/java/com/campus/competition/modules/checkin/model/CheckinReviewCommand.java`
- Create: `server/src/main/java/com/campus/competition/modules/registration/model/RegistrationManageSummary.java`
- Modify: `server/src/main/java/com/campus/competition/modules/checkin/model/CheckinSummary.java`
- Modify: `server/src/main/java/com/campus/competition/modules/checkin/persistence/CheckinEntity.java`
- Modify: `server/src/main/java/com/campus/competition/modules/checkin/service/CheckinService.java`
- Modify: `server/src/main/java/com/campus/competition/modules/registration/controller/AdminRegistrationController.java`
- Modify: `server/src/main/java/com/campus/competition/modules/registration/service/RegistrationService.java`

**Step 1: Write the failing test**

- 复用 `CheckinApprovalApiTest` 红灯测试。

**Step 2: Run test to verify it fails**

Run: `mvn -f server/pom.xml -Dtest=CheckinApprovalApiTest test`
Expected: FAIL

**Step 3: Write minimal implementation**

- 扩展签到表结构和实体
- 签到提交时写入 `PENDING`
- 后台确认/驳回时更新签到状态
- 后台报名管理列表合并签到申请信息
- 后台手动标记到场/缺席后同步签到申请状态

**Step 4: Run test to verify it passes**

Run: `mvn -f server/pom.xml -Dtest=CheckinApprovalApiTest test`
Expected: PASS

### Task 3: 实现后台报名页签到审核交互

**Files:**
- Modify: `admin-web/src/api/registration-manage.ts`
- Modify: `admin-web/src/views/competition/RegistrationManagePage.vue`
- Modify: `admin-web/src/__tests__/registration-manage-copy.spec.ts`

**Step 1: Write the failing test**

- 增加后台 API 测试和页面文案测试，要求出现“确认签到”“驳回签到申请”“待老师确认”等文案。

**Step 2: Run test to verify it fails**

Run: `pnpm --dir admin-web test -- registration-manage.spec.ts registration-manage-copy.spec.ts`
Expected: FAIL

**Step 3: Write minimal implementation**

- 后台页展示签到申请状态、申请时间、驳回原因
- 新增审核按钮和驳回原因编辑

**Step 4: Run test to verify it passes**

Run: `pnpm --dir admin-web test -- registration-manage.spec.ts registration-manage-copy.spec.ts`
Expected: PASS

### Task 4: 实现小程序签到申请页

**Files:**
- Modify: `wechat-native/services/checkin.js`
- Modify: `wechat-native/pages/competition/checkin/index.js`
- Modify: `wechat-native/pages/competition/checkin/index.wxml`
- Modify: `wechat-native/pages/competition/checkin/index.wxss`
- Modify: `wechat-native/tests/competition-source.test.js`

**Step 1: Write the failing test**

- 复用 `checkin-source.test.js`，要求页面不再把提交动作描述成“立即签到”直接完成。

**Step 2: Run test to verify it fails**

Run: `node --test wechat-native/tests/checkin-source.test.js wechat-native/tests/competition-source.test.js`
Expected: FAIL

**Step 3: Write minimal implementation**

- 小程序签到页同时读取报名记录与签到申请记录
- 根据状态显示“待老师确认 / 已驳回 / 已确认到场”
- 按状态控制按钮可用性与文案

**Step 4: Run test to verify it passes**

Run: `node --test wechat-native/tests/checkin-source.test.js wechat-native/tests/competition-source.test.js`
Expected: PASS

### Task 5: 全量回归验证

**Files:**
- Verify only

**Step 1: Run backend verification**

Run: `mvn -f server/pom.xml -Dtest=CheckinApprovalApiTest,CompetitionFlowApiTest,RegistrationManageApiTest test`
Expected: PASS

**Step 2: Run admin verification**

Run: `pnpm --dir admin-web test -- registration-manage.spec.ts registration-manage-copy.spec.ts`
Expected: PASS

**Step 3: Run miniapp verification**

Run: `node --test wechat-native/tests/*.test.js`
Expected: PASS
