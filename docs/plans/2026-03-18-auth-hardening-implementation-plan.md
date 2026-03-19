# 真鉴权收口 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 为后台与用户端补上服务端真鉴权、角色校验和核心主体一致性校验。

**Architecture:** 采用自定义签名 token + Spring MVC 拦截器的方式，在不引入完整 Spring Security 框架的前提下补齐登录态校验。控制器层负责最小必要的主体一致性校验，服务层尽量保持原有业务职责。

**Tech Stack:** Spring Boot 3、MyBatis-Plus、MockMvc、Vue 3、微信小程序原生项目

---

### Task 1: 补鉴权失败测试

**Files:**
- Modify: `server/src/test/java/com/campus/competition/modules/profile/ProfileApiTest.java`
- Create: `server/src/test/java/com/campus/competition/modules/auth/AuthGuardApiTest.java`

**Step 1: Write the failing test**

- 为后台接口增加“无 token 拒绝访问”和“非管理员 token 拒绝访问”测试。
- 为 app 接口增加“token 用户与请求 userId 不一致时拒绝访问”测试。

**Step 2: Run test to verify it fails**

Run: `mvn -f server/pom.xml "-Dtest=AuthGuardApiTest,ProfileApiTest" test`

Expected: 因缺少鉴权拦截和主体校验而失败。

**Step 3: Commit**

暂不提交，进入实现。

### Task 2: 实现 token 服务与拦截器

**Files:**
- Create: `server/src/main/java/com/campus/competition/modules/auth/security/AuthPrincipal.java`
- Create: `server/src/main/java/com/campus/competition/modules/auth/security/AuthContext.java`
- Create: `server/src/main/java/com/campus/competition/modules/auth/security/AuthTokenService.java`
- Create: `server/src/main/java/com/campus/competition/modules/auth/security/AuthInterceptor.java`
- Create: `server/src/main/java/com/campus/competition/modules/auth/security/AuthPathPolicy.java`
- Create: `server/src/main/java/com/campus/competition/common/config/WebMvcConfig.java`
- Modify: `server/src/main/java/com/campus/competition/modules/auth/service/AuthService.java`

**Step 1: Write minimal implementation**

- 登录时生成签名 token。
- 拦截器解析 `Authorization` 头并校验 token。
- 公开路径跳过鉴权。
- 管理端路径要求 `ADMIN`。

**Step 2: Run test to verify it partially passes**

Run: `mvn -f server/pom.xml "-Dtest=AuthGuardApiTest" test`

Expected: 后台拒绝访问测试转绿，主体一致性测试可能仍失败。

### Task 3: 实现控制器主体一致性校验

**Files:**
- Modify: `server/src/main/java/com/campus/competition/modules/profile/controller/AppProfileController.java`
- Modify: `server/src/main/java/com/campus/competition/modules/message/controller/AppMessageController.java`
- Modify: `server/src/main/java/com/campus/competition/modules/points/controller/AppPointsTaskController.java`
- Modify: `server/src/main/java/com/campus/competition/modules/result/controller/AppResultController.java`
- Modify: `server/src/main/java/com/campus/competition/modules/dashboard/controller/AppTeacherDashboardController.java`
- Modify: `server/src/main/java/com/campus/competition/modules/registration/controller/AppRegistrationController.java`
- Modify: `server/src/main/java/com/campus/competition/modules/checkin/controller/AppCheckinController.java`
- Modify: `server/src/main/java/com/campus/competition/modules/submission/controller/AppSubmissionController.java`

**Step 1: Write minimal implementation**

- 从请求上下文获取当前用户。
- 对核心 app 接口补 userId / teacherId / senderUserId 一致性校验。
- 保持原有响应结构不变。

**Step 2: Run tests**

Run: `mvn -f server/pom.xml "-Dtest=AuthGuardApiTest,ProfileApiTest,AdminUserGovernanceApiTest,AdminGovernanceDeepeningApiTest" test`

Expected: 新老鉴权相关测试通过。

### Task 4: 验证前端兼容性

**Files:**
- Modify: `admin-web/src/__tests__/admin-auth.spec.ts`
- Modify: `wechat-native/tests/session.test.js`

**Step 1: Add or update tests if needed**

- 确认前端仍按 token 字段存储登录态。

**Step 2: Run verification**

Run: `pnpm --dir admin-web exec vitest run`
Run: `pnpm --dir admin-web build`

Expected: 后台前端测试和构建通过。
