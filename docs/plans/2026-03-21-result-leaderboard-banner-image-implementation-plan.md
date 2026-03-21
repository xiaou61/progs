# Result Leaderboard And Banner Image Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 将比赛结果页改成比赛公开榜单，并把后台轮播图升级为支持本地图片上传的图片轮播。

**Architecture:** 后端在现有比赛结果接口上补充获奖人信息视图，同时为 banner 增加图片字段、图片上传接口和静态资源映射；管理端增加 banner 图片上传与预览；小程序结果页改用比赛榜单接口，首页轮播图优先展示图片并允许空跳转。

**Tech Stack:** Spring Boot、MyBatis-Plus、Vue 3、Vitest、微信小程序原生页面

---

### Task 1: 比赛榜单接口

**Files:**
- Create: `server/src/test/java/com/campus/competition/modules/result/AppResultControllerTest.java`
- Modify: `server/src/main/java/com/campus/competition/modules/result/controller/AppResultController.java`
- Modify: `server/src/main/java/com/campus/competition/modules/score/service/ScoreService.java`
- Create: `server/src/main/java/com/campus/competition/modules/result/model/CompetitionResultItem.java`

**Step 1: Write the failing test**

验证比赛结果接口返回榜单时，包含学生姓名和学号。

**Step 2: Run test to verify it fails**

Run: `mvn -f server/pom.xml "-Dtest=AppResultControllerTest" test`

Expected: FAIL，因为返回结构里还没有 `studentName/studentNo`。

**Step 3: Write minimal implementation**

- 在结果控制器中保留 `/api/app/results/competition/{competitionId}` 路由。
- 服务层新增比赛榜单视图装配。
- 通过 `UserMapper` 将 `studentId` 转为 `studentNo/realName`。

**Step 4: Run test to verify it passes**

Run: `mvn -f server/pom.xml "-Dtest=AppResultControllerTest" test`

Expected: PASS

### Task 2: 轮播图图片字段与上传链路

**Files:**
- Create: `server/src/main/resources/db/migration/V20__banner_image_upload.sql`
- Modify: `server/src/main/java/com/campus/competition/modules/system/persistence/BannerEntity.java`
- Modify: `server/src/main/java/com/campus/competition/modules/system/controller/AdminBannerController.java`
- Modify: `server/src/main/java/com/campus/competition/modules/system/controller/AppBannerController.java`
- Create: `server/src/main/java/com/campus/competition/modules/system/service/BannerFileStorageService.java`
- Create: `server/src/main/java/com/campus/competition/modules/system/controller/AdminBannerFileController.java`
- Modify: `server/src/main/java/com/campus/competition/modules/common/config/UploadStaticResourceConfig.java`
- Create: `server/src/test/java/com/campus/competition/modules/system/AdminBannerFileControllerTest.java`

**Step 1: Write the failing tests**

- 验证 banner 视图中包含 `imageUrl`
- 验证上传接口返回 `/uploads/banners/...`

**Step 2: Run tests to verify they fail**

Run: `mvn -f server/pom.xml "-Dtest=AppBannerControllerTest,AdminBannerFileControllerTest" test`

Expected: FAIL

**Step 3: Write minimal implementation**

- 新增 `image_url` 字段迁移
- 新增 banner 文件存储服务和上传控制器
- 更新 app/admin banner 视图返回值
- `jumpPath` 改为可空，但标题仍必填

**Step 4: Run tests to verify they pass**

Run: `mvn -f server/pom.xml "-Dtest=AppBannerControllerTest,AdminBannerFileControllerTest" test`

Expected: PASS

### Task 3: 管理端轮播图上传与预览

**Files:**
- Modify: `admin-web/src/api/system.ts`
- Create: `admin-web/src/api/upload.ts`
- Modify: `admin-web/src/views/system/banner/BannerPage.vue`
- Modify: `admin-web/src/__tests__/api-clients.spec.ts`
- Create: `admin-web/src/__tests__/banner-page-upload.spec.ts`

**Step 1: Write the failing tests**

- API client 断言 `imageUrl`
- 页面源码断言上传按钮、图片预览和“跳转路径可空”的文案/逻辑存在

**Step 2: Run tests to verify they fail**

Run: `pnpm -C admin-web exec vitest run src/__tests__/api-clients.spec.ts src/__tests__/banner-page-upload.spec.ts`

Expected: FAIL

**Step 3: Write minimal implementation**

- 后台 API 类型增加 `imageUrl`
- 新增 banner 图片上传 API
- 页面增加图片上传、上传中状态、预览、空跳转路径说明

**Step 4: Run tests to verify they pass**

Run: `pnpm -C admin-web exec vitest run src/__tests__/api-clients.spec.ts src/__tests__/banner-page-upload.spec.ts`

Expected: PASS

### Task 4: 小程序结果页与图片轮播

**Files:**
- Modify: `wechat-native/services/result.js`
- Modify: `wechat-native/utils/result.js`
- Modify: `wechat-native/pages/competition/result/index.js`
- Modify: `wechat-native/pages/competition/result/index.wxml`
- Modify: `wechat-native/pages/competition/result/index.wxss`
- Modify: `wechat-native/services/system.js`
- Modify: `wechat-native/pages/home/index.wxml`
- Modify: `wechat-native/pages/home/index.wxss`
- Modify: `wechat-native/tests/result-utils.test.js`
- Create: `wechat-native/tests/result-banner-source.test.js`

**Step 1: Write the failing tests**

- 结果工具断言榜单卡片包含学生姓名/学号
- 首页源码断言图片轮播和空跳转保护存在

**Step 2: Run tests to verify they fail**

Run: `node --test wechat-native/tests/result-utils.test.js wechat-native/tests/result-banner-source.test.js`

Expected: FAIL

**Step 3: Write minimal implementation**

- 结果页带 `competitionId` 时改用比赛榜单接口
- 结果卡片展示获奖人信息
- 首页 banner 优先图片渲染，无图片回退文字卡片
- 点击无 `jumpPath` 的轮播项时直接返回

**Step 4: Run tests to verify they pass**

Run: `node --test wechat-native/tests/result-utils.test.js wechat-native/tests/result-banner-source.test.js`

Expected: PASS

### Task 5: 聚焦验证

**Files:**
- Verify only

**Step 1: Run backend targeted tests**

Run: `mvn -f server/pom.xml "-Dtest=AppResultControllerTest,AppBannerControllerTest,AdminBannerFileControllerTest" test`

**Step 2: Run admin targeted tests**

Run: `pnpm -C admin-web exec vitest run src/__tests__/api-clients.spec.ts src/__tests__/banner-page-upload.spec.ts`

**Step 3: Run mini-program targeted tests**

Run: `node --test wechat-native/tests/result-utils.test.js wechat-native/tests/result-banner-source.test.js`

**Step 4: Commit**

```bash
git add server admin-web wechat-native docs/plans
git commit -m "feat: expose competition leaderboards and banner images"
```
