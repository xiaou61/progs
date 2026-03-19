# Local Upload And Result Filter Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 为微信小程序补齐本地文件上传能力，并让比赛结果页支持按 `competitionId` 查看单场比赛结果。

**Architecture:** 后端新增独立的本地文件上传控制器和静态资源映射，把上传文件保存到服务器本地目录并暴露可访问 URL。小程序提交页改成“选择文件 -> 上传文件 -> 提交作品记录”的两段式流程，结果页则在现有学生结果汇总接口上按 `competitionId` 做前端过滤。

**Tech Stack:** Spring Boot 3、MockMvc、Vue/微信小程序原生 JS、Node `node:test`

---

### Task 1: 为本地上传接口写失败测试

**Files:**
- Create: `server/src/test/java/com/campus/competition/modules/submission/SubmissionFileUploadApiTest.java`

**Step 1: Write the failing test**

- 使用 `MockMultipartFile` 调用新的上传接口。
- 断言返回 `fileUrl` 以 `/uploads/submissions/` 开头。
- 断言上传后本地测试目录中存在实际文件。

**Step 2: Run test to verify it fails**

Run: `mvn -f server/pom.xml "-Dtest=SubmissionFileUploadApiTest" test`

Expected: FAIL，因为上传接口和本地存储实现尚不存在。

**Step 3: Write minimal implementation**

- 新增本地文件存储服务。
- 新增上传控制器。
- 新增静态资源映射配置。

**Step 4: Run test to verify it passes**

Run: `mvn -f server/pom.xml "-Dtest=SubmissionFileUploadApiTest" test`

Expected: PASS。

**Step 5: Commit**

```bash
git add server/src/test/java/com/campus/competition/modules/submission/SubmissionFileUploadApiTest.java server/src/main/java/com/campus/competition/modules/submission server/src/main/java/com/campus/competition/modules/common/config
git commit -m "feat: add local submission file upload"
```

### Task 2: 为小程序上传服务写失败测试

**Files:**
- Modify: `wechat-native/services/submission.js`
- Create: `wechat-native/tests/submission-upload.test.js`

**Step 1: Write the failing test**

- mock `wx.uploadFile`
- 断言上传服务会携带 Bearer token
- 断言成功时能返回 `fileUrl`

**Step 2: Run test to verify it fails**

Run: `node --test wechat-native/tests/submission-upload.test.js`

Expected: FAIL，因为上传服务方法尚未实现。

**Step 3: Write minimal implementation**

- 在 `wechat-native/services/submission.js` 中新增上传方法
- 统一解析 `ApiResponse`

**Step 4: Run test to verify it passes**

Run: `node --test wechat-native/tests/submission-upload.test.js`

Expected: PASS。

**Step 5: Commit**

```bash
git add wechat-native/services/submission.js wechat-native/tests/submission-upload.test.js
git commit -m "test: cover miniapp submission upload service"
```

### Task 3: 为结果页按比赛过滤写失败测试

**Files:**
- Create: `wechat-native/utils/result.js`
- Create: `wechat-native/tests/result-utils.test.js`
- Modify: `wechat-native/pages/competition/result/index.js`

**Step 1: Write the failing test**

- 测试 `competitionId = 0` 时保留全部结果
- 测试 `competitionId > 0` 时只保留当前比赛结果
- 测试映射后仍保留比赛标题、名次文案和发布时间文案

**Step 2: Run test to verify it fails**

Run: `node --test wechat-native/tests/result-utils.test.js`

Expected: FAIL，因为结果过滤工具尚未实现。

**Step 3: Write minimal implementation**

- 新增结果过滤与展示转换工具
- 结果页接入 `competitionId`
- 修正 `requireLogin()` 的 redirect 参数

**Step 4: Run test to verify it passes**

Run: `node --test wechat-native/tests/result-utils.test.js`

Expected: PASS。

**Step 5: Commit**

```bash
git add wechat-native/utils/result.js wechat-native/tests/result-utils.test.js wechat-native/pages/competition/result/index.js
git commit -m "feat: filter miniapp results by competition"
```

### Task 4: 接入小程序作品上传页面

**Files:**
- Modify: `wechat-native/pages/competition/submission/index.js`
- Modify: `wechat-native/pages/competition/submission/index.wxml`

**Step 1: Write the failing test**

- 在现有源码/行为测试中补充断言，要求页面不再依赖手填 URL 作为主入口。

**Step 2: Run test to verify it fails**

Run: `node --test wechat-native/tests/competition-source.test.js`

Expected: FAIL，因为页面仍然要求手填 URL。

**Step 3: Write minimal implementation**

- 新增“选择本地文件”按钮
- 页面状态记录本地文件信息
- 提交时先上传文件，再提交作品记录

**Step 4: Run test to verify it passes**

Run: `node --test wechat-native/tests/competition-source.test.js`

Expected: PASS。

**Step 5: Commit**

```bash
git add wechat-native/pages/competition/submission/index.js wechat-native/pages/competition/submission/index.wxml wechat-native/tests/competition-source.test.js
git commit -m "feat: use local file upload in miniapp submission page"
```

### Task 5: 全量验证与回归检查

**Files:**
- Modify: `docs/plans/2026-03-18-local-upload-and-result-filter-design.md`
- Modify: `docs/plans/2026-03-18-local-upload-and-result-filter-implementation-plan.md`

**Step 1: Run focused backend tests**

Run: `mvn -f server/pom.xml "-Dtest=SubmissionFileUploadApiTest,ReviewTaskNullReviewerApiTest" test`

Expected: PASS。

**Step 2: Run miniapp tests**

Run: `node --test wechat-native/tests/*.test.js`

Expected: PASS。

**Step 3: Run admin-web safety regression**

Run: `pnpm --dir admin-web test`

Expected: PASS。

**Step 4: Run source grep regression**

Run: `rg -n -S "请输入作品文件 URL|fetchStudentOverview\\(session\\.userId\\)|competitionId: 1|https://example.com/work-v1.pptx" wechat-native server/src/main/java`

Expected: 只剩合理引用，不再有上传页默认示例 URL 和比赛页硬编码默认值。

**Step 5: Commit**

```bash
git add docs/plans/2026-03-18-local-upload-and-result-filter-design.md docs/plans/2026-03-18-local-upload-and-result-filter-implementation-plan.md
git commit -m "docs: record local upload and result filter plan"
```
