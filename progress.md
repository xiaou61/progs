# Progress Log

## Session: 2026-03-17

### Phase 1: 需求读取与范围确认
- **Status:** complete
- **Started:** 2026-03-17
- Actions taken:
  - 扫描项目目录，确认当前是空项目，仅包含一个 docx 需求文档
  - 定位需求文档 `重新师生系统.docx`
  - 读取相关技能说明，确定采用需求提取 + 文件化规划方式推进
  - 使用 `pandoc` 提取 docx 内容并完成初步需求拆解
  - 将提取结果保存为 `docx_requirements.md`，便于后续按章节检索
- Files created/modified:
  - `task_plan.md`（created）
  - `findings.md`（created）
  - `progress.md`（created）
  - `docx_requirements.md`（created）

### Phase 2: 架构规划与模块拆解
- **Status:** complete
- Actions taken:
  - 识别核心角色：学生、老师、管理员
  - 识别核心业务域：比赛、报名、签到、作品、评审、评分、积分、奖品、订单、消息、数据大屏、系统配置
  - 整理总体架构、领域拆分、接口分组、阶段里程碑
  - 产出正式设计文档与实施计划文档
- Files created/modified:
  - `task_plan.md`
  - `findings.md`
  - `progress.md`
  - `docs/plans/2026-03-17-campus-competition-platform-design.md`（created）
  - `docs/plans/2026-03-17-campus-competition-platform-implementation-plan.md`（created）

### Phase 3: 首批任务执行（Task 1-3）
- **Status:** complete
- Actions taken:
  - 创建 `scripts/verify-structure.ps1`，先在空目录下执行，确认因缺少目录而失败
  - 创建 `README.md`、`pnpm-workspace.yaml`、`.editorconfig`、`.gitignore`、`infra/docker-compose.yml`
  - 创建 `admin-web`、`miniapp`、`server` 基础目录并执行 `git init`
  - 新建 `server/pom.xml` 和 `HealthControllerTest`，先让测试因为缺少 Spring Boot 启动类而失败
  - 补齐 `CampusCompetitionApplication`、`HealthController`、`application.yml`、`application-dev.yml`
  - 新建 `ApiResponseTest`，先让测试因为缺少 `ApiResponse` 而失败
  - 补齐 `ApiResponse`、`GlobalExceptionHandler`、`UserContext` 以及公共配置占位类
  - 重新运行后端测试，确认全部通过
- Files created/modified:
  - `README.md`（created）
  - `pnpm-workspace.yaml`（created）
  - `.editorconfig`（created）
  - `.gitignore`（created）
  - `infra/docker-compose.yml`（created）
  - `docs/runbooks/local-setup.md`（created）
  - `scripts/verify-structure.ps1`（created）
  - `server/pom.xml`（created）
  - `server/src/main/java/com/campus/competition/CampusCompetitionApplication.java`（created）
  - `server/src/main/java/com/campus/competition/modules/common/controller/HealthController.java`（created）
  - `server/src/main/java/com/campus/competition/modules/common/model/ApiResponse.java`（created）
  - `server/src/main/java/com/campus/competition/modules/common/exception/GlobalExceptionHandler.java`（created）
  - `server/src/main/java/com/campus/competition/modules/common/config/SaTokenConfig.java`（created）
  - `server/src/main/java/com/campus/competition/modules/common/config/MybatisPlusConfig.java`（created）
  - `server/src/main/java/com/campus/competition/modules/common/util/UserContext.java`（created）
  - `server/src/main/resources/application.yml`（created）
  - `server/src/main/resources/application-dev.yml`（created）
  - `server/src/test/java/com/campus/competition/HealthControllerTest.java`（created）
  - `server/src/test/java/com/campus/competition/ApiResponseTest.java`（created）

### Phase 4: 第二批任务执行（Task 4-6）
- **Status:** complete
- Actions taken:
  - 为 `admin-web` 创建 `package.json`、`vite.config.ts`、`tsconfig.json` 与 `router.spec.ts`
  - 使用 TDD 让后台路由测试先因缺少 `router` 失败，再补齐 `App.vue`、`main.ts`、`router`、`Pinia store`、登录页和工作台页
  - 通过 `pnpm install --node-linker=hoisted` 与 `pnpm --dir admin-web build` 完成后台脚手架验证
  - 为 `miniapp` 创建 `uni-app` 基础配置、`pages.json`、`manifest.json` 与 `role-menu.spec.ts`
  - 排查 `pnpm` 在当前盘符上的链接问题，增加根目录 `.npmrc` 并在 `miniapp` 使用隔离安装参数
  - 修正 `uni-app` 插件导出方式、切换到 `uni` CLI 脚本、补齐 `index.html` 和最小 `App.vue`
  - 补齐 `miniapp` 的 `main.ts`、`stores/user.ts`、登录页和首页，并通过测试与 H5 构建
  - 为账号基础域新增 `AuthServiceTest`，先让测试因缺少 `AuthService` 与模型类失败
  - 补齐 `RegisterCommand`、`LoginCommand`、`LoginResult`、`UserSummary`、`AuthService`、`AppAuthController`、`AdminUserController`、`AdminCampusController`
  - 新增基础 SQL 迁移脚本 `V1__init_base_schema.sql`
  - 串行执行后端全量测试，确认 3 个测试全部通过
- Files created/modified:
  - `.npmrc`（created）
  - `admin-web/package.json`（created）
  - `admin-web/tsconfig.json`（created）
  - `admin-web/vite.config.ts`（created）
  - `admin-web/index.html`（created）
  - `admin-web/src/App.vue`（created）
  - `admin-web/src/main.ts`（created）
  - `admin-web/src/router/index.ts`（created）
  - `admin-web/src/stores/app.ts`（created）
  - `admin-web/src/views/login/LoginPage.vue`（created）
  - `admin-web/src/views/dashboard/DashboardPage.vue`（created）
  - `admin-web/src/env.d.ts`（created）
  - `admin-web/src/__tests__/router.spec.ts`（created）
  - `miniapp/package.json`（created）
  - `miniapp/tsconfig.json`（created）
  - `miniapp/vite.config.ts`（created）
  - `miniapp/index.html`（created）
  - `miniapp/src/App.vue`（created）
  - `miniapp/src/main.ts`（created）
  - `miniapp/src/pages.json`（created）
  - `miniapp/src/manifest.json`（created）
  - `miniapp/src/env.d.ts`（created）
  - `miniapp/src/stores/user.ts`（created）
  - `miniapp/src/pages/login/index.vue`（created）
  - `miniapp/src/pages/home/index.vue`（created）
  - `miniapp/src/__tests__/role-menu.spec.ts`（created）
  - `server/pom.xml`（modified）
  - `server/src/main/resources/db/migration/V1__init_base_schema.sql`（created）
  - `server/src/main/java/com/campus/competition/modules/auth/model/RegisterCommand.java`（created）
  - `server/src/main/java/com/campus/competition/modules/auth/model/LoginCommand.java`（created）
  - `server/src/main/java/com/campus/competition/modules/auth/model/LoginResult.java`（created）
  - `server/src/main/java/com/campus/competition/modules/auth/model/UserSummary.java`（created）
  - `server/src/main/java/com/campus/competition/modules/auth/service/AuthService.java`（created）
  - `server/src/main/java/com/campus/competition/modules/auth/controller/AppAuthController.java`（created）
  - `server/src/main/java/com/campus/competition/modules/user/controller/AdminUserController.java`（created）
  - `server/src/main/java/com/campus/competition/modules/org/controller/AdminCampusController.java`（created）
  - `server/src/test/java/com/campus/competition/modules/auth/AuthServiceTest.java`（created）

### Phase 5: 第三批任务执行（Task 7-9）
- **Status:** complete
- Actions taken:
  - 为后台权限菜单补充测试与菜单构造工具，新增用户、角色、校区 API 模块和页面
  - 扩展后台路由，补齐用户、角色、校区管理页与比赛发布页
  - 为比赛发布流程新增 `CompetitionPublishTest`，实现比赛基础域、比赛列表与详情接口
  - 为学生报名流程新增 `RegistrationServiceTest`，实现报名服务、报名接口以及小程序比赛列表、详情、报名页面
  - 串行执行后端全量测试，并复跑后台测试/构建和小程序测试/H5 构建
- Files created/modified:
  - `admin-web/src/__tests__/permission-menu.spec.ts`（created）
  - `admin-web/src/utils/menus.ts`（created）
  - `admin-web/src/api/auth.ts`（created）
  - `admin-web/src/api/users.ts`（created）
  - `admin-web/src/api/campuses.ts`（created）
  - `admin-web/src/views/system/users/UserListPage.vue`（created）
  - `admin-web/src/views/system/roles/RoleListPage.vue`（created）
  - `admin-web/src/views/system/campuses/CampusListPage.vue`（created）
  - `admin-web/src/views/competition/CompetitionEditorPage.vue`（created）
  - `admin-web/src/router/index.ts`（modified）
  - `miniapp/src/pages/teacher/competition-editor/index.vue`（created）
  - `miniapp/src/pages/competition/list/index.vue`（created）
  - `miniapp/src/pages/competition/detail/index.vue`（created）
  - `miniapp/src/pages/competition/register/index.vue`（created）
  - `miniapp/src/pages.json`（modified）
  - `server/src/main/resources/db/migration/V2__competition_schema.sql`（created）
  - `server/src/main/resources/db/migration/V3__registration_schema.sql`（created）
  - `server/src/main/java/com/campus/competition/modules/competition/model/PublishCompetitionCommand.java`（created）
  - `server/src/main/java/com/campus/competition/modules/competition/model/CompetitionSummary.java`（created）
  - `server/src/main/java/com/campus/competition/modules/competition/model/CompetitionDetail.java`（created）
  - `server/src/main/java/com/campus/competition/modules/competition/service/CompetitionService.java`（created）
  - `server/src/main/java/com/campus/competition/modules/competition/controller/AppCompetitionController.java`（created）
  - `server/src/main/java/com/campus/competition/modules/competition/controller/AdminCompetitionController.java`（created）
  - `server/src/main/java/com/campus/competition/modules/registration/model/RegisterCompetitionCommand.java`（created）
  - `server/src/main/java/com/campus/competition/modules/registration/model/RegistrationSummary.java`（created）
  - `server/src/main/java/com/campus/competition/modules/registration/service/RegistrationService.java`（created）
  - `server/src/main/java/com/campus/competition/modules/registration/controller/AppRegistrationController.java`（created）
  - `server/src/test/java/com/campus/competition/modules/competition/CompetitionPublishTest.java`（created）
  - `server/src/test/java/com/campus/competition/modules/registration/RegistrationServiceTest.java`（created）

### Phase 6: 第四批任务执行（Task 10-12）
- **Status:** complete
- Actions taken:
  - 为签到和作品上传流程新增 `CheckinServiceTest`、`SubmissionServiceTest`，先验证缺少领域实现时会红灯
  - 新增 `V4__checkin_submission_schema.sql`，补齐签到/作品模型、服务、控制器，以及小程序签到页、作品上传页
  - 为结果发布与积分入账新增 `ScorePublishTest`，补齐 `PointsService`、`ScoreService`、评审/评分/结果控制器，以及后台评审工作台、小程序结果页和积分页
  - 为后台日志接口新增 `AdminLogControllerTest`，补齐轮播图、系统配置、日志控制器与后台对应页面
  - 新增 MVP 接口清单、数据库说明和验收手册，完成本轮交付文档
  - 串行执行后端全量测试，复跑后台测试/构建和小程序测试/H5 构建，确认整批通过
- Files created/modified:
  - `server/src/main/resources/db/migration/V4__checkin_submission_schema.sql`（created）
  - `server/src/main/resources/db/migration/V5__review_score_points_schema.sql`（created）
  - `server/src/main/java/com/campus/competition/modules/checkin/model/CheckInCommand.java`（created）
  - `server/src/main/java/com/campus/competition/modules/checkin/model/CheckinSummary.java`（created）
  - `server/src/main/java/com/campus/competition/modules/checkin/service/CheckinService.java`（created）
  - `server/src/main/java/com/campus/competition/modules/checkin/controller/AppCheckinController.java`（created）
  - `server/src/main/java/com/campus/competition/modules/submission/model/SubmitWorkCommand.java`（created）
  - `server/src/main/java/com/campus/competition/modules/submission/model/SubmissionSummary.java`（created）
  - `server/src/main/java/com/campus/competition/modules/submission/service/SubmissionService.java`（created）
  - `server/src/main/java/com/campus/competition/modules/submission/controller/AppSubmissionController.java`（created）
  - `server/src/main/java/com/campus/competition/modules/points/model/PointsAccountSummary.java`（created）
  - `server/src/main/java/com/campus/competition/modules/points/model/PointsRecordSummary.java`（created）
  - `server/src/main/java/com/campus/competition/modules/points/service/PointsService.java`（created）
  - `server/src/main/java/com/campus/competition/modules/score/model/PublishResultCommand.java`（created）
  - `server/src/main/java/com/campus/competition/modules/score/model/ScoreSummary.java`（created）
  - `server/src/main/java/com/campus/competition/modules/score/service/ScoreService.java`（created）
  - `server/src/main/java/com/campus/competition/modules/review/model/ReviewTaskSummary.java`（created）
  - `server/src/main/java/com/campus/competition/modules/review/controller/AdminReviewController.java`（created）
  - `server/src/main/java/com/campus/competition/modules/score/controller/AdminScoreController.java`（created）
  - `server/src/main/java/com/campus/competition/modules/result/controller/AppResultController.java`（created）
  - `server/src/main/java/com/campus/competition/modules/system/controller/AdminBannerController.java`（created）
  - `server/src/main/java/com/campus/competition/modules/system/controller/AdminConfigController.java`（created）
  - `server/src/main/java/com/campus/competition/modules/log/controller/AdminLogController.java`（created）
  - `server/src/test/java/com/campus/competition/modules/checkin/CheckinServiceTest.java`（created）
  - `server/src/test/java/com/campus/competition/modules/submission/SubmissionServiceTest.java`（created）
  - `server/src/test/java/com/campus/competition/modules/score/ScorePublishTest.java`（created）
  - `server/src/test/java/com/campus/competition/modules/log/AdminLogControllerTest.java`（created）
  - `admin-web/src/views/review/ReviewWorkbenchPage.vue`（created）
  - `admin-web/src/views/system/banner/BannerPage.vue`（created）
  - `admin-web/src/views/system/config/SystemConfigPage.vue`（created）
  - `admin-web/src/router/index.ts`（modified）
  - `miniapp/src/pages/competition/checkin/index.vue`（created）
  - `miniapp/src/pages/competition/submission/index.vue`（created）
  - `miniapp/src/pages/competition/result/index.vue`（created）
  - `miniapp/src/pages/points/index.vue`（created）
  - `miniapp/src/pages.json`（modified）
  - `docs/api/mvp-api-list.md`（created）
  - `docs/db/mvp-schema.md`（created）
  - `docs/runbooks/mvp-acceptance.md`（created）

### Phase 7: 第五批任务执行（持久化底座与核心域落库）
- **Status:** complete
- Actions taken:
  - 为账号域新增 `AuthPersistenceTest`，为比赛/报名域新增 `CompetitionRegistrationPersistenceTest`，先验证缺少 Mapper、实体和事务支持时会红灯
  - 在 `pom.xml` 中引入 `MyBatis-Plus`、`Flyway`、`MySQL` 驱动与测试用 `H2`
  - 新增 `application-test.yml`，使用 H2 的 MySQL 模式承载测试环境迁移与落库验证
  - 新增 `UserEntity`、`CompetitionEntity`、`RegistrationEntity` 以及对应 `Mapper`
  - 将 `AuthService`、`CompetitionService`、`RegistrationService` 重构为“双通道”模式：Spring Bean 环境走数据库，手工 new 的测试仍可走最小内存实现
  - 更新 `HealthControllerTest` 和 `AdminLogControllerTest` 使用 `test` profile，确保集成测试统一走测试数据库
  - 串行执行新增落库测试与后端全量测试，确认全部通过
- Files created/modified:
  - `server/pom.xml`（modified）
  - `server/src/main/resources/application.yml`（modified）
  - `server/src/main/resources/application-test.yml`（created）
  - `server/src/main/java/com/campus/competition/modules/common/config/MybatisPlusConfig.java`（modified）
  - `server/src/main/java/com/campus/competition/modules/auth/persistence/UserEntity.java`（created）
  - `server/src/main/java/com/campus/competition/modules/auth/mapper/UserMapper.java`（created）
  - `server/src/main/java/com/campus/competition/modules/competition/persistence/CompetitionEntity.java`（created）
  - `server/src/main/java/com/campus/competition/modules/competition/mapper/CompetitionMapper.java`（created）
  - `server/src/main/java/com/campus/competition/modules/registration/persistence/RegistrationEntity.java`（created）
  - `server/src/main/java/com/campus/competition/modules/registration/mapper/RegistrationMapper.java`（created）
  - `server/src/main/java/com/campus/competition/modules/auth/service/AuthService.java`（modified）
  - `server/src/main/java/com/campus/competition/modules/competition/service/CompetitionService.java`（modified）
  - `server/src/main/java/com/campus/competition/modules/registration/service/RegistrationService.java`（modified）
  - `server/src/test/java/com/campus/competition/modules/auth/AuthPersistenceTest.java`（created）
  - `server/src/test/java/com/campus/competition/modules/competition/CompetitionRegistrationPersistenceTest.java`（created）
  - `server/src/test/java/com/campus/competition/HealthControllerTest.java`（modified）
  - `server/src/test/java/com/campus/competition/modules/log/AdminLogControllerTest.java`（modified）

### Phase 8: 第六批任务执行（剩余核心域持久化）
- **Status:** complete
- Actions taken:
  - 为签到、作品、结果、积分领域新增 `CheckinPersistenceTest`、`SubmissionPersistenceTest`、`ScorePointsPersistenceTest`，先验证缺少实体、Mapper 和数据库分支时会红灯
  - 新增 `CheckinEntity`、`SubmissionEntity`、`ScoreEntity`、`PointsAccountEntity`、`PointsRecordEntity` 以及对应 `Mapper`
  - 将 `CheckinService`、`SubmissionService`、`ScoreService`、`PointsService` 扩展为“双通道”模式：Spring Bean 环境走数据库，手工 `new` 的测试继续走内存实现
  - 在结果查询丢失积分值的问题上补了一轮 TDD，新增 `V6__score_points_column.sql`，补齐 `cmp_score.points` 列以及持久化读写
  - 串行执行新增落库测试与后端全量测试，确认剩余核心域迁移后整体仍全部通过
- Files created/modified:
  - `server/src/main/resources/db/migration/V6__score_points_column.sql`（created）
  - `server/src/main/java/com/campus/competition/modules/checkin/persistence/CheckinEntity.java`（created）
  - `server/src/main/java/com/campus/competition/modules/checkin/mapper/CheckinMapper.java`（created）
  - `server/src/main/java/com/campus/competition/modules/submission/persistence/SubmissionEntity.java`（created）
  - `server/src/main/java/com/campus/competition/modules/submission/mapper/SubmissionMapper.java`（created）
  - `server/src/main/java/com/campus/competition/modules/score/persistence/ScoreEntity.java`（created）
  - `server/src/main/java/com/campus/competition/modules/score/mapper/ScoreMapper.java`（created）
  - `server/src/main/java/com/campus/competition/modules/points/persistence/PointsAccountEntity.java`（created）
  - `server/src/main/java/com/campus/competition/modules/points/persistence/PointsRecordEntity.java`（created）
  - `server/src/main/java/com/campus/competition/modules/points/mapper/PointsAccountMapper.java`（created）
  - `server/src/main/java/com/campus/competition/modules/points/mapper/PointsRecordMapper.java`（created）
  - `server/src/main/java/com/campus/competition/modules/checkin/service/CheckinService.java`（modified）
  - `server/src/main/java/com/campus/competition/modules/submission/service/SubmissionService.java`（modified）
  - `server/src/main/java/com/campus/competition/modules/score/service/ScoreService.java`（modified）
  - `server/src/main/java/com/campus/competition/modules/points/service/PointsService.java`（modified）

### Phase 9: 第七批任务执行（消息中心首批闭环）
- **Status:** complete
- Actions taken:
  - 为消息中心新增 `MessageCenterApiTest`，先验证系统消息、咨询老师私信、比赛群聊和会话未读数链路会红灯
  - 新增 `V12__message_center_schema.sql`，补齐系统消息、用户会话、消息明细三张表及索引
  - 新增 `MessageService`、`AppMessageController`、消息实体、Mapper 与模型，接入系统消息、私信、比赛群聊和会话未读数逻辑
  - 新增 `miniapp` 消息 API、聊天路由工具、消息中心页和会话详情页，并在首页加入消息中心入口
  - 在比赛详情页新增“咨询发起人”入口，直接发起与比赛老师的私信会话
  - 复跑后端全量测试、小程序全量测试与 H5 构建、后台测试与构建，确认全部通过
- Files created/modified:
  - `server/src/main/resources/db/migration/V12__message_center_schema.sql`（created）
  - `server/src/main/java/com/campus/competition/modules/message/controller/AppMessageController.java`（created）
  - `server/src/main/java/com/campus/competition/modules/message/service/MessageService.java`（created）
  - `server/src/main/java/com/campus/competition/modules/message/model/SystemMessageSummary.java`（created）
  - `server/src/main/java/com/campus/competition/modules/message/model/ConversationSummary.java`（created）
  - `server/src/main/java/com/campus/competition/modules/message/model/ChatMessageSummary.java`（created）
  - `server/src/main/java/com/campus/competition/modules/message/model/ConsultTeacherCommand.java`（created）
  - `server/src/main/java/com/campus/competition/modules/message/model/SendPrivateMessageCommand.java`（created）
  - `server/src/main/java/com/campus/competition/modules/message/model/SendGroupMessageCommand.java`（created）
  - `server/src/main/java/com/campus/competition/modules/message/model/ConsultConversationSummary.java`（created）
  - `server/src/main/java/com/campus/competition/modules/message/persistence/SystemMessageEntity.java`（created）
  - `server/src/main/java/com/campus/competition/modules/message/persistence/ConversationEntity.java`（created）
  - `server/src/main/java/com/campus/competition/modules/message/persistence/MessageEntity.java`（created）
  - `server/src/main/java/com/campus/competition/modules/message/mapper/SystemMessageMapper.java`（created）
  - `server/src/main/java/com/campus/competition/modules/message/mapper/ConversationMapper.java`（created）
  - `server/src/main/java/com/campus/competition/modules/message/mapper/MessageMapper.java`（created）
  - `server/src/test/java/com/campus/competition/modules/message/MessageCenterApiTest.java`（created）
  - `miniapp/src/api/message.ts`（created）
  - `miniapp/src/utils/message-navigation.ts`（created）
  - `miniapp/src/pages/message/index.vue`（created）
  - `miniapp/src/pages/message/chat/index.vue`（created）
  - `miniapp/src/__tests__/message-api.spec.ts`（created）
  - `miniapp/src/__tests__/message-navigation.spec.ts`（created）
  - `miniapp/src/pages/competition/detail/index.vue`（modified）
  - `miniapp/src/pages/home/index.vue`（modified）
  - `miniapp/src/pages.json`（modified）
  - `miniapp/src/stores/user.ts`（modified）
  - `miniapp/src/utils/home-navigation.ts`（modified）
  - `server/src/test/java/com/campus/competition/modules/checkin/CheckinPersistenceTest.java`（created）
  - `server/src/test/java/com/campus/competition/modules/submission/SubmissionPersistenceTest.java`（created）
  - `server/src/test/java/com/campus/competition/modules/score/ScorePointsPersistenceTest.java`（created）

### Phase 9: 第七批任务执行（前端真实接口接入首批）
- **Status:** complete
- Actions taken:
  - 为 `admin-web` 与 `miniapp` 新增接口层测试，先验证前端会真实请求后端并解包 `ApiResponse.data`
  - 为两个前端新增统一 HTTP 请求封装，支持统一基址、错误处理和 JSON 请求体
  - 将后台用户、校区 API 改为真实请求；新增评审 API，并把评审工作台切到真实接口
  - 将小程序比赛列表、比赛结果、积分页切到真实接口，并补齐 `loading / empty / error` 状态
  - 串行执行后台与小程序测试、构建，确认首批前端联调改造全部通过
- Files created/modified:
  - `admin-web/vite.config.ts`（modified）
  - `admin-web/src/api/http.ts`（created）
  - `admin-web/src/api/review.ts`（created）
  - `admin-web/src/api/auth.ts`（modified）
  - `admin-web/src/api/users.ts`（modified）
  - `admin-web/src/api/campuses.ts`（modified）
  - `admin-web/src/views/review/ReviewWorkbenchPage.vue`（modified）
  - `admin-web/src/__tests__/api-clients.spec.ts`（created）
  - `miniapp/vite.config.ts`（modified）
  - `miniapp/src/api/http.ts`（created）
  - `miniapp/src/api/competition.ts`（created）
  - `miniapp/src/api/result.ts`（created）
  - `miniapp/src/stores/user.ts`（modified）
  - `miniapp/src/pages/competition/list/index.vue`（modified）
  - `miniapp/src/pages/competition/result/index.vue`（modified）
  - `miniapp/src/pages/points/index.vue`（modified）
  - `miniapp/src/__tests__/api-clients.spec.ts`（created）

### Phase 10: 第八批任务执行（表单型主链路真实接入）
- **Status:** complete
- Actions taken:
  - 为后台比赛发布页和小程序报名/签到/作品上传流程新增 API 与表单辅助逻辑测试，先验证请求路径、载荷与路由参数解析
  - 新增后台比赛发布 API 模块与表单工具，并将后台比赛发布页切到真实提交与校验流程
  - 新增小程序报名、签到、作品上传 API 模块与路由参数工具，并将三个页面切到真实提交与状态回显
  - 在签到页接入真实签到记录查询，在作品上传页接入真实版本记录查询
  - 串行执行前端全量测试与构建，确认表单型主链路接入后整体仍可正常运行
- Files created/modified:
  - `admin-web/src/api/competition.ts`（created）
  - `admin-web/src/utils/competition-form.ts`（created）
  - `admin-web/src/views/competition/CompetitionEditorPage.vue`（modified）
  - `admin-web/src/__tests__/competition-form.spec.ts`（created）
  - `miniapp/src/api/registration.ts`（created）
  - `miniapp/src/api/checkin.ts`（created）
  - `miniapp/src/api/submission.ts`（created）
  - `miniapp/src/utils/competition-workflow.ts`（created）
  - `miniapp/src/pages/competition/register/index.vue`（modified）
  - `miniapp/src/pages/competition/checkin/index.vue`（modified）
  - `miniapp/src/pages/competition/submission/index.vue`（modified）
  - `miniapp/src/__tests__/competition-workflow.spec.ts`（created）

### Phase 11: 第九批任务执行（详情链路与跳转联动）
- **Status:** complete
- Actions taken:
  - 为小程序补充比赛详情接口、状态格式化和跳转路由测试，先验证详情请求路径与页面路由构造
  - 新增比赛导航工具，统一比赛状态文案、时间窗口格式化和页面跳转路径拼装
  - 将比赛列表页改为点击进入详情页，并将详情页切到真实接口展示
  - 在比赛详情页补齐报名、签到、作品上传、结果查看四个操作入口
  - 串行执行小程序全量测试与 H5 构建，确认详情链路接入后整体通过
- Files created/modified:
  - `miniapp/src/api/competition.ts`（modified）
  - `miniapp/src/utils/competition-navigation.ts`（created）
  - `miniapp/src/pages/competition/list/index.vue`（modified）
  - `miniapp/src/pages/competition/detail/index.vue`（modified）
  - `miniapp/src/__tests__/competition-navigation.spec.ts`（created）

### Phase 12: 第十批任务执行（闭环入口补齐）
- **Status:** complete
- Actions taken:
  - 为后台结果发布和老师端发布比赛补充 API 与导航测试，先验证发布路径、提交载荷和首页菜单路由
  - 在后台评审工作台新增结果发布表单，支持录入学生编号、成绩、名次、奖项和积分，并在发布后刷新工作台数据
  - 为小程序比赛 API 增加老师端发布能力，并将老师端发布页切到真实提交与校验流程
  - 为小程序首页补齐菜单路由，让老师端发布页和学生端关键入口都可从首页进入
  - 执行后台全量测试/构建和小程序串行全量测试/H5 构建，确认闭环入口补齐后整体通过
- Files created/modified:
  - `admin-web/src/api/review.ts`（modified）
  - `admin-web/src/utils/score-publish-form.ts`（created）
  - `admin-web/src/views/review/ReviewWorkbenchPage.vue`（modified）
  - `admin-web/src/__tests__/score-publish.spec.ts`（created）
  - `miniapp/src/api/competition.ts`（modified）
  - `miniapp/src/utils/home-navigation.ts`（created）
  - `miniapp/src/pages/home/index.vue`（modified）
  - `miniapp/src/pages/teacher/competition-editor/index.vue`（modified）
  - `miniapp/src/__tests__/teacher-navigation.spec.ts`（created）

### Phase 13: 第十一批任务执行（真实登录与演示账号）
- **Status:** complete
- Actions taken:
  - 为后端补充演示账号初始化测试，先验证 `demo-data` profile 下老师和学生账号可直接登录
  - 为小程序补充登录 API 与 store 登录态测试，先验证登录请求路径和登录结果写入状态
  - 新增开发环境/演示环境账号初始化器，为老师和学生预置最小演示账号
  - 新增小程序登录 API、token 登录态和登录页表单，支持老师/学生演示账号一键带入并真实登录
  - 执行后端全量测试、小程序串行全量测试和 H5 构建，确认真实登录接入后整体通过
- Files created/modified:
  - `server/src/main/java/com/campus/competition/modules/auth/config/DemoAccountInitializer.java`（created）
  - `server/src/test/java/com/campus/competition/modules/auth/DevDemoDataInitializerTest.java`（created）
  - `miniapp/src/api/auth.ts`（created）
  - `miniapp/src/stores/user.ts`（modified）
  - `miniapp/src/pages/login/index.vue`（modified）
  - `miniapp/src/__tests__/auth-login.spec.ts`（created）

### Phase 14: 第十二批任务执行（登录态持久化与接口闭环回归）
- **Status:** complete
- Actions taken:
  - 为小程序补充登录态持久化、登录回跳和携带 token 请求的红灯测试，先卡住“登录后可恢复”的关键体验
  - 新增小程序会话存储与登录导航工具，并把启动恢复、首页退出登录、受保护页基础守卫接入主链路页面
  - 为后端新增 `MockMvc` 闭环测试，覆盖老师登录发布比赛、学生报名签到上传作品、后台发布结果、学生查看积分与结果
  - 在闭环回归测试里把比赛时间窗口改为相对当前时间的合法场景，避免绝对日期随时间推进失效
  - 执行后端全量测试、小程序全量测试与 H5 构建，确认登录持久化和接口闭环回归接入后整体通过
- Files created/modified:
  - `miniapp/src/utils/auth-session.ts`（created）
  - `miniapp/src/utils/auth-navigation.ts`（created）
  - `miniapp/src/stores/user.ts`（modified）
  - `miniapp/src/main.ts`（modified）
  - `miniapp/src/api/http.ts`（modified）
  - `miniapp/src/pages/login/index.vue`（modified）
  - `miniapp/src/pages/home/index.vue`（modified）
  - `miniapp/src/pages/competition/detail/index.vue`（modified）
  - `miniapp/src/pages/competition/register/index.vue`（modified）
  - `miniapp/src/pages/competition/checkin/index.vue`（modified）
  - `miniapp/src/pages/competition/submission/index.vue`（modified）
  - `miniapp/src/pages/competition/result/index.vue`（modified）
  - `miniapp/src/pages/points/index.vue`（modified）
  - `miniapp/src/pages/teacher/competition-editor/index.vue`（modified）
  - `miniapp/src/__tests__/auth-session.spec.ts`（created）
  - `server/src/test/java/com/campus/competition/modules/competition/CompetitionFlowApiTest.java`（created）

### Phase 15: 第十三批任务执行（账号与个人中心基础版）
- **Status:** complete
- Actions taken:
  - 基于“去掉第三方能力”的新范围，新增剩余模块设计文档和实施计划，明确后续只做本地可运行能力
  - 为后端新增 `ProfileApiTest`，先验证资料查询、资料更新、密码修改、意见反馈、账号注销五个行为
  - 新增 `V7__user_profile_settings.sql`，扩展 `sys_user` 的头像、组织、提醒、隐私字段，并新增 `sys_feedback` 表
  - 新增 `ProfileService`、`AppProfileController`、资料命令模型、反馈实体和 Mapper，补齐个人中心基础后端实现
  - 为小程序新增 `profile-api.spec.ts`，先验证资料 API 路径、菜单路由和请求载荷
  - 新增 `miniapp` 个人中心 API、页面、首页入口和菜单标签，支持资料编辑、修改密码、提交反馈、账号注销
  - 在全量后端回归中发现 `ProfileApiTest` 依赖固定主键不稳定，改为按学号动态查找学生 ID 后恢复通过
  - 执行后端全量测试、小程序全量测试和小程序 H5 构建，确认个人中心批次接入后整体通过
- Files created/modified:
  - `docs/plans/2026-03-17-campus-competition-remaining-modules-design.md`（created）
  - `docs/plans/2026-03-17-campus-competition-remaining-modules-implementation-plan.md`（created）
  - `server/src/main/resources/db/migration/V7__user_profile_settings.sql`（created）
  - `server/src/main/java/com/campus/competition/modules/auth/persistence/UserEntity.java`（modified）
  - `server/src/main/java/com/campus/competition/modules/auth/service/AuthService.java`（modified）
  - `server/src/main/java/com/campus/competition/modules/profile/controller/AppProfileController.java`（created）
  - `server/src/main/java/com/campus/competition/modules/profile/service/ProfileService.java`（created）
  - `server/src/main/java/com/campus/competition/modules/profile/model/ProfileSummary.java`（created）
  - `server/src/main/java/com/campus/competition/modules/profile/model/UpdateProfileCommand.java`（created）
  - `server/src/main/java/com/campus/competition/modules/profile/model/ChangePasswordCommand.java`（created）
  - `server/src/main/java/com/campus/competition/modules/profile/model/FeedbackCommand.java`（created）
  - `server/src/main/java/com/campus/competition/modules/profile/model/CancelAccountCommand.java`（created）
  - `server/src/main/java/com/campus/competition/modules/profile/persistence/FeedbackEntity.java`（created）
  - `server/src/main/java/com/campus/competition/modules/profile/mapper/FeedbackMapper.java`（created）
  - `server/src/test/java/com/campus/competition/modules/profile/ProfileApiTest.java`（created）
  - `miniapp/src/api/profile.ts`（created）
  - `miniapp/src/pages/profile/index.vue`（created）
  - `miniapp/src/pages/home/index.vue`（modified）
  - `miniapp/src/pages.json`（modified）
  - `miniapp/src/stores/user.ts`（modified）
  - `miniapp/src/utils/home-navigation.ts`（modified）
  - `miniapp/src/__tests__/profile-api.spec.ts`（created）
  - `miniapp/src/__tests__/role-menu.spec.ts`（modified）
  - `miniapp/src/__tests__/teacher-navigation.spec.ts`（modified）

### Phase 16: 第十四批任务执行（比赛生命周期增强版）
- **Status:** complete
- Actions taken:
  - 为后端新增 `CompetitionManageApiTest`，先验证草稿保存、比赛编辑、推荐置顶、下架和公开列表过滤
  - 新增 `V8__competition_manage_enhancement.sql`，为比赛表扩展推荐、置顶和更新时间字段
  - 新增 `SaveCompetitionDraftCommand`、`UpdateCompetitionCommand`、`CompetitionDraftSummary`、`CompetitionFeatureCommand`
  - 扩展 `CompetitionService` 和 `AdminCompetitionController`，支持草稿、编辑、推荐置顶、下架以及管理列表查询
  - 为后台新增 `competition-manage.spec.ts`，先验证管理 API 模块不存在时的红灯
  - 新增 `admin-web` 比赛管理 API 模块，并把比赛页升级为“发布 + 草稿 + 编辑 + 推荐置顶 + 下架 + 管理列表”一体页
  - 扩展老师端小程序比赛页，支持加载本人比赛、编辑、推荐置顶和下架
  - 执行后端全量测试、后台全量测试、小程序全量测试，以及后台和小程序构建，确认生命周期增强版整体通过
- Files created/modified:
  - `server/src/main/resources/db/migration/V8__competition_manage_enhancement.sql`（created）
  - `server/src/main/java/com/campus/competition/modules/competition/model/SaveCompetitionDraftCommand.java`（created）
  - `server/src/main/java/com/campus/competition/modules/competition/model/UpdateCompetitionCommand.java`（created）
  - `server/src/main/java/com/campus/competition/modules/competition/model/CompetitionDraftSummary.java`（created）
  - `server/src/main/java/com/campus/competition/modules/competition/model/CompetitionFeatureCommand.java`（created）
  - `server/src/main/java/com/campus/competition/modules/competition/model/CompetitionSummary.java`（modified）
  - `server/src/main/java/com/campus/competition/modules/competition/model/CompetitionDetail.java`（modified）
  - `server/src/main/java/com/campus/competition/modules/competition/persistence/CompetitionEntity.java`（modified）
  - `server/src/main/java/com/campus/competition/modules/competition/service/CompetitionService.java`（modified）
  - `server/src/main/java/com/campus/competition/modules/competition/controller/AdminCompetitionController.java`（modified）
  - `server/src/test/java/com/campus/competition/modules/competition/CompetitionManageApiTest.java`（created）
  - `admin-web/src/api/competition-manage.ts`（created）
  - `admin-web/src/views/competition/CompetitionEditorPage.vue`（modified）
  - `admin-web/src/__tests__/competition-manage.spec.ts`（created）
  - `miniapp/src/api/competition.ts`（modified）
  - `miniapp/src/pages/teacher/competition-editor/index.vue`（modified）

### Phase 17: 第十五批任务执行（报名与参赛管理增强版）
- **Status:** complete
- Actions taken:
  - 为后端新增 `RegistrationManageApiTest`，先验证报名取消、后台驳回、手动加人和到场/缺席标记
  - 新增 `V9__registration_manage_enhancement.sql`，为报名表扩展审核状态、到场状态、备注和更新时间字段
  - 新增 `CancelRegistrationCommand`、`ManualRegistrationCommand`、`RejectRegistrationCommand`、`RegistrationAttendanceCommand`
  - 扩展 `RegistrationService`、`AppRegistrationController` 和 `AdminRegistrationController`，补齐学生取消报名和管理员视角管理接口
  - 为后台新增 `registration-manage.spec.ts`，并新增参赛管理 API 模块与 `RegistrationManagePage.vue`
  - 扩展学生端报名页和路由测试，补齐取消报名入口与状态回显
  - 执行 `RegistrationManageApiTest`、后台参赛管理测试和小程序参赛管理测试，确认增强链路通过
- Files created/modified:
  - `server/src/main/resources/db/migration/V9__registration_manage_enhancement.sql`（created）
  - `server/src/main/java/com/campus/competition/modules/registration/model/CancelRegistrationCommand.java`（created）
  - `server/src/main/java/com/campus/competition/modules/registration/model/ManualRegistrationCommand.java`（created）
  - `server/src/main/java/com/campus/competition/modules/registration/model/RejectRegistrationCommand.java`（created）
  - `server/src/main/java/com/campus/competition/modules/registration/model/RegistrationAttendanceCommand.java`（created）
  - `server/src/main/java/com/campus/competition/modules/registration/model/RegistrationSummary.java`（modified）
  - `server/src/main/java/com/campus/competition/modules/registration/service/RegistrationService.java`（modified）
  - `server/src/main/java/com/campus/competition/modules/registration/controller/AppRegistrationController.java`（modified）
  - `server/src/main/java/com/campus/competition/modules/registration/controller/AdminRegistrationController.java`（created）
  - `server/src/test/java/com/campus/competition/modules/registration/RegistrationManageApiTest.java`（created）
  - `admin-web/src/api/registration-manage.ts`（created）
  - `admin-web/src/views/competition/RegistrationManagePage.vue`（created）
  - `admin-web/src/__tests__/registration-manage.spec.ts`（created）
  - `admin-web/src/router/index.ts`（modified）
  - `miniapp/src/pages/competition/register/index.vue`（modified）
  - `miniapp/src/__tests__/registration-manage.spec.ts`（created）

### Phase 18: 第十六批任务执行（评审增强与电子奖状）
- **Status:** complete
- Actions taken:
  - 为后端新增 `ReviewCertificateApiTest`，先验证评审提交、结果发布携带评语，以及学生端结果返回电子奖状信息
  - 新增 `V10__review_certificate_enhancement.sql`，为评审任务和结果表扩展学生维度、评审意见、建议分数、奖状编号和奖状标题字段
  - 新增 `SubmitReviewCommand`、`ReviewTaskEntity`、`ReviewTaskMapper`、`ReviewService`，并扩展 `AdminReviewController`
  - 扩展 `PublishResultCommand`、`ScoreSummary`、`ScoreEntity`、`ScoreService`，在结果发布时自动生成电子奖状编号和标题
  - 为后台扩展 `review.ts`、`api-clients.spec.ts`、`score-publish.spec.ts`，并重写 `ReviewWorkbenchPage.vue`
  - 为小程序扩展结果 API 和结果页展示，支持查看评审老师、评审意见和电子奖状编号
  - 执行评审增强针对性测试、三端全量测试和前端构建，确认本轮增强与既有主链路兼容
- Files created/modified:
  - `server/src/main/resources/db/migration/V10__review_certificate_enhancement.sql`（created）
  - `server/src/main/java/com/campus/competition/modules/review/model/SubmitReviewCommand.java`（created）
  - `server/src/main/java/com/campus/competition/modules/review/model/ReviewTaskSummary.java`（modified）
  - `server/src/main/java/com/campus/competition/modules/review/persistence/ReviewTaskEntity.java`（created）
  - `server/src/main/java/com/campus/competition/modules/review/mapper/ReviewTaskMapper.java`（created）
  - `server/src/main/java/com/campus/competition/modules/review/service/ReviewService.java`（created）
  - `server/src/main/java/com/campus/competition/modules/review/controller/AdminReviewController.java`（modified）
  - `server/src/main/java/com/campus/competition/modules/score/model/PublishResultCommand.java`（modified）
  - `server/src/main/java/com/campus/competition/modules/score/model/ScoreSummary.java`（modified）
  - `server/src/main/java/com/campus/competition/modules/score/persistence/ScoreEntity.java`（modified）
  - `server/src/main/java/com/campus/competition/modules/score/service/ScoreService.java`（modified）
  - `server/src/test/java/com/campus/competition/modules/review/ReviewCertificateApiTest.java`（created）
  - `server/src/test/java/com/campus/competition/modules/score/ScorePublishTest.java`（modified）
  - `server/src/test/java/com/campus/competition/modules/score/ScorePointsPersistenceTest.java`（modified）
  - `admin-web/src/api/review.ts`（modified）
  - `admin-web/src/views/review/ReviewWorkbenchPage.vue`（modified）
  - `admin-web/src/__tests__/api-clients.spec.ts`（modified）
  - `admin-web/src/__tests__/score-publish.spec.ts`（modified）
  - `miniapp/src/api/result.ts`（modified）
  - `miniapp/src/pages/competition/result/index.vue`（modified）
  - `miniapp/src/__tests__/api-clients.spec.ts`（modified）

### Phase 19: 第十七批任务执行（每日签到、分享任务与个人比赛汇总）
- **Status:** complete
- Actions taken:
  - 为后端新增 `DailyTaskOverviewApiTest`，先验证每日签到、比赛分享积分、防重复领取和个人比赛汇总查询
  - 新增 `V11__daily_task_overview.sql`，为积分流水、报名和作品表补齐日常任务查询所需索引
  - 新增 `DailyCheckinCommand`、`ShareCompetitionCommand`、`DailyTaskSummary`、`PersonalCompetitionOverview` 和 `AppPointsTaskController`
  - 扩展 `PointsService`，支持每日签到、每日分享限制、今日任务积分统计；扩展 `RegistrationService` 与 `SubmissionService` 的按用户查询能力
  - 为小程序新增 `daily-task` API 与工具模块，并新增 `daily-task-api.spec.ts`、`daily-task.spec.ts`
  - 将首页、个人中心、比赛详情页、积分页接到每日任务和个人比赛汇总接口，补齐签到与分享积分入口
  - 执行后端全量测试、后台全量测试、小程序全量测试，以及后台和小程序构建，确认本轮增强与既有链路兼容
- Files created/modified:
  - `server/src/main/resources/db/migration/V11__daily_task_overview.sql`（created）
  - `server/src/main/java/com/campus/competition/modules/points/model/DailyCheckinCommand.java`（created）
  - `server/src/main/java/com/campus/competition/modules/points/model/ShareCompetitionCommand.java`（created）
  - `server/src/main/java/com/campus/competition/modules/points/model/DailyTaskSummary.java`（created）
  - `server/src/main/java/com/campus/competition/modules/points/model/PersonalCompetitionOverview.java`（created）
  - `server/src/main/java/com/campus/competition/modules/points/controller/AppPointsTaskController.java`（created）
  - `server/src/main/java/com/campus/competition/modules/points/service/PointsService.java`（modified）
  - `server/src/main/java/com/campus/competition/modules/registration/service/RegistrationService.java`（modified）
  - `server/src/main/java/com/campus/competition/modules/submission/service/SubmissionService.java`（modified）
  - `server/src/test/java/com/campus/competition/modules/points/DailyTaskOverviewApiTest.java`（created）
  - `miniapp/src/api/daily-task.ts`（created）
  - `miniapp/src/utils/daily-task.ts`（created）
  - `miniapp/src/pages/home/index.vue`（modified）
  - `miniapp/src/pages/profile/index.vue`（modified）
  - `miniapp/src/pages/competition/detail/index.vue`（modified）
  - `miniapp/src/pages/points/index.vue`（modified）
  - `miniapp/src/__tests__/daily-task-api.spec.ts`（created）
  - `miniapp/src/__tests__/daily-task.spec.ts`（created）

### Phase 20: 第十八批任务执行（后台角色权限与用户治理首批闭环）
- **Status:** complete
- Actions taken:
  - 为后端新增 `AdminUserGovernanceApiTest`，先验证角色列表、创建角色、编辑角色、冻结/解冻账号、重置密码和改角色
  - 新增 `V13__role_governance_schema.sql`、`RoleEntity`、`RoleMapper`、`RoleService`、`AdminRoleController`，落地角色管理最小闭环
  - 新增 `AdminUserService` 与 `AdminUserServiceImpl`，将用户列表、冻结/解冻、重置密码、改角色统一收敛到治理服务
  - 修正 `MybatisPlusConfig` 仅扫描带 `@Mapper` 注解的接口，并为 `FeedbackMapper` 显式补上 `@Mapper`，排除服务接口被误注册的问题
  - 为后台新增 `roles.ts` 与 `role-governance.ts`，扩展 `users.ts`，补齐角色表单和密码规则的前端工具层
  - 重写 `UserListPage.vue` 与 `RoleListPage.vue`，将原静态页升级为“真实列表 + 真实操作面板”的后台治理页
  - 执行后端红绿灯测试、后台定向测试、后台全量测试、后台构建和后端全量测试，确认本轮治理增强与既有能力兼容
- Files created/modified:
  - `server/src/main/resources/db/migration/V13__role_governance_schema.sql`（created）
  - `server/src/main/java/com/campus/competition/modules/common/config/MybatisPlusConfig.java`（modified）
  - `server/src/main/java/com/campus/competition/modules/profile/mapper/FeedbackMapper.java`（modified）
  - `server/src/main/java/com/campus/competition/modules/role/persistence/RoleEntity.java`（created）
  - `server/src/main/java/com/campus/competition/modules/role/mapper/RoleMapper.java`（created）
  - `server/src/main/java/com/campus/competition/modules/role/model/RoleSummary.java`（created）
  - `server/src/main/java/com/campus/competition/modules/role/model/SaveRoleCommand.java`（created）
  - `server/src/main/java/com/campus/competition/modules/role/service/RoleService.java`（created）
  - `server/src/main/java/com/campus/competition/modules/role/controller/AdminRoleController.java`（created）
  - `server/src/main/java/com/campus/competition/modules/user/model/FreezeUserCommand.java`（created）
  - `server/src/main/java/com/campus/competition/modules/user/model/ResetPasswordCommand.java`（created）
  - `server/src/main/java/com/campus/competition/modules/user/model/AssignUserRoleCommand.java`（created）
  - `server/src/main/java/com/campus/competition/modules/user/service/AdminUserService.java`（created）
  - `server/src/main/java/com/campus/competition/modules/user/service/AdminUserServiceImpl.java`（created）
  - `server/src/main/java/com/campus/competition/modules/user/controller/AdminUserController.java`（modified）
  - `server/src/test/java/com/campus/competition/modules/user/AdminUserGovernanceApiTest.java`（created）
  - `admin-web/src/api/users.ts`（modified）
  - `admin-web/src/api/roles.ts`（created）
  - `admin-web/src/utils/role-governance.ts`（created）
  - `admin-web/src/views/system/users/UserListPage.vue`（modified）
  - `admin-web/src/views/system/roles/RoleListPage.vue`（modified）
  - `admin-web/src/__tests__/api-clients.spec.ts`（modified）
  - `admin-web/src/__tests__/role-governance.spec.ts`（created）

## Test Results
| Test | Input | Expected | Actual | Status |
|------|-------|----------|--------|--------|
| 需求文档提取 | `pandoc -f docx -t markdown_strict` | 输出可读文本 | 成功提取主要需求内容 | ✓ |
| 结构校验脚本（红灯） | `powershell -ExecutionPolicy Bypass -File .\scripts\verify-structure.ps1` | 因目录缺失失败 | 失败并提示缺少 `admin-web, miniapp, server, infra` | ✓ |
| 结构校验脚本（绿灯） | `powershell -ExecutionPolicy Bypass -File .\scripts\verify-structure.ps1` | 工作区结构通过 | 输出 `Workspace structure ok` | ✓ |
| 后端健康检查（红灯） | `mvn -f server/pom.xml -Dtest=HealthControllerTest test` | 因缺少启动类失败 | 失败并提示缺少 `@SpringBootConfiguration` | ✓ |
| 后端健康检查（绿灯） | `mvn -f server/pom.xml -Dtest=HealthControllerTest test` | 测试通过 | 通过 | ✓ |
| 通用响应体（红灯） | `mvn -f server/pom.xml -Dtest=ApiResponseTest test` | 因缺少 `ApiResponse` 失败 | 编译失败 | ✓ |
| 后端全量测试（绿灯） | `mvn -f server/pom.xml test` | 所有当前测试通过 | 2 个测试全部通过 | ✓ |
| 后台路由测试（红灯） | `pnpm --dir admin-web test -- --run` | 因缺少 `router` 失败 | 失败并提示无法解析 `../router` | ✓ |
| 后台路由测试（绿灯） | `pnpm --dir admin-web test -- --run` | 测试通过 | 1 个测试通过 | ✓ |
| 后台生产构建 | `pnpm --dir admin-web build` | 后台骨架可构建 | 构建通过 | ✓ |
| 小程序菜单测试（红灯） | `pnpm test -- --run` | 因缺少 `stores/user` 失败 | 初始失败，后校正到缺少 store | ✓ |
| 小程序菜单测试（绿灯） | `pnpm test -- --run` | 测试通过 | 1 个测试通过 | ✓ |
| 小程序 H5 构建 | `pnpm build:h5` | H5 骨架可构建 | 构建通过 | ✓ |
| 账号服务测试（红灯） | `mvn -f server/pom.xml -Dtest=AuthServiceTest test` | 因缺少 `AuthService` 与模型类失败 | 编译失败 | ✓ |
| 账号服务测试（绿灯） | `mvn -f server/pom.xml -Dtest=AuthServiceTest test` | 测试通过 | 1 个测试通过 | ✓ |
| 后端全量测试（第二批） | `mvn -f server/pom.xml test` | 所有当前测试通过 | 3 个测试全部通过 | ✓ |
| 签到/作品测试（红灯） | `mvn -f server/pom.xml "-Dtest=CheckinServiceTest,SubmissionServiceTest" test` | 因缺少签到和提交服务失败 | 编译失败，提示缺少 `checkin/submission` 相关类 | ✓ |
| 签到/作品测试（绿灯） | `mvn -f server/pom.xml "-Dtest=CheckinServiceTest,SubmissionServiceTest" test` | 签到与作品流程通过 | 2 个测试全部通过 | ✓ |
| 结果发布积分测试（红灯） | `mvn -f server/pom.xml -Dtest=ScorePublishTest test` | 因缺少积分与评分服务失败 | 编译失败，提示缺少 `points/score` 相关类 | ✓ |
| 结果发布积分测试（绿灯） | `mvn -f server/pom.xml -Dtest=ScorePublishTest test` | 结果发布与积分入账通过 | 1 个测试通过 | ✓ |
| 后台日志接口测试（红灯） | `mvn -f server/pom.xml -Dtest=AdminLogControllerTest test` | 因缺少日志控制器失败 | 接口返回 `code=500` | ✓ |
| 后台日志接口测试（绿灯） | `mvn -f server/pom.xml -Dtest=AdminLogControllerTest test` | 日志接口返回成功 | 1 个测试通过 | ✓ |
| 后端全量测试（第四批） | `mvn -f server/pom.xml test` | 所有当前测试通过 | 9 个测试全部通过 | ✓ |
| 后台测试（第四批） | `pnpm --dir admin-web test -- --run` | 后台测试通过 | 2 个测试通过 | ✓ |
| 后台构建（第四批） | `pnpm --dir admin-web build` | 后台新增页面可构建 | 构建通过 | ✓ |
| 小程序测试（第四批） | `pnpm --dir miniapp test -- --run` | 小程序测试通过 | 1 个测试通过 | ✓ |
| 小程序 H5 构建（第四批） | `pnpm --dir miniapp build:h5` | 小程序新增页面可构建 | 构建通过 | ✓ |
| 持久化测试（红灯） | `mvn -f server/pom.xml "-Dtest=AuthPersistenceTest,CompetitionRegistrationPersistenceTest" test` | 因缺少 Mapper/实体/事务支持失败 | 编译失败，提示缺少 `mapper/persistence/transactional` 相关类 | ✓ |
| 持久化测试（绿灯） | `mvn -f server/pom.xml "-Dtest=AuthPersistenceTest,CompetitionRegistrationPersistenceTest" test` | 账号、比赛、报名可真实落库 | 2 个测试全部通过 | ✓ |
| 后端全量测试（第五批） | `mvn -f server/pom.xml test` | 原有能力与持久化升级兼容通过 | 11 个测试全部通过 | ✓ |
| 剩余核心域持久化测试（红灯） | `mvn -f server/pom.xml "-Dtest=CheckinPersistenceTest,SubmissionPersistenceTest,ScorePointsPersistenceTest" test` | 因缺少实体、Mapper 和数据库分支失败 | 编译失败，提示缺少 `checkin/submission/score/points` 持久化相关类 | ✓ |
| 剩余核心域持久化测试（绿灯） | `mvn -f server/pom.xml "-Dtest=CheckinPersistenceTest,SubmissionPersistenceTest,ScorePointsPersistenceTest" test` | 签到、作品、结果、积分可真实落库 | 3 个测试全部通过 | ✓ |
| 后端全量测试（第六批） | `mvn -f server/pom.xml test` | 剩余核心域迁移后整体通过 | 14 个测试全部通过 | ✓ |
| 后台 API 客户端测试（红灯） | `pnpm --dir admin-web test -- --run src/__tests__/api-clients.spec.ts` | 因缺少真实请求模块失败 | 失败并提示无法解析 `@/api/review` | ✓ |
| 小程序 API 客户端测试（红灯） | `pnpm --dir miniapp test -- --run src/__tests__/api-clients.spec.ts` | 因缺少真实请求模块失败 | 失败并提示缺少 `src/api/competition` | ✓ |
| 后台 API 客户端测试（绿灯） | `pnpm --dir admin-web test -- --run src/__tests__/api-clients.spec.ts` | 后台客户端会请求真实接口 | 3 个测试全部通过 | ✓ |
| 小程序 API 客户端测试（绿灯） | `pnpm --dir miniapp test -- --run src/__tests__/api-clients.spec.ts` | 小程序客户端会请求真实接口 | 2 个测试全部通过 | ✓ |
| 后台全量测试（第七批） | `pnpm --dir admin-web test -- --run` | 后台改造后原有测试保持通过 | 5 个测试全部通过 | ✓ |
| 小程序全量测试（第七批） | `pnpm --dir miniapp test -- --run` | 小程序改造后原有测试保持通过 | 3 个测试全部通过 | ✓ |
| 后台构建（第七批） | `pnpm --dir admin-web build` | 后台真实接口改造后可正常构建 | 构建通过 | ✓ |
| 小程序 H5 构建（第七批） | `pnpm --dir miniapp build:h5` | 小程序真实接口改造后可正常构建 | 构建通过 | ✓ |
| 比赛发布表单测试（红灯） | `pnpm --dir admin-web test -- --run src/__tests__/competition-form.spec.ts` | 因缺少比赛发布 API 与表单工具失败 | 失败并提示无法解析 `@/api/competition` | ✓ |
| 比赛工作流测试（红灯） | `pnpm --dir miniapp test -- --run src/__tests__/competition-workflow.spec.ts` | 因缺少报名/签到/作品 API 与路由工具失败 | 失败并提示缺少 `src/api/registration` | ✓ |
| 比赛发布表单测试（绿灯） | `pnpm --dir admin-web test -- --run src/__tests__/competition-form.spec.ts` | 后台发布页可构造真实提交载荷 | 2 个测试全部通过 | ✓ |
| 比赛工作流测试（绿灯） | `pnpm --dir miniapp test -- --run src/__tests__/competition-workflow.spec.ts` | 小程序主流程可调用报名/签到/作品接口 | 4 个测试全部通过 | ✓ |
| 后台全量测试（第八批） | `pnpm --dir admin-web test -- --run` | 比赛发布页改造后整体通过 | 7 个测试全部通过 | ✓ |
| 小程序全量测试（第八批） | `pnpm --dir miniapp test -- --run` | 报名/签到/作品页改造后整体通过 | 7 个测试全部通过 | ✓ |
| 后台构建（第八批） | `pnpm --dir admin-web build` | 比赛发布页改造后可正常构建 | 构建通过 | ✓ |
| 小程序 H5 构建（第八批） | `pnpm --dir miniapp build:h5` | 表单型主链路接入后可正常构建 | 构建通过 | ✓ |
| 比赛详情导航测试（红灯） | `pnpm --dir miniapp test -- --run src/__tests__/competition-navigation.spec.ts` | 因缺少详情接口与导航工具失败 | 失败并提示缺少 `competition-navigation` 模块 | ✓ |
| 比赛详情导航测试（绿灯） | `pnpm --dir miniapp test -- --run src/__tests__/competition-navigation.spec.ts` | 详情接口、状态格式化与路由拼装可用 | 3 个测试全部通过 | ✓ |
| 小程序全量测试（第九批） | `pnpm --dir miniapp test -- --run` | 详情链路接入后整体通过 | 10 个测试全部通过 | ✓ |
| 小程序 H5 构建（第九批） | `pnpm --dir miniapp build:h5` | 列表/详情/动作页联动后可正常构建 | 构建通过 | ✓ |
| 结果发布测试（红灯） | `pnpm --dir admin-web test -- --run src/__tests__/score-publish.spec.ts` | 因缺少结果发布表单工具失败 | 失败并提示缺少 `score-publish-form` 模块 | ✓ |
| 老师发布与首页导航测试（红灯） | `pnpm --dir miniapp test -- --run src/__tests__/teacher-navigation.spec.ts` | 因缺少首页路由工具失败 | 失败并提示缺少 `home-navigation` 模块 | ✓ |
| 结果发布测试（绿灯） | `pnpm --dir admin-web test -- --run src/__tests__/score-publish.spec.ts` | 后台结果发布入口可真实提交 | 2 个测试全部通过 | ✓ |
| 老师发布与首页导航测试（绿灯） | `pnpm --dir miniapp test -- --run src/__tests__/teacher-navigation.spec.ts` | 老师端发布页和首页快捷入口可用 | 2 个测试全部通过 | ✓ |
| 后台全量测试（第十批） | `pnpm --dir admin-web test -- --run` | 后台补齐结果发布入口后整体通过 | 9 个测试全部通过 | ✓ |
| 后台构建（第十批） | `pnpm --dir admin-web build` | 后台结果发布入口补齐后可正常构建 | 构建通过 | ✓ |
| 小程序全量测试（第十批） | `pnpm --dir miniapp test -- --run` | 老师端发布页与首页导航补齐后整体通过 | 12 个测试全部通过 | ✓ |
| 小程序 H5 构建（第十批） | `pnpm --dir miniapp build:h5` | 闭环入口补齐后可正常构建 | 构建通过 | ✓ |
| 演示账号初始化测试（红灯） | `mvn -f server/pom.xml -Dtest=DevDemoDataInitializerTest test` | 因缺少演示账号初始化器失败 | 失败并提示登录时报 `账号不存在` | ✓ |
| 小程序登录测试（红灯） | `pnpm --dir miniapp test -- --run src/__tests__/auth-login.spec.ts` | 因缺少登录 API 与登录态能力失败 | 失败并提示缺少 `src/api/auth` | ✓ |
| 演示账号初始化测试（绿灯） | `mvn -f server/pom.xml -Dtest=DevDemoDataInitializerTest test` | `demo-data` profile 下老师和学生账号可直接登录 | 1 个测试通过 | ✓ |
| 小程序登录测试（绿灯） | `pnpm --dir miniapp test -- --run src/__tests__/auth-login.spec.ts` | 小程序登录请求与登录态更新可用 | 2 个测试全部通过 | ✓ |
| 后端全量测试（第十一批） | `mvn -f server/pom.xml test` | 演示账号初始化接入后整体通过 | 15 个测试全部通过 | ✓ |
| 小程序全量测试（第十一批） | `pnpm --dir miniapp test -- --run` | 小程序真实登录接入后整体通过 | 14 个测试全部通过 | ✓ |
| 小程序 H5 构建（第十一批） | `pnpm --dir miniapp build:h5` | 登录页接入真实接口后可正常构建 | 构建通过 | ✓ |
| 小程序会话测试（红灯） | `pnpm --dir miniapp test -- --run src/__tests__/auth-session.spec.ts` | 因缺少会话存储与登录导航工具失败 | 失败并提示缺少 `src/utils/auth-navigation` | ✓ |
| 小程序会话测试（绿灯） | `pnpm --dir miniapp test -- --run src/__tests__/auth-session.spec.ts` | 登录态可持久化恢复且请求能带 token | 4 个测试全部通过 | ✓ |
| 比赛闭环接口测试（红灯） | `mvn -f server/pom.xml -Dtest=CompetitionFlowApiTest test` | 因固定日期不合法导致报名失败 | 返回 `code=400`，提示“当前不在报名时间内” | ✓ |
| 比赛闭环接口测试（绿灯） | `mvn -f server/pom.xml -Dtest=CompetitionFlowApiTest test` | 老师发布到学生查看结果的接口闭环通过 | 1 个测试通过 | ✓ |
| 后端全量测试（第十二批） | `mvn -f server/pom.xml test` | 增加闭环回归测试后整体通过 | 16 个测试全部通过 | ✓ |
| 小程序全量测试（第十二批） | `pnpm --dir miniapp test -- --run` | 增加登录持久化后整体通过 | 18 个测试全部通过 | ✓ |
| 小程序 H5 构建（第十二批） | `pnpm --dir miniapp build:h5` | 登录态持久化和页面守卫接入后可正常构建 | 构建通过 | ✓ |
| 个人中心后端测试（红灯） | `mvn -f server/pom.xml -Dtest=ProfileApiTest test` | 因缺少 profile 相关实现失败 | 初次编译失败，提示缺少 `FeedbackMapper` 等类 | ✓ |
| 个人中心后端测试（绿灯） | `mvn -f server/pom.xml -Dtest=ProfileApiTest test` | 资料、密码、反馈、注销接口通过 | 1 个测试通过 | ✓ |
| 个人中心小程序测试（红灯） | `pnpm --dir miniapp test -- --run src/__tests__/profile-api.spec.ts` | 因缺少 `profile` API 模块失败 | 失败并提示无法解析 `@/api/profile` | ✓ |
| 个人中心小程序测试（绿灯） | `pnpm --dir miniapp test -- --run src/__tests__/profile-api.spec.ts src/__tests__/role-menu.spec.ts src/__tests__/teacher-navigation.spec.ts` | 资料接口、菜单和路由全部通过 | 3 个文件共 6 个测试通过 | ✓ |
| 后端全量测试（第十三批） | `mvn -f server/pom.xml test` | 个人中心接入后整体通过 | 17 个测试全部通过 | ✓ |
| 小程序全量测试（第十三批） | `pnpm --dir miniapp test -- --run` | 个人中心接入后整体通过 | 21 个测试全部通过 | ✓ |
| 小程序 H5 构建（第十三批） | `pnpm --dir miniapp build:h5` | 个人中心页面接入后可正常构建 | 构建通过 | ✓ |
| 比赛管理后端测试（红灯） | `mvn -f server/pom.xml -Dtest=CompetitionManageApiTest test` | 因缺少草稿和管理接口失败 | 首次返回 `code=500`，提示不存在 `/api/admin/competitions/draft` | ✓ |
| 比赛管理后端测试（绿灯） | `mvn -f server/pom.xml -Dtest=CompetitionManageApiTest test` | 草稿、编辑、推荐置顶、下架和公开列表过滤通过 | 1 个测试通过 | ✓ |
| 比赛管理后台测试（红灯） | `pnpm --dir admin-web test -- --run src/__tests__/competition-manage.spec.ts` | 因缺少 `competition-manage` API 模块失败 | 失败并提示无法解析 `@/api/competition-manage` | ✓ |
| 比赛管理后台测试（绿灯） | `pnpm --dir admin-web test -- --run src/__tests__/competition-manage.spec.ts` | 管理 API 路径与请求载荷正确 | 2 个测试通过 | ✓ |
| 后端全量测试（第十四批） | `mvn -f server/pom.xml test` | 比赛生命周期增强版接入后整体通过 | 18 个测试全部通过 | ✓ |
| 后台全量测试（第十四批） | `pnpm --dir admin-web test -- --run` | 比赛管理增强接入后整体通过 | 11 个测试全部通过 | ✓ |
| 小程序全量测试（第十四批） | `pnpm --dir miniapp test -- --run` | 老师端比赛管理增强接入后整体通过 | 21 个测试全部通过 | ✓ |
| 后台构建（第十四批） | `pnpm --dir admin-web build` | 比赛管理增强页可正常构建 | 构建通过 | ✓ |
| 小程序 H5 构建（第十四批） | `pnpm --dir miniapp build:h5` | 老师端比赛管理增强页可正常构建 | 构建通过 | ✓ |
| 报名管理后端测试（第十五批） | `mvn -f server/pom.xml -Dtest=RegistrationManageApiTest test` | 取消报名、驳回、手动加人、到场标记通过 | 1 个测试通过 | ✓ |
| 报名管理后台测试（第十五批） | `pnpm --dir admin-web test -- --run src/__tests__/registration-manage.spec.ts` | 参赛管理 API 路径与请求载荷正确 | 1 个文件共 2 个测试通过 | ✓ |
| 报名管理小程序测试（第十五批） | `pnpm --dir miniapp test -- --run src/__tests__/registration-manage.spec.ts` | 学生端取消报名与状态展示通过 | 1 个文件共 2 个测试通过 | ✓ |
| 后端全量测试（第十六批） | `mvn -f server/pom.xml test` | 评审增强与电子奖状接入后整体通过 | 20 个测试全部通过 | ✓ |
| 后台全量测试（第十六批） | `pnpm --dir admin-web test -- --run` | 评审工作台增强接入后整体通过 | 7 个文件共 13 个测试通过 | ✓ |
| 小程序全量测试（第十六批） | `pnpm --dir miniapp test -- --run` | 结果页评审意见与电子奖状展示接入后整体通过 | 9 个文件共 23 个测试通过 | ✓ |
| 后台构建（第十六批） | `pnpm --dir admin-web build` | 评审增强页面可正常构建 | 构建通过 | ✓ |
| 小程序 H5 构建（第十六批） | `pnpm --dir miniapp build:h5` | 结果页电子奖状展示可正常构建 | 构建通过，仍有既有 `circular dependency/finally` warning | ✓ |
| 每日任务后端测试（第十七批） | `mvn -f server/pom.xml -Dtest=DailyTaskOverviewApiTest test` | 每日签到、比赛分享与个人汇总接口通过 | 1 个测试通过 | ✓ |
| 每日任务小程序测试（第十七批） | `pnpm --dir miniapp test -- --run src/__tests__/daily-task.spec.ts src/__tests__/daily-task-api.spec.ts` | 每日任务 API 与展示工具通过 | 2 个文件共 4 个测试通过 | ✓ |
| 后端全量测试（第十七批） | `mvn -f server/pom.xml test` | 每日任务与个人比赛汇总接入后整体通过 | 21 个测试全部通过 | ✓ |
| 后台全量测试（第十七批） | `pnpm --dir admin-web test -- --run` | 小程序侧增强后后台保持稳定 | 7 个文件共 13 个测试通过 | ✓ |
| 小程序全量测试（第十七批） | `pnpm --dir miniapp test -- --run` | 首页、个人中心、详情页、积分页接入后整体通过 | 11 个文件共 27 个测试通过 | ✓ |
| 后台构建（第十七批） | `pnpm --dir admin-web build` | 当前整仓状态下后台仍可正常构建 | 构建通过 | ✓ |
| 小程序 H5 构建（第十七批） | `pnpm --dir miniapp build:h5` | 每日任务与个人汇总页面可正常构建 | 构建通过，仍有既有 `circular dependency/finally` warning | ✓ |
| 后台治理后端测试（红灯） | `mvn -f server/pom.xml -Dtest=AdminUserGovernanceApiTest test` | 因缺少角色管理接口失败 | 初次返回 `404`，随后暴露 `AdminUserService` 误被扫描和 `FeedbackMapper` 缺少注解问题 | ✓ |
| 后台治理后端测试（绿灯） | `mvn -f server/pom.xml -Dtest=AdminUserGovernanceApiTest test` | 角色管理与用户治理接口通过 | 1 个测试通过 | ✓ |
| 后台治理前端测试（红灯） | `pnpm --dir admin-web test -- --run src/__tests__/api-clients.spec.ts src/__tests__/role-governance.spec.ts` | 因缺少 `roles` API 和治理工具失败 | 失败并提示无法解析 `@/api/roles` 与 `@/utils/role-governance` | ✓ |
| 后台治理前端测试（绿灯） | `pnpm --dir admin-web test -- --run src/__tests__/api-clients.spec.ts src/__tests__/role-governance.spec.ts` | 角色 API、用户治理 API 和表单工具通过 | 2 个文件共 7 个测试通过 | ✓ |
| 后台全量测试（第十八批） | `pnpm --dir admin-web test -- --run` | 角色权限页和用户治理页改造后整体通过 | 8 个文件共 17 个测试通过 | ✓ |
| 后台构建（第十八批） | `pnpm --dir admin-web build` | 后台治理页面可正常构建 | 构建通过 | ✓ |
| 后端全量测试（第十八批） | `mvn -f server/pom.xml test` | 角色管理与用户治理接入后整体通过 | 23 个测试全部通过 | ✓ |
| 数据大屏后端测试（红灯） | `mvn -f server/pom.xml -Dtest=DashboardOverviewApiTest test` | 因缺少后台汇总和老师看板接口失败 | 首次返回 `code=500`，提示不存在 `/api/admin/dashboard/overview` | ✓ |
| 数据大屏后端测试（绿灯） | `mvn -f server/pom.xml -Dtest=DashboardOverviewApiTest test` | 后台汇总、老师看板和 CSV 导出接口通过 | 1 个测试通过 | ✓ |
| 数据大屏后台测试（红灯） | `pnpm --dir admin-web test -- --run src/__tests__/dashboard-api.spec.ts` | 因缺少 `dashboard` API 模块失败 | 失败并提示无法解析 `@/api/dashboard` | ✓ |
| 数据大屏小程序测试（红灯） | `pnpm --dir miniapp test -- --run src/__tests__/teacher-dashboard-api.spec.ts src/__tests__/role-menu.spec.ts` | 因缺少老师看板 API 和首页入口失败 | 失败并提示缺少 `src/api/dashboard`，且菜单不含 `teacher-dashboard` | ✓ |
| 数据大屏后台测试（绿灯） | `pnpm --dir admin-web test -- --run src/__tests__/dashboard-api.spec.ts` | 后台看板 API 与导出请求正确 | 1 个测试通过 | ✓ |
| 数据大屏小程序测试（绿灯） | `pnpm --dir miniapp test -- --run src/__tests__/teacher-dashboard-api.spec.ts src/__tests__/role-menu.spec.ts` | 老师看板 API 与首页入口路由正确 | 2 个测试通过 | ✓ |
| 后台全量测试（第十九批） | `pnpm --dir admin-web test -- --run` | 后台首页升级为真实看板后整体通过 | 9 个文件共 18 个测试通过 | ✓ |
| 小程序全量测试（第十九批） | `pnpm --dir miniapp test -- --run` | 老师看板页面和新菜单接入后整体通过 | 15 个文件共 35 个测试通过 | ✓ |
| 后台构建（第十九批） | `pnpm --dir admin-web build` | 后台看板页面接入后可正常构建 | 构建通过 | ✓ |
| 小程序 H5 构建（第十九批） | `pnpm --dir miniapp build:h5` | 老师看板页面接入后可正常构建 | 构建通过，仍有既有 `circular dependency/finally` warning | ✓ |
| 后端全量测试（第十九批） | `mvn -f server/pom.xml test` | 数据大屏与导出接入后整体通过 | 25 个测试全部通过 | ✓ |

## Error Log
| Timestamp | Error | Attempt | Resolution |
|-----------|-------|---------|------------|
| 2026-03-17 | 并行读取提取文件时序冲突 | 1 | 确认文件已生成后重新读取，问题消失 |
| 2026-03-17 | `HealthControllerTest` 初次执行失败，缺少 Spring Boot 配置类 | 1 | 补齐 `CampusCompetitionApplication` 和健康检查接口 |
| 2026-03-17 | `ApiResponseTest` 初次执行失败，缺少 `ApiResponse` 类 | 1 | 补齐统一响应体与公共基础类 |
| 2026-03-17 | `git status` 失败，提示 `dubious ownership` | 1 | 暂不修改全局 Git 配置，记录为后续提交前置事项 |
| 2026-03-17 | `pnpm install` 在当前盘符下因链接策略失败 | 1 | 改为 `--ignore-workspace --node-linker=hoisted --package-import-method=copy` |
| 2026-03-17 | `miniapp` 初始构建命令错误，直接调用 `vite` 失败 | 1 | 改为 `uni -p h5` 和 `uni build -p h5` |
| 2026-03-17 | `mvn test` 并行执行导致 `.m2` 缓存锁冲突 | 1 | 改为串行执行，恢复成功 |
| 2026-03-17 | PowerShell 把 Maven 的 `-Dtest=A,B` 解析成多个参数 | 1 | 为 `-Dtest` 参数整体加引号，恢复多测试类执行 |
| 2026-03-17 | `CheckinServiceTest`、`SubmissionServiceTest`、`ScorePublishTest` 初次因比赛时间窗口不合法失败 | 1 | 调整测试前置数据为“报名未截止且比赛已开始”的合法场景 |
| 2026-03-17 | 引入持久化后，测试环境缺少事务与数据库兼容支持 | 1 | 增加 `MyBatis-Plus`、`Flyway`、`H2` 依赖，并使用 `application-test.yml` 固定测试数据源 |
| 2026-03-17 | `ScorePointsPersistenceTest` 暴露数据库分支未保存结果积分值 | 1 | 新增 `V6__score_points_column.sql`，并修正 `ScoreEntity`、`ScoreService` 的积分持久化映射 |
| 2026-03-17 | 前端 API 客户端测试初次失败，缺少真实请求模块 | 1 | 新增 `http.ts` 请求层和对应业务 API 模块，恢复测试通过 |
| 2026-03-17 | 报名页最初的姓名/学号/手机号字段与后端命令不一致 | 1 | 调整页面为“当前账号确认报名”，避免前后端数据模型偏差 |
| 2026-03-17 | `uni-app` 测试并行执行出现 `EADDRINUSE` 端口占用告警 | 1 | 改为串行执行小程序测试，避免环境噪音影响验证判断 |
| 2026-03-17 | 老师端发布页和后台结果发布入口仍是骨架，无法做完整闭环验证 | 1 | 补齐真实提交表单、菜单入口和刷新逻辑后，闭环关键入口已可用 |
| 2026-03-17 | 没有开发环境演示账号时，真实登录流程无法直接联调 | 1 | 新增 `dev/demo-data` 演示账号初始化器，并把小程序登录页接到真实接口 |
| 2026-03-17 | 小程序真实登录后没有持久化会导致重启即丢失登录态 | 1 | 增加本地会话存储、启动恢复、登录回跳和基础登录守卫 |
| 2026-03-17 | `CompetitionFlowApiTest` 初次使用固定日期，导致报名接口按业务规则拒绝 | 1 | 改为基于 `LocalDateTime.now()` 生成合法报名/比赛窗口，使闭环回归稳定通过 |
| 2026-03-17 | Windows 下一次性写入较大补丁时触发 `文件名或扩展名太长` | 1 | 改用多次小步 `apply_patch`，分骨架、逻辑、样式三段写入 |
| 2026-03-17 | `ProfileApiTest` 初版依赖固定 `userId=2`，全量测试时会命中错误账号 | 1 | 改为按学号 `S20260001` 动态查询学生 ID，恢复全量稳定性 |
| 2026-03-17 | 比赛公开列表和管理列表最初共用同一查询，无法满足草稿/下架隔离 | 1 | 拆分为“前台只看 `PUBLISHED`，后台/老师端看全量状态”的两套查询语义 |
| 2026-03-17 | 执行剩余模块计划时，Git worktree 方案会因仓库未提交且受 `safe.directory` 限制而丢失当前上下文 | 1 | 保持在当前工作区继续开发，并在文档中显式记录该约束，避免切到空白 worktree |
| 2026-03-17 | 评审增强首轮测试缺少 `/api/admin/reviews/submit` 接口，导致请求落到静态资源处理器返回 `500` | 1 | 补齐 `SubmitReviewCommand`、`ReviewService` 和控制器映射后恢复通过 |
| 2026-03-17 | 每日任务首轮测试缺少 `/api/app/points/tasks/*` 路由，导致请求落到静态资源处理器返回 `500` | 1 | 补齐 `AppPointsTaskController` 和任务模型后恢复通过 |
| 2026-03-17 | 收紧 `@MapperScan` 后，`AdminUserService` 被误识别为 MyBatis Mapper，导致 `Invalid bound statement` | 1 | 将 `MapperScan` 改为只扫描带 `@Mapper` 注解的接口，恢复服务注入语义 |
| 2026-03-17 | `FeedbackMapper` 历史上缺少 `@Mapper`，在新的扫描规则下导致 Spring 上下文启动失败 | 1 | 为 `FeedbackMapper` 显式增加 `@Mapper` 注解，恢复 `ProfileService` 注入 |
| 2026-03-17 | 数据大屏首轮测试命中了不存在的 `/api/admin/dashboard/overview` 路由 | 1 | 新增 `DashboardService`、后台汇总接口、老师看板接口和 CSV 导出接口后恢复通过 |
| 2026-03-17 | `DashboardService` 初版因泛型擦除导致 `countByStatus` 方法签名冲突 | 1 | 将比赛实体统计和看板项统计拆成不同命名的方法，恢复编译通过 |

## 5-Question Reboot Check
| Question | Answer |
|----------|--------|
| Where am I? | Phase 11：去第三方后的剩余模块开发已经完成前十批，个人中心、比赛管理、报名管理、评审增强、每日任务、消息中心、后台治理首批闭环、消息增强和数据大屏都已落地 |
| Where am I going? | 继续实现本地敏感词审核与后台治理深化 |
| What's the goal? | 在不接入任何第三方服务的前提下，把剩余 PRD 的本地可运行能力按批次全部补齐 |
| What have I learned? | 数据大屏首版完全可以先依托真实业务表做实时聚合，再用 CSV 导出满足本地可运行验收，不需要先引入快照表或第三方导出库 |
| What have I done? | 已完成个人中心、比赛生命周期增强、报名与参赛管理增强、评审增强与电子奖状、每日签到/分享任务/个人比赛汇总、消息中心、后台角色权限与用户治理首批闭环、消息增强，以及后台汇总大屏和老师个人看板，并完成最新全量验证 |

## Latest Update

### Phase 20: 第二十批任务执行（本地审核与违规记录）
- **Status:** complete
- Actions taken:
  - 为本地审核补充 `LocalAuditApiTest` 红灯测试，覆盖比赛敏感词拦截、私信敏感词拦截、作品文件白名单校验、审核规则接口和违规记录接口
  - 新增 `V15__local_audit_schema.sql`、`sys_violation_record` 表、`ContentAuditService`、`AdminAuditController`、违规记录实体与 Mapper
  - 将比赛发布、消息发送、作品上传三条主链路接入本地审核服务，命中后立即拦截并记录违规内容摘要
  - 为后台补充 `audit-api.spec.ts` 红灯测试和 `/system/audit` 路由，新增审核规则与违规记录页
  - 修复审核接口在 `MockMvc` 手工读取响应体时缺少 UTF-8 charset 导致的中文断言伪失败
  - 重新执行后端全量测试、后台全量测试/构建、小程序全量测试/H5 构建，确认整仓仍通过
- Files created/modified:
  - `server/src/main/resources/db/migration/V15__local_audit_schema.sql`（created）
  - `server/src/main/java/com/campus/competition/modules/audit/persistence/ViolationRecordEntity.java`（created）
  - `server/src/main/java/com/campus/competition/modules/audit/mapper/ViolationRecordMapper.java`（created）
  - `server/src/main/java/com/campus/competition/modules/audit/model/AuditRuleSummary.java`（created）
  - `server/src/main/java/com/campus/competition/modules/audit/model/ViolationRecordSummary.java`（created）
  - `server/src/main/java/com/campus/competition/modules/audit/service/ContentAuditService.java`（created）
  - `server/src/main/java/com/campus/competition/modules/audit/controller/AdminAuditController.java`（created）
  - `server/src/main/java/com/campus/competition/modules/competition/service/CompetitionService.java`（modified）
  - `server/src/main/java/com/campus/competition/modules/message/service/MessageService.java`（modified）
  - `server/src/main/java/com/campus/competition/modules/submission/service/SubmissionService.java`（modified）
  - `server/src/test/java/com/campus/competition/modules/audit/LocalAuditApiTest.java`（created）
  - `admin-web/src/api/audit.ts`（created）
  - `admin-web/src/views/system/audit/AuditViolationPage.vue`（created）
  - `admin-web/src/router/index.ts`（modified）
  - `admin-web/src/__tests__/audit-api.spec.ts`（created）
  - `admin-web/src/__tests__/router.spec.ts`（modified）

## Latest Verification

| Check | Command | Purpose | Result | Status |
|-------|---------|---------|--------|--------|
| 本地审核后端测试（红灯） | `mvn -f server/pom.xml -Dtest=LocalAuditApiTest test` | 确认缺少审核逻辑时，比赛敏感词不会被拦截 | 初次失败，返回 `code=0` 而非预期的 `code=400` | ✓ |
| 后台审核 API/路由测试（红灯） | `pnpm --dir admin-web test -- --run src/__tests__/audit-api.spec.ts src/__tests__/router.spec.ts` | 确认缺少审核 API 模块和 `/system/audit` 路由时会失败 | 初次失败，提示无法解析 `@/api/audit` 且路由缺失 | ✓ |
| 本地审核后端测试（绿灯） | `mvn -f server/pom.xml -Dtest=LocalAuditApiTest test` | 验证比赛/消息/作品审核拦截、规则接口和违规记录接口 | 1 个测试通过 | ✓ |
| 后端全量测试（第二十批） | `mvn -f server/pom.xml test` | 本地审核接入后整仓后端保持稳定 | 26 个测试全部通过 | ✓ |
| 后台审核 API/路由测试（绿灯） | `pnpm --dir admin-web test -- --run src/__tests__/audit-api.spec.ts src/__tests__/router.spec.ts` | 验证后台审核 API 路径和审核页路由 | 2 个测试通过 | ✓ |
| 后台全量测试（第二十批） | `pnpm --dir admin-web test -- --run` | 审核页接入后后台整体通过 | 10 个文件共 19 个测试通过 | ✓ |
| 后台构建（第二十批） | `pnpm --dir admin-web build` | 审核页接入后后台可正常构建 | 构建通过 | ✓ |
| 小程序全量测试（第二十批） | `pnpm --dir miniapp test -- --run` | 审核批次未改小程序，但需确认整仓回归稳定 | 15 个文件共 35 个测试通过 | ✓ |
| 小程序 H5 构建（第二十批） | `pnpm --dir miniapp build:h5` | 确认小程序在最新整仓状态下仍可构建 | 构建通过，仍有既有 `circular dependency/finally` warning | ✓ |

## 5-Question Reboot Check (Latest)
| Question | Answer |
|----------|--------|
| Where am I? | Phase 11：去第三方后的剩余模块开发已经完成到 `Task 10`，本地审核与违规记录闭环已落地 |
| Where am I going? | 继续实现后台治理深化，包括菜单级权限、日志导出和更多治理动作 |
| What's the goal? | 在不接入任何第三方服务的前提下，把剩余 PRD 的本地可运行能力按批次全部补齐 |
| What have I learned? | `MockMvc` 的手工响应体读取如果没有显式 UTF-8 charset，会造成中文断言出现伪失败，审核接口这类需要二次解析 JSON 的场景必须显式声明字符集 |
| What have I done? | 已完成个人中心、比赛增强、报名增强、评审增强、每日任务、消息中心、后台治理首批闭环、消息增强、数据大屏，以及本地审核与违规记录闭环，并完成最新全量回归 |
