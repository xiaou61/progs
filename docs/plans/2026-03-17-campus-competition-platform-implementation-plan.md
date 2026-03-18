# Campus Competition Platform Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 从 0 开始搭建师生比赛管理平台的首期可运行版本，完成工程初始化、统一鉴权、比赛主链路和基础后台能力。

**Architecture:** 采用单仓多应用结构，`server` 提供统一领域服务与接口，`admin-web` 负责后台管理，`miniapp` 负责学生/老师小程序入口。首期以单体后端 + 模块化分层为主，优先交付“注册登录 -> 发布比赛 -> 报名 -> 签到/作品上传 -> 评分结果 -> 积分入账”的闭环。

**Tech Stack:** Vue 3, Vite, TypeScript, Pinia, Element Plus, uni-app, Spring Boot 3, MyBatis-Plus, MySQL 8, Redis, MinIO, Sa-Token, EasyExcel, ECharts

---

## 实施边界

本计划聚焦首期落地，不在首期实现以下能力：

- 一对一私信和比赛群聊
- 商城现金支付
- 高级大屏与复杂钻取
- 第三方内容审核正式接入
- 多校区隔离的高级权限模型

## 目录约定

```text
/
├─ admin-web/
├─ miniapp/
├─ server/
├─ infra/
├─ docs/
│  ├─ api/
│  ├─ db/
│  ├─ plans/
│  └─ runbooks/
├─ scripts/
├─ pnpm-workspace.yaml
└─ README.md
```

### Task 1: 初始化仓库与工作区

**Files:**
- Create: `README.md`
- Create: `pnpm-workspace.yaml`
- Create: `.editorconfig`
- Create: `.gitignore`
- Create: `infra/docker-compose.yml`
- Create: `scripts/verify-structure.ps1`
- Create: `docs/runbooks/local-setup.md`

**Step 1: 写结构校验脚本，先让它失败**

```powershell
$required = @('admin-web', 'miniapp', 'server', 'infra', 'docs')
$missing = $required | Where-Object { -not (Test-Path $_) }
if ($missing.Count -gt 0) {
  Write-Error ("Missing: " + ($missing -join ', '))
  exit 1
}
Write-Host "Workspace structure ok"
```

**Step 2: 运行校验脚本确认失败**

Run: `powershell -ExecutionPolicy Bypass -File .\scripts\verify-structure.ps1`
Expected: FAIL with `Missing: admin-web, miniapp, server, infra, docs`

**Step 3: 创建工作区骨架与基础文件**

```yaml
packages:
  - admin-web
  - miniapp
```

**Step 4: 再次运行校验脚本确认通过**

Run: `powershell -ExecutionPolicy Bypass -File .\scripts\verify-structure.ps1`
Expected: PASS with `Workspace structure ok`

**Step 5: 初始化 Git**

Run: `git init`
Expected: repository initialized

**Step 6: Commit**

```bash
git add .
git commit -m "chore: initialize workspace structure"
```

### Task 2: 搭建后端基础工程

**Files:**
- Create: `server/pom.xml`
- Create: `server/src/main/java/com/campus/competition/CampusCompetitionApplication.java`
- Create: `server/src/main/resources/application.yml`
- Create: `server/src/main/resources/application-dev.yml`
- Create: `server/src/test/java/com/campus/competition/HealthControllerTest.java`
- Create: `server/src/main/java/com/campus/competition/modules/common/controller/HealthController.java`

**Step 1: 先写健康检查失败测试**

```java
@SpringBootTest
@AutoConfigureMockMvc
class HealthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/public/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }
}
```

**Step 2: 运行测试确认失败**

Run: `mvn -f server/pom.xml -Dtest=HealthControllerTest test`
Expected: FAIL with `404` or application bootstrap failure

**Step 3: 写最小可运行后端骨架**

```java
@SpringBootApplication
public class CampusCompetitionApplication {
    public static void main(String[] args) {
        SpringApplication.run(CampusCompetitionApplication.class, args);
    }
}
```

```java
@RestController
@RequestMapping("/api/public/health")
public class HealthController {
    @GetMapping
    public Map<String, Object> health() {
        return Map.of("code", 0, "message", "ok", "data", "UP");
    }
}
```

**Step 4: 跑测试确认通过**

Run: `mvn -f server/pom.xml -Dtest=HealthControllerTest test`
Expected: PASS

**Step 5: 补充本地配置文档**

Run: edit `docs/runbooks/local-setup.md`
Expected: record Java, Maven, MySQL, Redis, MinIO startup steps

**Step 6: Commit**

```bash
git add server docs/runbooks/local-setup.md
git commit -m "feat: bootstrap spring boot service"
```

### Task 3: 建立后端公共基础设施

**Files:**
- Create: `server/src/main/java/com/campus/competition/modules/common/config/SaTokenConfig.java`
- Create: `server/src/main/java/com/campus/competition/modules/common/config/MybatisPlusConfig.java`
- Create: `server/src/main/java/com/campus/competition/modules/common/exception/GlobalExceptionHandler.java`
- Create: `server/src/main/java/com/campus/competition/modules/common/model/ApiResponse.java`
- Create: `server/src/main/java/com/campus/competition/modules/common/util/UserContext.java`
- Create: `server/src/test/java/com/campus/competition/ApiResponseTest.java`

**Step 1: 先写响应体测试**

```java
class ApiResponseTest {
    @Test
    void successShouldContainCodeZero() {
        ApiResponse<String> response = ApiResponse.success("ok");
        assertEquals(0, response.getCode());
        assertEquals("ok", response.getData());
    }
}
```

**Step 2: 运行测试确认失败**

Run: `mvn -f server/pom.xml -Dtest=ApiResponseTest test`
Expected: FAIL with `cannot find symbol ApiResponse`

**Step 3: 实现通用响应与异常处理**

```java
@Data
@AllArgsConstructor
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(0, "success", data);
    }
}
```

**Step 4: 运行测试确认通过**

Run: `mvn -f server/pom.xml -Dtest=ApiResponseTest test`
Expected: PASS

**Step 5: 集成 Sa-Token、MyBatis-Plus、统一异常**

Run: `mvn -f server/pom.xml test`
Expected: project boots and tests all pass

**Step 6: Commit**

```bash
git add server
git commit -m "feat: add backend common infrastructure"
```

### Task 4: 搭建管理后台工程

**Files:**
- Create: `admin-web/package.json`
- Create: `admin-web/vite.config.ts`
- Create: `admin-web/src/main.ts`
- Create: `admin-web/src/App.vue`
- Create: `admin-web/src/router/index.ts`
- Create: `admin-web/src/stores/app.ts`
- Create: `admin-web/src/views/login/LoginPage.vue`
- Create: `admin-web/src/views/dashboard/DashboardPage.vue`
- Create: `admin-web/src/__tests__/router.spec.ts`

**Step 1: 先写后台路由测试**

```ts
import { describe, expect, it } from 'vitest'
import { routes } from '../router'

describe('admin routes', () => {
  it('contains login and dashboard routes', () => {
    expect(routes.some((item) => item.path === '/login')).toBe(true)
    expect(routes.some((item) => item.path === '/')).toBe(true)
  })
})
```

**Step 2: 运行测试确认失败**

Run: `pnpm --dir admin-web test -- --run`
Expected: FAIL because routes file does not exist

**Step 3: 搭建 Vue3 + Vite + Pinia + Router 骨架**

```ts
export const routes = [
  { path: '/login', component: () => import('@/views/login/LoginPage.vue') },
  { path: '/', component: () => import('@/views/dashboard/DashboardPage.vue') }
]
```

**Step 4: 运行测试确认通过**

Run: `pnpm --dir admin-web test -- --run`
Expected: PASS

**Step 5: 启动开发服务冒烟**

Run: `pnpm --dir admin-web dev`
Expected: login page and dashboard page render without blank screen

**Step 6: Commit**

```bash
git add admin-web
git commit -m "feat: bootstrap admin web application"
```

### Task 5: 搭建小程序工程

**Files:**
- Create: `miniapp/package.json`
- Create: `miniapp/src/main.ts`
- Create: `miniapp/src/pages.json`
- Create: `miniapp/src/App.vue`
- Create: `miniapp/src/stores/user.ts`
- Create: `miniapp/src/pages/login/index.vue`
- Create: `miniapp/src/pages/home/index.vue`
- Create: `miniapp/src/__tests__/role-menu.spec.ts`

**Step 1: 先写角色菜单测试**

```ts
import { describe, expect, it } from 'vitest'
import { buildHomeMenus } from '@/stores/user'

describe('home menus', () => {
  it('teacher should see manage menu', () => {
    expect(buildHomeMenus('TEACHER')).toContain('competition-manage')
  })
})
```

**Step 2: 运行测试确认失败**

Run: `pnpm --dir miniapp test -- --run`
Expected: FAIL because store does not exist

**Step 3: 实现 uni-app 基础骨架与角色菜单逻辑**

```ts
export function buildHomeMenus(role: 'STUDENT' | 'TEACHER') {
  return role === 'TEACHER'
    ? ['competition-list', 'my-points', 'competition-manage']
    : ['competition-list', 'my-points']
}
```

**Step 4: 运行测试确认通过**

Run: `pnpm --dir miniapp test -- --run`
Expected: PASS

**Step 5: 真机/开发者工具冒烟**

Run: `pnpm --dir miniapp dev:h5`
Expected: login page and home page can switch by role

**Step 6: Commit**

```bash
git add miniapp
git commit -m "feat: bootstrap miniapp application"
```

### Task 6: 实现账号、用户、角色、校区基础域

**Files:**
- Create: `server/src/main/resources/db/migration/V1__init_base_schema.sql`
- Create: `server/src/main/java/com/campus/competition/modules/auth/controller/AppAuthController.java`
- Create: `server/src/main/java/com/campus/competition/modules/auth/service/AuthService.java`
- Create: `server/src/main/java/com/campus/competition/modules/user/controller/AdminUserController.java`
- Create: `server/src/main/java/com/campus/competition/modules/org/controller/AdminCampusController.java`
- Create: `server/src/test/java/com/campus/competition/modules/auth/AuthServiceTest.java`

**Step 1: 先写注册登录测试**

```java
class AuthServiceTest {
    @Test
    void shouldRegisterStudentWithUniqueStudentNo() {
        RegisterCommand command = new RegisterCommand("20260001", "张三", "13800000000", "STUDENT", "Abcd1234");
        Long userId = authService.register(command);
        assertNotNull(userId);
    }
}
```

**Step 2: 运行测试确认失败**

Run: `mvn -f server/pom.xml -Dtest=AuthServiceTest test`
Expected: FAIL because command/service/schema do not exist

**Step 3: 建表并实现最小注册登录能力**

```sql
create table sys_user (
  id bigint primary key auto_increment,
  student_no varchar(32) not null unique,
  real_name varchar(64) not null,
  phone varchar(20) not null,
  role_code varchar(32) not null,
  password_hash varchar(128) not null,
  status varchar(16) not null default 'ENABLED'
);
```

**Step 4: 运行测试确认通过**

Run: `mvn -f server/pom.xml -Dtest=AuthServiceTest test`
Expected: PASS

**Step 5: 补充后台用户和校区基础 CRUD**

Run: `mvn -f server/pom.xml test`
Expected: auth and admin base tests pass

**Step 6: Commit**

```bash
git add server
git commit -m "feat: add auth user role and campus base domain"
```

### Task 7: 实现后台用户、角色、校区管理页面

**Files:**
- Create: `admin-web/src/api/auth.ts`
- Create: `admin-web/src/api/users.ts`
- Create: `admin-web/src/api/campuses.ts`
- Create: `admin-web/src/views/system/users/UserListPage.vue`
- Create: `admin-web/src/views/system/roles/RoleListPage.vue`
- Create: `admin-web/src/views/system/campuses/CampusListPage.vue`
- Create: `admin-web/src/__tests__/permission-menu.spec.ts`

**Step 1: 先写权限菜单测试**

```ts
describe('permission menus', () => {
  it('admin should see user and campus menus', () => {
    const menus = buildMenus(['USER_MANAGE', 'CAMPUS_MANAGE'])
    expect(menus).toContain('users')
    expect(menus).toContain('campuses')
  })
})
```

**Step 2: 运行测试确认失败**

Run: `pnpm --dir admin-web test -- --run`
Expected: FAIL because menu builder or pages do not exist

**Step 3: 实现后台基础系统管理页**

```ts
export function buildMenus(codes: string[]) {
  return [
    ...(codes.includes('USER_MANAGE') ? ['users'] : []),
    ...(codes.includes('CAMPUS_MANAGE') ? ['campuses'] : [])
  ]
}
```

**Step 4: 运行测试确认通过**

Run: `pnpm --dir admin-web test -- --run`
Expected: PASS

**Step 5: 联调后台登录和用户管理**

Run: `pnpm --dir admin-web dev`
Expected: can log in and view user/campus lists with mock or real API

**Step 6: Commit**

```bash
git add admin-web
git commit -m "feat: add admin user role and campus pages"
```

### Task 8: 实现比赛基础域与老师发布流程

**Files:**
- Create: `server/src/main/resources/db/migration/V2__competition_schema.sql`
- Create: `server/src/main/java/com/campus/competition/modules/competition/controller/AppCompetitionController.java`
- Create: `server/src/main/java/com/campus/competition/modules/competition/controller/AdminCompetitionController.java`
- Create: `server/src/main/java/com/campus/competition/modules/competition/service/CompetitionService.java`
- Create: `server/src/test/java/com/campus/competition/modules/competition/CompetitionPublishTest.java`
- Create: `admin-web/src/views/competition/CompetitionEditorPage.vue`
- Create: `miniapp/src/pages/teacher/competition-editor/index.vue`

**Step 1: 先写比赛发布测试**

```java
class CompetitionPublishTest {
    @Test
    void shouldPublishCompetitionWhenRequiredFieldsPresent() {
        PublishCompetitionCommand command = TestCompetitionCommands.valid();
        Long competitionId = competitionService.publish(command);
        assertNotNull(competitionId);
    }
}
```

**Step 2: 运行测试确认失败**

Run: `mvn -f server/pom.xml -Dtest=CompetitionPublishTest test`
Expected: FAIL because competition schema/service do not exist

**Step 3: 实现比赛表结构与发布服务**

```sql
create table cmp_competition (
  id bigint primary key auto_increment,
  title varchar(128) not null,
  organizer_id bigint not null,
  status varchar(16) not null,
  signup_start_at datetime not null,
  signup_end_at datetime not null,
  start_at datetime not null,
  end_at datetime not null
);
```

**Step 4: 运行测试确认通过**

Run: `mvn -f server/pom.xml -Dtest=CompetitionPublishTest test`
Expected: PASS

**Step 5: 实现老师发布页与后台比赛管理页**

Run: `pnpm --dir admin-web test -- --run`
Expected: publish form renders and validation works

**Step 6: Commit**

```bash
git add server admin-web miniapp
git commit -m "feat: add competition publish workflow"
```

### Task 9: 实现学生比赛浏览与报名流程

**Files:**
- Create: `server/src/main/resources/db/migration/V3__registration_schema.sql`
- Create: `server/src/main/java/com/campus/competition/modules/registration/controller/AppRegistrationController.java`
- Create: `server/src/main/java/com/campus/competition/modules/registration/service/RegistrationService.java`
- Create: `server/src/test/java/com/campus/competition/modules/registration/RegistrationServiceTest.java`
- Create: `miniapp/src/pages/competition/list/index.vue`
- Create: `miniapp/src/pages/competition/detail/index.vue`
- Create: `miniapp/src/pages/competition/register/index.vue`

**Step 1: 先写报名测试**

```java
class RegistrationServiceTest {
    @Test
    void shouldRegisterWhenQuotaAvailable() {
        Long registrationId = registrationService.register(validCommand());
        assertNotNull(registrationId);
    }
}
```

**Step 2: 运行测试确认失败**

Run: `mvn -f server/pom.xml -Dtest=RegistrationServiceTest test`
Expected: FAIL because registration schema/service do not exist

**Step 3: 实现报名能力**

```sql
create table cmp_registration (
  id bigint primary key auto_increment,
  competition_id bigint not null,
  user_id bigint not null,
  status varchar(16) not null,
  created_at datetime not null
);
```

**Step 4: 运行测试确认通过**

Run: `mvn -f server/pom.xml -Dtest=RegistrationServiceTest test`
Expected: PASS

**Step 5: 实现小程序列表、详情、报名页**

Run: `pnpm --dir miniapp test -- --run`
Expected: list/detail/register page tests pass

**Step 6: Commit**

```bash
git add server miniapp
git commit -m "feat: add competition browse and registration flow"
```

### Task 10: 实现签到与作品上传流程

**Files:**
- Create: `server/src/main/resources/db/migration/V4__checkin_submission_schema.sql`
- Create: `server/src/main/java/com/campus/competition/modules/checkin/controller/AppCheckinController.java`
- Create: `server/src/main/java/com/campus/competition/modules/submission/controller/AppSubmissionController.java`
- Create: `server/src/main/java/com/campus/competition/modules/submission/service/SubmissionService.java`
- Create: `server/src/test/java/com/campus/competition/modules/checkin/CheckinServiceTest.java`
- Create: `server/src/test/java/com/campus/competition/modules/submission/SubmissionServiceTest.java`
- Create: `miniapp/src/pages/competition/checkin/index.vue`
- Create: `miniapp/src/pages/competition/submission/index.vue`

**Step 1: 先写签到和作品上传测试**

```java
@Test
void shouldCheckInWithinTimeWindow() {
    boolean checked = checkinService.checkIn(validCommand());
    assertTrue(checked);
}
```

```java
@Test
void shouldReplaceOldSubmissionWhenReuploadAllowed() {
    Long submissionId = submissionService.submit(validCommand());
    assertNotNull(submissionId);
}
```

**Step 2: 运行测试确认失败**

Run: `mvn -f server/pom.xml -Dtest=CheckinServiceTest,SubmissionServiceTest test`
Expected: FAIL because services do not exist

**Step 3: 实现签到与提交能力**

```sql
create table cmp_checkin (
  id bigint primary key auto_increment,
  competition_id bigint not null,
  user_id bigint not null,
  method varchar(16) not null,
  checked_at datetime not null
);
```

```sql
create table cmp_submission (
  id bigint primary key auto_increment,
  competition_id bigint not null,
  user_id bigint not null,
  file_url varchar(255) not null,
  version_no int not null default 1
);
```

**Step 4: 运行测试确认通过**

Run: `mvn -f server/pom.xml -Dtest=CheckinServiceTest,SubmissionServiceTest test`
Expected: PASS

**Step 5: 实现小程序签到和上传页**

Run: `pnpm --dir miniapp test -- --run`
Expected: checkin and submission pages pass component/store tests

**Step 6: Commit**

```bash
git add server miniapp
git commit -m "feat: add checkin and submission flow"
```

### Task 11: 实现评审、评分、结果发布与积分入账

**Files:**
- Create: `server/src/main/resources/db/migration/V5__review_score_points_schema.sql`
- Create: `server/src/main/java/com/campus/competition/modules/review/controller/AdminReviewController.java`
- Create: `server/src/main/java/com/campus/competition/modules/score/controller/AdminScoreController.java`
- Create: `server/src/main/java/com/campus/competition/modules/result/controller/AppResultController.java`
- Create: `server/src/main/java/com/campus/competition/modules/points/service/PointsService.java`
- Create: `server/src/test/java/com/campus/competition/modules/score/ScorePublishTest.java`
- Create: `admin-web/src/views/review/ReviewWorkbenchPage.vue`
- Create: `miniapp/src/pages/competition/result/index.vue`
- Create: `miniapp/src/pages/points/index.vue`

**Step 1: 先写结果发布与积分测试**

```java
class ScorePublishTest {
    @Test
    void shouldPublishResultsAndGrantPoints() {
        PublishResultCommand command = validCommand();
        scoreService.publish(command);
        assertEquals(30, pointsService.queryAvailablePoints(studentId));
    }
}
```

**Step 2: 运行测试确认失败**

Run: `mvn -f server/pom.xml -Dtest=ScorePublishTest test`
Expected: FAIL because score and points services do not exist

**Step 3: 实现评审、评分、结果、积分表与服务**

```sql
create table pts_account (
  user_id bigint primary key,
  available_points int not null default 0,
  total_points int not null default 0
);
```

```sql
create table pts_record (
  id bigint primary key auto_increment,
  user_id bigint not null,
  change_amount int not null,
  biz_type varchar(32) not null,
  biz_id bigint,
  remark varchar(255)
);
```

**Step 4: 运行测试确认通过**

Run: `mvn -f server/pom.xml -Dtest=ScorePublishTest test`
Expected: PASS

**Step 5: 实现后台评分页和小程序结果页/积分页**

Run: `pnpm --dir admin-web test -- --run && pnpm --dir miniapp test -- --run`
Expected: PASS

**Step 6: Commit**

```bash
git add server admin-web miniapp
git commit -m "feat: add review scoring result and points flow"
```

### Task 12: 实现后台基础运营能力与验收文档

**Files:**
- Create: `server/src/main/java/com/campus/competition/modules/system/controller/AdminBannerController.java`
- Create: `server/src/main/java/com/campus/competition/modules/system/controller/AdminConfigController.java`
- Create: `server/src/main/java/com/campus/competition/modules/log/controller/AdminLogController.java`
- Create: `admin-web/src/views/system/banner/BannerPage.vue`
- Create: `admin-web/src/views/system/config/SystemConfigPage.vue`
- Create: `docs/api/mvp-api-list.md`
- Create: `docs/db/mvp-schema.md`
- Create: `docs/runbooks/mvp-acceptance.md`

**Step 1: 先写日志列表接口测试**

```java
@Test
void shouldReturnOperationLogs() throws Exception {
    mockMvc.perform(get("/api/admin/logs"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(0));
}
```

**Step 2: 运行测试确认失败**

Run: `mvn -f server/pom.xml test`
Expected: FAIL because log controller does not exist

**Step 3: 实现轮播图、系统配置、日志查询最小能力**

```java
@RestController
@RequestMapping("/api/admin/logs")
public class AdminLogController {
    @GetMapping
    public ApiResponse<List<String>> list() {
        return ApiResponse.success(List.of());
    }
}
```

**Step 4: 运行测试确认通过**

Run: `mvn -f server/pom.xml test`
Expected: PASS

**Step 5: 完成联调与验收文档**

Run: update `docs/api/mvp-api-list.md`, `docs/db/mvp-schema.md`, `docs/runbooks/mvp-acceptance.md`
Expected: all MVP endpoints, tables, and acceptance cases documented

**Step 6: Commit**

```bash
git add server admin-web docs
git commit -m "feat: add base system ops and mvp docs"
```

## MVP 验收清单

- 管理后台可登录
- 小程序支持学生/老师角色登录
- 老师可以发布比赛
- 学生可以浏览、报名、签到、上传作品
- 老师可以查看报名名单、评审、评分、发布结果
- 学生可以查看结果与积分
- 后台可查看用户、比赛、校区、日志、系统配置基础页

## MVP 后续路线

MVP 完成后，再按以下顺序进入第二轮规划：

1. 每日签到与分享积分
2. 奖品商城与订单
3. 系统消息
4. 私信与比赛群聊
5. 数据大屏
6. 多校区与复杂权限
7. 内容审核与第三方系统对接

## 执行提示

- 每个 Task 完成后先跑对应测试再提交
- 所有接口先写测试再写实现
- 所有页面至少保留 1 个可自动化验证的 store/router/api 测试
- 数据库迁移统一放在 `server/src/main/resources/db/migration`
- 所有新增接口同步登记到 `docs/api/mvp-api-list.md`
