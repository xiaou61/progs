<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { fetchSystemConfig, updateSystemConfig, type SystemConfigSummary } from '@/api/system'

const loading = ref(false)
const saving = ref(false)
const error = ref('')
const success = ref('')

const form = reactive<SystemConfigSummary>({
  platformName: '',
  mvpPhase: '',
  pointsEnabled: true,
  submissionReuploadEnabled: true
})

function clearNotice() {
  error.value = ''
  success.value = ''
}

function applyConfig(config: SystemConfigSummary) {
  form.platformName = config.platformName
  form.mvpPhase = config.mvpPhase
  form.pointsEnabled = config.pointsEnabled
  form.submissionReuploadEnabled = config.submissionReuploadEnabled
}

async function loadConfig() {
  loading.value = true
  error.value = ''
  try {
    const config = await fetchSystemConfig()
    applyConfig(config)
  } catch (loadError) {
    error.value = loadError instanceof Error ? loadError.message : '加载系统配置失败'
  } finally {
    loading.value = false
  }
}

function validateForm() {
  if (!form.platformName.trim()) {
    return '平台名称不能为空'
  }
  if (!form.mvpPhase.trim()) {
    return '当前阶段不能为空'
  }
  return ''
}

async function handleSubmit() {
  clearNotice()
  const validationMessage = validateForm()
  if (validationMessage) {
    error.value = validationMessage
    return
  }

  saving.value = true
  try {
    const updated = await updateSystemConfig({
      platformName: form.platformName.trim(),
      mvpPhase: form.mvpPhase.trim(),
      pointsEnabled: form.pointsEnabled,
      submissionReuploadEnabled: form.submissionReuploadEnabled
    })
    applyConfig(updated)
    success.value = '系统配置已保存'
  } catch (submitError) {
    error.value = submitError instanceof Error ? submitError.message : '保存系统配置失败'
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  void loadConfig()
})
</script>

<template>
  <section class="page">
    <header class="hero">
      <div>
        <p class="eyebrow">Config</p>
        <h1>系统配置</h1>
        <p>这里现在支持直接调整平台名称、当前阶段和核心开关，不再只是展示只读信息。</p>
      </div>
      <button class="ghost-button" type="button" :disabled="loading" @click="loadConfig">
        {{ loading ? '刷新中...' : '刷新配置' }}
      </button>
    </header>

    <p v-if="error" class="message error-message">{{ error }}</p>
    <p v-if="success" class="message success-message">{{ success }}</p>

    <section class="layout">
      <section class="panel summary-panel">
        <article class="config-item">
          <span>当前平台名称</span>
          <strong>{{ form.platformName || '未设置' }}</strong>
        </article>
        <article class="config-item">
          <span>当前阶段</span>
          <strong>{{ form.mvpPhase || '未设置' }}</strong>
        </article>
        <article class="config-item">
          <span>积分体系</span>
          <strong>{{ form.pointsEnabled ? '已开启' : '已关闭' }}</strong>
        </article>
        <article class="config-item">
          <span>作品重传</span>
          <strong>{{ form.submissionReuploadEnabled ? '允许覆盖最新版本' : '关闭' }}</strong>
        </article>
      </section>

      <section class="panel form-panel">
        <div class="panel-head">
          <div>
            <h2>编辑系统配置</h2>
            <p>适合维护 MVP 阶段对外展示口径和运营开关。</p>
          </div>
        </div>

        <label class="field">
          <span>平台名称</span>
          <input v-model="form.platformName" type="text" placeholder="请输入平台名称" />
        </label>

        <label class="field">
          <span>当前阶段</span>
          <input v-model="form.mvpPhase" type="text" placeholder="例如 Phase 2" />
        </label>

        <div class="switch-list">
          <label class="switch-item">
            <div>
              <strong>积分体系</strong>
              <p>控制比赛结果发布后是否同步积分体系。</p>
            </div>
            <input v-model="form.pointsEnabled" type="checkbox" />
          </label>

          <label class="switch-item">
            <div>
              <strong>作品重传</strong>
              <p>控制学生是否允许重复提交并覆盖最新版本。</p>
            </div>
            <input v-model="form.submissionReuploadEnabled" type="checkbox" />
          </label>
        </div>

        <button class="primary-button full-button" type="button" :disabled="saving" @click="handleSubmit">
          {{ saving ? '保存中...' : '保存系统配置' }}
        </button>
      </section>
    </section>
  </section>
</template>

<style scoped>
.page {
  min-height: 100%;
  padding: 32px;
  color: #1f2f3c;
}

.hero {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
  margin-bottom: 20px;
}

.eyebrow {
  margin: 0 0 12px;
  color: #9f6230;
  font-size: 14px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.hero h1,
h2 {
  margin: 0 0 12px;
  font-size: 36px;
}

.hero p:last-child,
.panel-head p,
.config-item span,
.switch-item p {
  max-width: 760px;
  margin: 0;
  color: #56697a;
  line-height: 1.7;
}

.ghost-button {
  border: none;
  border-radius: 999px;
  padding: 12px 18px;
  background: #eef3f7;
  color: #314657;
  font: inherit;
  cursor: pointer;
}

.message {
  margin: 0 0 16px;
  padding: 12px 14px;
  border-radius: 14px;
  font-size: 14px;
}

.error-message {
  background: rgba(194, 62, 46, 0.12);
  color: #9f2e21;
}

.success-message {
  background: rgba(45, 133, 91, 0.12);
  color: #17603d;
}

.layout {
  display: grid;
  grid-template-columns: minmax(0, 0.9fr) minmax(320px, 1.1fr);
  gap: 18px;
}

.panel {
  padding: 24px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 24px 60px rgba(33, 50, 68, 0.08);
}

.summary-panel {
  display: grid;
  gap: 16px;
}

.config-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 22px 24px;
  border-radius: 20px;
  background: #f8fafc;
}

.config-item strong,
.switch-item strong {
  color: #9f6230;
}

.panel-head {
  margin-bottom: 20px;
}

.field {
  display: grid;
  gap: 10px;
  margin-bottom: 18px;
}

.field span {
  color: #3a4655;
  font-weight: 600;
}

input[type="text"] {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid #d9e1e8;
  border-radius: 14px;
  background: #fbfdff;
  color: #1d2a3a;
  font: inherit;
  box-sizing: border-box;
}

.switch-list {
  display: grid;
  gap: 14px;
  margin-bottom: 20px;
}

.switch-item {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
  padding: 18px;
  border-radius: 18px;
  background: #f8fafc;
}

.switch-item input {
  width: 22px;
  height: 22px;
}

.primary-button {
  border: none;
  border-radius: 14px;
  padding: 11px 16px;
  background: linear-gradient(135deg, #cc6d37 0%, #e58f45 100%);
  color: #fff;
  font: inherit;
  font-weight: 600;
  cursor: pointer;
}

.full-button {
  width: 100%;
}

@media (max-width: 980px) {
  .layout {
    grid-template-columns: 1fr;
  }
}
</style>
