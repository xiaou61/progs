# 微信老师发布时间选择器与作品上传引导改造 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 把微信老师端比赛发布时间从手填改成选择器，并把作品上传页改成更清晰的步骤式提交流程。

**Architecture:** 在 `wechat-native` 中新增轻量时间工具，负责解析、拼装和格式化时间字符串。老师端比赛发布页改用原生日期/时间选择器，学生端作品上传页只调整页面状态与文案结构，不改接口协议。

**Tech Stack:** 微信小程序原生页面、Node `node:test`、现有 `wechat-native` 服务层

---

### Task 1: 补时间工具与页面结构红灯测试

**Files:**
- Create: `wechat-native/tests/datetime-utils.test.js`
- Modify: `wechat-native/tests/teacher-competition-source.test.js`
- Modify: `wechat-native/tests/submission-upload.test.js`

**Step 1: Write the failing test**

```js
test('datetime utilities should parse and build picker values', () => {
  const { splitDateTimeValue, mergeDateTimeValue } = require('../utils/datetime')
  assert.deepEqual(splitDateTimeValue('2026-03-21T09:30:00'), { date: '2026-03-21', time: '09:30' })
  assert.equal(mergeDateTimeValue('2026-03-21', '09:30'), '2026-03-21T09:30:00')
})
```

**Step 2: Run test to verify it fails**

Run: `node --test wechat-native/tests/datetime-utils.test.js wechat-native/tests/teacher-competition-source.test.js wechat-native/tests/submission-upload.test.js`
Expected: FAIL，因为时间工具和新页面文案尚未实现。

**Step 3: Write minimal implementation**

- 新增 `wechat-native/utils/datetime.js`
- 补充老师端与作品上传页源码中对应的结构与文案

**Step 4: Run test to verify it passes**

Run: `node --test wechat-native/tests/datetime-utils.test.js wechat-native/tests/teacher-competition-source.test.js wechat-native/tests/submission-upload.test.js`
Expected: PASS

### Task 2: 改老师端时间选择器

**Files:**
- Modify: `wechat-native/pages/teacher/competition-editor/index.js`
- Modify: `wechat-native/pages/teacher/competition-editor/index.wxml`
- Modify: `wechat-native/pages/teacher/competition-editor/index.wxss`

**Step 1: Write the failing test**

- 复用 Task 1 中老师端源码测试，要求不再出现手填时间示例文案，并出现日期/时间选择器。

**Step 2: Run test to verify it fails**

Run: `node --test wechat-native/tests/teacher-competition-source.test.js`
Expected: FAIL，因为当前页面仍是 `<input>` 手填时间。

**Step 3: Write minimal implementation**

- 使用时间工具维护四个字段的日期和时间子状态
- 新增 `bindDateChange` 和 `bindTimeChange`
- 编辑回显时同步选择器状态
- 提交前自动合并成接口格式

**Step 4: Run test to verify it passes**

Run: `node --test wechat-native/tests/teacher-competition-source.test.js`
Expected: PASS

### Task 3: 改作品上传页步骤引导

**Files:**
- Modify: `wechat-native/pages/competition/submission/index.js`
- Modify: `wechat-native/pages/competition/submission/index.wxml`
- Modify: `wechat-native/pages/competition/submission/index.wxss`

**Step 1: Write the failing test**

- 复用 Task 1 中作品上传源码测试，要求出现三步说明、已选文件摘要和更明确的按钮文案。

**Step 2: Run test to verify it fails**

Run: `node --test wechat-native/tests/submission-upload.test.js`
Expected: FAIL，因为当前页面还没有三步引导。

**Step 3: Write minimal implementation**

- 页面增加三步说明和文件摘要卡片
- 调整主按钮和版本记录说明文案
- 保持上传接口调用逻辑不变

**Step 4: Run test to verify it passes**

Run: `node --test wechat-native/tests/submission-upload.test.js`
Expected: PASS

### Task 4: 全量回归验证

**Files:**
- Verify only

**Step 1: Run focused tests**

Run: `node --test wechat-native/tests/datetime-utils.test.js wechat-native/tests/teacher-competition-source.test.js wechat-native/tests/submission-upload.test.js`
Expected: PASS

**Step 2: Run miniapp related regression tests**

Run: `node --test wechat-native/tests/*.test.js`
Expected: PASS，无新增回归。
