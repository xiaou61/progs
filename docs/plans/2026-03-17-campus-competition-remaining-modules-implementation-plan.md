# Campus Competition Remaining Modules Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 在不接入任何第三方服务的前提下，完成 PRD 中剩余的账号增强、比赛增强、消息中心、后台治理、大屏和本地审核能力。

**Progress Status (2026-03-17):** `Task 1` 账号与个人中心基础版、`Task 2` 比赛生命周期增强版、`Task 3` 报名与参赛管理增强版、`Task 4` 评审增强与电子奖状、`Task 5` 每日签到/分享任务/个人比赛汇总已完成；`Task 6` 已完成首批闭环（系统消息、咨询老师私信、比赛群聊、消息中心页面）；`Task 7` 已完成首批闭环（角色列表、创建编辑角色、冻结/解冻、重置密码、改角色、后台真实页面）；`Task 8` 已完成（会话置顶、免打扰、清空）；`Task 9` 已完成（后台汇总大屏、老师个人看板、CSV 导出）；`Task 10` 已完成（比赛/消息/作品本地审核、违规记录与后台审核页），下一步继续做后台治理深化。

**Architecture:** 延续当前单仓多应用结构，在 `server` 内继续按领域扩展模块，在 `miniapp` 和 `admin-web` 上补齐对应页面、状态和路由。所有新增能力都必须复用现有真实登录、比赛主链路与数据库迁移体系，以本地可运行版本为最终交付标准。

**Tech Stack:** Vue 3, Vite, TypeScript, Pinia, uni-app, Spring Boot 3, MyBatis-Plus, MySQL 8, H2(test), Flyway, ECharts

---

## 实施边界

本计划明确不实现以下能力：

- 短信验证码
- 微信服务通知
- 第三方支付
- 第三方物流
- 第三方 AI 审核
- 外部系统对接
- 积分商城与订单
- 多校区高级隔离

## Task 1: 账号与个人中心基础版

**Files:**
- Modify: `server/src/main/resources/db/migration/V1__init_base_schema.sql`
- Create: `server/src/main/resources/db/migration/V7__user_profile_settings.sql`
- Create: `server/src/main/java/com/campus/competition/modules/profile/controller/AppProfileController.java`
- Create: `server/src/main/java/com/campus/competition/modules/profile/model/ProfileSummary.java`
- Create: `server/src/main/java/com/campus/competition/modules/profile/model/UpdateProfileCommand.java`
- Create: `server/src/main/java/com/campus/competition/modules/profile/model/ChangePasswordCommand.java`
- Create: `server/src/main/java/com/campus/competition/modules/profile/model/FeedbackCommand.java`
- Create: `server/src/main/java/com/campus/competition/modules/profile/service/ProfileService.java`
- Create: `server/src/main/java/com/campus/competition/modules/profile/persistence/FeedbackEntity.java`
- Create: `server/src/main/java/com/campus/competition/modules/profile/mapper/FeedbackMapper.java`
- Create: `server/src/test/java/com/campus/competition/modules/profile/ProfileApiTest.java`
- Create: `miniapp/src/api/profile.ts`
- Create: `miniapp/src/pages/profile/index.vue`
- Create: `miniapp/src/__tests__/profile-api.spec.ts`
- Modify: `miniapp/src/pages.json`
- Modify: `miniapp/src/stores/user.ts`
- Modify: `miniapp/src/utils/home-navigation.ts`
- Modify: `miniapp/src/pages/home/index.vue`

**Step 1: Write the failing backend profile API test**

验证获取资料、修改资料、修改密码、提交意见反馈、注销账号五个关键行为。

**Step 2: Run backend test to verify it fails**

Run: `mvn -f server/pom.xml -Dtest=ProfileApiTest test`
Expected: FAIL because profile controller/service/models do not exist

**Step 3: Write the minimal backend implementation**

- 为 `sys_user` 增加头像、组织字段、消息提醒、隐私设置、更新时间
- 新增意见反馈表
- 实现资料查询、资料更新、密码修改、意见反馈、账号注销

**Step 4: Run backend test to verify it passes**

Run: `mvn -f server/pom.xml -Dtest=ProfileApiTest test`
Expected: PASS

**Step 5: Write the failing miniapp profile API test**

验证资料接口请求路径、个人中心菜单路由和更新请求载荷。

**Step 6: Run miniapp test to verify it fails**

Run: `pnpm --dir miniapp test -- --run src/__tests__/profile-api.spec.ts`
Expected: FAIL because profile api/page do not exist

**Step 7: Write the minimal miniapp implementation**

- 新增个人中心页面
- 首页增加入口
- 支持查看和编辑资料、修改密码、反馈、注销

**Step 8: Run miniapp test to verify it passes**

Run: `pnpm --dir miniapp test -- --run src/__tests__/profile-api.spec.ts`
Expected: PASS

## Task 2: 比赛生命周期增强版

**Files:**
- Create: `server/src/main/resources/db/migration/V8__competition_manage_enhancement.sql`
- Create: `server/src/test/java/com/campus/competition/modules/competition/CompetitionManageApiTest.java`
- Modify: `server/src/main/java/com/campus/competition/modules/competition/service/CompetitionService.java`
- Modify: `server/src/main/java/com/campus/competition/modules/competition/controller/AdminCompetitionController.java`
- Create: `server/src/main/java/com/campus/competition/modules/competition/model/SaveCompetitionDraftCommand.java`
- Create: `server/src/main/java/com/campus/competition/modules/competition/model/UpdateCompetitionCommand.java`
- Create: `server/src/main/java/com/campus/competition/modules/competition/model/CompetitionDraftSummary.java`
- Create: `admin-web/src/__tests__/competition-manage.spec.ts`
- Modify: `admin-web/src/views/competition/CompetitionEditorPage.vue`
- Create: `admin-web/src/api/competition-manage.ts`
- Modify: `miniapp/src/api/competition.ts`
- Modify: `miniapp/src/pages/teacher/competition-editor/index.vue`

**Step 1: Write the failing competition manage test**

覆盖草稿保存、比赛编辑、比赛下架、推荐/置顶状态更新。

**Step 2: Run backend test to verify it fails**

Run: `mvn -f server/pom.xml -Dtest=CompetitionManageApiTest test`
Expected: FAIL because draft/edit/offline endpoints do not exist

**Step 3: Write the minimal backend implementation**

- 新增草稿与管理字段
- 支持保存草稿、编辑比赛、下架比赛

**Step 4: Run backend test to verify it passes**

Run: `mvn -f server/pom.xml -Dtest=CompetitionManageApiTest test`
Expected: PASS

**Step 5: Write the failing admin/miniapp tests**

验证老师端和后台比赛管理页能调用草稿、编辑、下架接口。

**Step 6: Run frontend tests to verify they fail**

Run: `pnpm --dir admin-web test -- --run src/__tests__/competition-manage.spec.ts`
Expected: FAIL

**Step 7: Write the minimal frontend implementation**

- 后台与老师端比赛编辑器支持草稿、编辑、下架

**Step 8: Run frontend tests to verify they pass**

Run: `pnpm --dir admin-web test -- --run src/__tests__/competition-manage.spec.ts`
Expected: PASS

## Task 3: 报名与参赛管理增强版

**Files:**
- Create: `server/src/main/resources/db/migration/V9__registration_manage_enhancement.sql`
- Create: `server/src/test/java/com/campus/competition/modules/registration/RegistrationManageApiTest.java`
- Modify: `server/src/main/java/com/campus/competition/modules/registration/service/RegistrationService.java`
- Modify: `server/src/main/java/com/campus/competition/modules/registration/controller/AppRegistrationController.java`
- Create: `server/src/main/java/com/campus/competition/modules/registration/controller/AdminRegistrationController.java`
- Create: `admin-web/src/__tests__/registration-manage.spec.ts`
- Create: `admin-web/src/views/competition/RegistrationManagePage.vue`
- Create: `admin-web/src/api/registration-manage.ts`
- Modify: `miniapp/src/pages/competition/register/index.vue`

**Step 1: Write the failing registration manage test**

覆盖报名取消、报名驳回、手动添加、到场/缺席标记。

**Step 2: Run backend test to verify it fails**

Run: `mvn -f server/pom.xml -Dtest=RegistrationManageApiTest test`
Expected: FAIL

**Step 3: Write the minimal backend implementation**

- 扩展报名状态和管理操作
- 补管理员视角接口

**Step 4: Run backend test to verify it passes**

Run: `mvn -f server/pom.xml -Dtest=RegistrationManageApiTest test`
Expected: PASS

**Step 5: Write the failing frontend test**

验证后台参赛管理与学生端取消报名入口。

**Step 6: Run frontend test to verify it fails**

Run: `pnpm --dir admin-web test -- --run src/__tests__/registration-manage.spec.ts`
Expected: FAIL

**Step 7: Write the minimal frontend implementation**

- 后台参赛管理页
- 学生端取消报名能力

**Step 8: Run frontend test to verify it passes**

Run: `pnpm --dir admin-web test -- --run src/__tests__/registration-manage.spec.ts`
Expected: PASS

## Task 4: 评审增强与电子奖状

**Files:**
- Create: `server/src/main/resources/db/migration/V10__review_certificate_enhancement.sql`
- Create: `server/src/test/java/com/campus/competition/modules/review/ReviewCertificateApiTest.java`
- Create: `server/src/main/java/com/campus/competition/modules/review/model/SubmitReviewCommand.java`
- Create: `server/src/main/java/com/campus/competition/modules/review/persistence/ReviewTaskEntity.java`
- Create: `server/src/main/java/com/campus/competition/modules/review/mapper/ReviewTaskMapper.java`
- Create: `server/src/main/java/com/campus/competition/modules/review/service/ReviewService.java`
- Modify: `server/src/main/java/com/campus/competition/modules/review/controller/AdminReviewController.java`
- Modify: `server/src/main/java/com/campus/competition/modules/review/model/ReviewTaskSummary.java`
- Modify: `server/src/main/java/com/campus/competition/modules/score/model/PublishResultCommand.java`
- Modify: `server/src/main/java/com/campus/competition/modules/score/model/ScoreSummary.java`
- Modify: `server/src/main/java/com/campus/competition/modules/score/persistence/ScoreEntity.java`
- Modify: `server/src/main/java/com/campus/competition/modules/score/service/ScoreService.java`
- Modify: `admin-web/src/api/review.ts`
- Modify: `admin-web/src/views/review/ReviewWorkbenchPage.vue`
- Modify: `admin-web/src/__tests__/api-clients.spec.ts`
- Modify: `admin-web/src/__tests__/score-publish.spec.ts`
- Modify: `miniapp/src/api/result.ts`
- Modify: `miniapp/src/pages/competition/result/index.vue`
- Modify: `miniapp/src/__tests__/api-clients.spec.ts`

**Step 1: Write the failing review/certificate backend test**

覆盖提交评审意见、查看评审任务、发布结果携带评语，以及学生端结果返回电子奖状信息。

**Step 2: Run backend test to verify it fails**

Run: `mvn -f server/pom.xml -Dtest=ReviewCertificateApiTest test`
Expected: FAIL because review submit endpoint and certificate fields do not exist

**Step 3: Write the minimal backend implementation**

- 扩展评审任务表字段，支持学生维度、评审意见、建议分数、评审时间
- 发布结果时保存评审老师、评审意见、电子奖状编号和标题
- 学生端结果接口同步返回评审意见和奖状信息

**Step 4: Run backend test to verify it passes**

Run: `mvn -f server/pom.xml "-Dtest=ReviewCertificateApiTest,ScorePublishTest,ScorePointsPersistenceTest" test`
Expected: PASS

**Step 5: Write the failing frontend tests**

验证后台评审工作台可提交评审意见、结果发布请求可携带评语，以及小程序结果接口模型能读取奖状字段。

**Step 6: Run frontend tests to verify they fail**

Run: `pnpm --dir admin-web test -- --run src/__tests__/api-clients.spec.ts src/__tests__/score-publish.spec.ts`
Expected: FAIL

**Step 7: Write the minimal frontend implementation**

- 后台工作台支持先提交评审意见，再将建议分数和评语带入结果发布
- 小程序结果页支持查看评审老师、评审意见和电子奖状编号

**Step 8: Run frontend tests to verify they pass**

Run: `pnpm --dir admin-web test -- --run src/__tests__/api-clients.spec.ts src/__tests__/score-publish.spec.ts`
Expected: PASS

## Task 5: 每日签到、分享任务与个人比赛汇总

**Files:**
- Create: `server/src/main/resources/db/migration/V11__daily_task_overview.sql`
- Create: `server/src/test/java/com/campus/competition/modules/points/DailyTaskOverviewApiTest.java`
- Create: `server/src/main/java/com/campus/competition/modules/points/controller/AppPointsTaskController.java`
- Create: `server/src/main/java/com/campus/competition/modules/points/model/DailyCheckinCommand.java`
- Create: `server/src/main/java/com/campus/competition/modules/points/model/ShareCompetitionCommand.java`
- Create: `server/src/main/java/com/campus/competition/modules/points/model/DailyTaskSummary.java`
- Create: `server/src/main/java/com/campus/competition/modules/points/model/PersonalCompetitionOverview.java`
- Modify: `server/src/main/java/com/campus/competition/modules/points/service/PointsService.java`
- Modify: `server/src/main/java/com/campus/competition/modules/result/controller/AppResultController.java`
- Create: `miniapp/src/api/daily-task.ts`
- Create: `miniapp/src/utils/daily-task.ts`
- Modify: `miniapp/src/pages/home/index.vue`
- Modify: `miniapp/src/pages/profile/index.vue`
- Modify: `miniapp/src/pages/competition/detail/index.vue`
- Modify: `miniapp/src/pages/points/index.vue`
- Create: `miniapp/src/__tests__/daily-task.spec.ts`
- Create: `miniapp/src/__tests__/daily-task-api.spec.ts`

**Step 1: Write the failing backend daily task test**

覆盖学生每日签到、每日分享比赛领积分、重复领取拦截，以及个人比赛汇总查询。

**Step 2: Run backend test to verify it fails**

Run: `mvn -f server/pom.xml -Dtest=DailyTaskOverviewApiTest test`
Expected: FAIL because daily task endpoints and overview model do not exist

**Step 3: Write the minimal backend implementation**

- 为积分流水增加任务来源语义，支持 `DAILY_CHECKIN` 和 `COMPETITION_SHARE`
- 新增每日签到和比赛分享接口，每日同类任务仅允许成功一次
- 新增个人比赛汇总接口，返回我参加的比赛、作品、获奖、积分汇总数据

**Step 4: Run backend test to verify it passes**

Run: `mvn -f server/pom.xml -Dtest=DailyTaskOverviewApiTest test`
Expected: PASS

**Step 5: Write the failing miniapp tests**

验证首页/个人中心的每日签到入口、比赛详情分享入口和个人比赛汇总展示所需 API 与格式化逻辑。

**Step 6: Run miniapp tests to verify they fail**

Run: `pnpm --dir miniapp test -- --run src/__tests__/daily-task.spec.ts src/__tests__/daily-task-api.spec.ts`
Expected: FAIL

**Step 7: Write the minimal miniapp implementation**

- 首页与个人中心展示每日签到卡片和个人比赛汇总
- 比赛详情页增加分享积分入口
- 积分页展示日常任务积分流水和今日状态

**Step 8: Run miniapp tests to verify they pass**

Run: `pnpm --dir miniapp test -- --run src/__tests__/daily-task.spec.ts src/__tests__/daily-task-api.spec.ts`
Expected: PASS

## Task 6: 系统消息、私信、比赛群聊

**Files:**
- Create: `server/src/main/resources/db/migration/V12__message_center_schema.sql`
- Create: `server/src/test/java/com/campus/competition/modules/message/MessageCenterApiTest.java`
- Create: `server/src/main/java/com/campus/competition/modules/message/controller/AppMessageController.java`
- Create: `server/src/main/java/com/campus/competition/modules/message/service/MessageService.java`
- Create: `server/src/main/java/com/campus/competition/modules/message/model/SystemMessageSummary.java`
- Create: `server/src/main/java/com/campus/competition/modules/message/model/ConversationSummary.java`
- Create: `server/src/main/java/com/campus/competition/modules/message/model/ChatMessageSummary.java`
- Create: `server/src/main/java/com/campus/competition/modules/message/model/ConsultTeacherCommand.java`
- Create: `server/src/main/java/com/campus/competition/modules/message/model/SendPrivateMessageCommand.java`
- Create: `server/src/main/java/com/campus/competition/modules/message/model/SendGroupMessageCommand.java`
- Create: `server/src/main/java/com/campus/competition/modules/message/model/ConsultConversationSummary.java`
- Create: `server/src/main/java/com/campus/competition/modules/message/persistence/SystemMessageEntity.java`
- Create: `server/src/main/java/com/campus/competition/modules/message/persistence/ConversationEntity.java`
- Create: `server/src/main/java/com/campus/competition/modules/message/persistence/MessageEntity.java`
- Create: `server/src/main/java/com/campus/competition/modules/message/mapper/SystemMessageMapper.java`
- Create: `server/src/main/java/com/campus/competition/modules/message/mapper/ConversationMapper.java`
- Create: `server/src/main/java/com/campus/competition/modules/message/mapper/MessageMapper.java`
- Create: `miniapp/src/api/message.ts`
- Create: `miniapp/src/utils/message-navigation.ts`
- Create: `miniapp/src/pages/message/index.vue`
- Create: `miniapp/src/pages/message/chat/index.vue`
- Create: `miniapp/src/__tests__/message-api.spec.ts`
- Create: `miniapp/src/__tests__/message-navigation.spec.ts`
- Modify: `miniapp/src/pages/competition/detail/index.vue`
- Modify: `miniapp/src/pages/home/index.vue`
- Modify: `miniapp/src/pages.json`
- Modify: `miniapp/src/stores/user.ts`
- Modify: `miniapp/src/utils/home-navigation.ts`

**Step 1: Write the failing backend message center test**

覆盖系统消息列表、比赛详情咨询老师、私信发送、比赛群聊发送和会话未读数。

**Step 2: Run backend test to verify it fails**

Run: `mvn -f server/pom.xml -Dtest=MessageCenterApiTest test`
Expected: FAIL because message endpoints and schema do not exist

**Step 3: Write the minimal backend implementation**

- 新增系统消息、会话、消息三张表
- 实现系统消息列表、咨询老师发起私信、私信消息列表、比赛群聊消息列表
- 会话列表支持按最新消息排序并返回未读数

**Step 4: Run backend test to verify it passes**

Run: `mvn -f server/pom.xml -Dtest=MessageCenterApiTest test`
Expected: PASS

**Step 5: Write the failing miniapp message tests**

验证消息 API 请求路径、首页消息入口路由和聊天页路由构造。

**Step 6: Run miniapp tests to verify they fail**

Run: `pnpm --dir miniapp test -- --run src/__tests__/message-api.spec.ts src/__tests__/message-navigation.spec.ts`
Expected: FAIL

**Step 7: Write the minimal miniapp implementation**

- 首页增加消息中心入口
- 新增消息中心页与会话详情页
- 比赛详情页增加“咨询发起人”入口

**Step 8: Run miniapp tests to verify they pass**

Run: `pnpm --dir miniapp test -- --run src/__tests__/message-api.spec.ts src/__tests__/message-navigation.spec.ts`
Expected: PASS

## Task 7: 后台角色权限与用户治理

**Files:**
- Create: `server/src/main/resources/db/migration/V13__role_governance_schema.sql`
- Create: `server/src/test/java/com/campus/competition/modules/user/AdminUserGovernanceApiTest.java`
- Create: `server/src/main/java/com/campus/competition/modules/role/persistence/RoleEntity.java`
- Create: `server/src/main/java/com/campus/competition/modules/role/mapper/RoleMapper.java`
- Create: `server/src/main/java/com/campus/competition/modules/role/model/RoleSummary.java`
- Create: `server/src/main/java/com/campus/competition/modules/role/model/SaveRoleCommand.java`
- Create: `server/src/main/java/com/campus/competition/modules/role/service/RoleService.java`
- Create: `server/src/main/java/com/campus/competition/modules/role/controller/AdminRoleController.java`
- Create: `server/src/main/java/com/campus/competition/modules/user/model/FreezeUserCommand.java`
- Create: `server/src/main/java/com/campus/competition/modules/user/model/ResetPasswordCommand.java`
- Create: `server/src/main/java/com/campus/competition/modules/user/model/AssignUserRoleCommand.java`
- Create: `server/src/main/java/com/campus/competition/modules/user/service/AdminUserService.java`
- Create: `server/src/main/java/com/campus/competition/modules/user/service/AdminUserServiceImpl.java`
- Modify: `server/src/main/java/com/campus/competition/modules/user/controller/AdminUserController.java`
- Modify: `server/src/main/java/com/campus/competition/modules/common/config/MybatisPlusConfig.java`
- Modify: `server/src/main/java/com/campus/competition/modules/profile/mapper/FeedbackMapper.java`
- Create: `admin-web/src/api/roles.ts`
- Modify: `admin-web/src/api/users.ts`
- Create: `admin-web/src/utils/role-governance.ts`
- Modify: `admin-web/src/views/system/users/UserListPage.vue`
- Modify: `admin-web/src/views/system/roles/RoleListPage.vue`
- Modify: `admin-web/src/__tests__/api-clients.spec.ts`
- Create: `admin-web/src/__tests__/role-governance.spec.ts`

**Step 1: Write the failing backend governance test**

覆盖角色列表、创建角色、编辑角色、冻结账号、冻结后登录失败、解冻账号、重置密码、分配角色与重新登录。

**Step 2: Run backend test to verify it fails**

Run: `mvn -f server/pom.xml -Dtest=AdminUserGovernanceApiTest test`
Expected: FAIL because role endpoints and user governance actions do not exist

**Step 3: Write the minimal backend implementation**

- 新增 `sys_role` 表和内置角色种子
- 实现角色列表、创建、编辑
- 实现用户冻结/解冻、重置密码、改角色
- 收紧 `MapperScan`，避免服务接口被误扫为 MyBatis Mapper

**Step 4: Run backend test to verify it passes**

Run: `mvn -f server/pom.xml -Dtest=AdminUserGovernanceApiTest test`
Expected: PASS

**Step 5: Write the failing admin governance tests**

验证角色 API 请求路径、用户治理 API 请求路径，以及角色表单/密码规则工具。

**Step 6: Run frontend tests to verify they fail**

Run: `pnpm --dir admin-web test -- --run src/__tests__/api-clients.spec.ts src/__tests__/role-governance.spec.ts`
Expected: FAIL

**Step 7: Write the minimal frontend implementation**

- 角色页支持列表、创建、编辑和权限勾选
- 用户治理页支持选中用户后改角色、冻结/解冻和重置密码

**Step 8: Run frontend tests to verify they pass**

Run: `pnpm --dir admin-web test -- --run src/__tests__/api-clients.spec.ts src/__tests__/role-governance.spec.ts`
Expected: PASS

## 后续任务顺序

10. [x] 本地敏感词审核与违规记录
11. 后台治理深化（菜单级权限、日志导出、更多治理动作）
12. 文档、验收清单与全量回归
