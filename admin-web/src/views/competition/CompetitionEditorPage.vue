<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { publishCompetition, type CompetitionParticipantType } from '@/api/competition'
import {
  fetchManagedCompetitions,
  offlineCompetition,
  saveCompetitionDraft,
  updateCompetition,
  updateCompetitionFeature,
  type CompetitionManageItem
} from '@/api/competition-manage'
import { fetchUsers, type UserItem } from '@/api/users'
import { buildPublishPayload, validateCompetitionForm, type CompetitionFormValues } from '@/utils/competition-form'

const defaultForm = (): CompetitionFormValues => ({
  organizerId: 0,
  title: '',
  description: '',
  signupStartAt: '',
  signupEndAt: '',
  startAt: '',
  endAt: '',
  quota: 100,
  participantType: 'STUDENT_ONLY',
  advisorTeacherId: null
})

const participantTypeOptions: Array<{ value: CompetitionParticipantType; label: string; hint: string }> = [
  {
    value: 'STUDENT_ONLY',
    label: '仅学生参加',
    hint: '报名账号仅限学生，且需要固定绑定一名指导老师。'
  },
  {
    value: 'TEACHER_ONLY',
    label: '仅老师参加',
    hint: '报名账号仅限老师，不需要指定指导老师。'
  }
]

const loading = ref(false)
const listLoading = ref(false)
const featureLoading = ref(false)
const error = ref('')
const success = ref('')
const competitions = ref<CompetitionManageItem[]>([])
const users = ref<UserItem[]>([])
const selectedCompetitionId = ref<number | null>(null)

const form = reactive<CompetitionFormValues>(defaultForm())
const featureForm = reactive({
  recommended: false,
  pinned: false
})

const submitLabel = computed(() => (selectedCompetitionId.value ? '更新比赛' : '发布比赛'))
const hasSelection = computed(() => selectedCompetitionId.value !== null)
const organizerOptions = computed(() =>
  users.value
    .filter((user) => user.status === 'ENABLED' && (user.roleCode === 'TEACHER' || user.roleCode === 'ADMIN'))
    .map((user) => ({
      id: user.id,
      label: `${user.realName} · ${user.studentNo} · ${user.roleCode === 'TEACHER' ? '老师' : '管理员'}`,
      phone: user.phone
    }))
)
const advisorTeacherOptions = computed(() =>
  users.value
    .filter((user) => user.status === 'ENABLED' && user.roleCode === 'TEACHER')
    .map((user) => ({
      id: user.id,
      label: `${user.realName} · ${user.studentNo}`,
      phone: user.phone
    }))
)
const selectedOrganizer = computed(() =>
  organizerOptions.value.find((item) => item.id === form.organizerId) ?? null
)
const selectedAdvisorTeacher = computed(() =>
  advisorTeacherOptions.value.find((item) => item.id === form.advisorTeacherId) ?? null
)
const isStudentOnly = computed(() => form.participantType === 'STUDENT_ONLY')

function clearNotice() {
  error.value = ''
  success.value = ''
}

function normalizeDateTimeForInput(value: string) {
  if (!value) {
    return ''
  }
  const normalized = value.includes(' ') ? value.replace(' ', 'T') : value
  return normalized.slice(0, 16)
}

function applyCompetition(item: CompetitionManageItem) {
  selectedCompetitionId.value = item.id
  form.organizerId = item.organizerId
  form.title = item.title
  form.description = item.description
  form.signupStartAt = normalizeDateTimeForInput(item.signupStartAt)
  form.signupEndAt = normalizeDateTimeForInput(item.signupEndAt)
  form.startAt = normalizeDateTimeForInput(item.startAt)
  form.endAt = normalizeDateTimeForInput(item.endAt)
  form.quota = item.quota
  form.participantType = item.participantType
  form.advisorTeacherId = item.advisorTeacherId ?? null
  featureForm.recommended = item.recommended
  featureForm.pinned = item.pinned
  applyDefaultAdvisorTeacher()
  clearNotice()
}

function resetForm() {
  Object.assign(form, defaultForm())
  featureForm.recommended = false
  featureForm.pinned = false
  selectedCompetitionId.value = null
  applyDefaultOrganizer()
  applyDefaultAdvisorTeacher()
  clearNotice()
}

function applyDefaultOrganizer() {
  if (!form.organizerId && organizerOptions.value.length > 0) {
    form.organizerId = organizerOptions.value[0].id
  }
}

function applyDefaultAdvisorTeacher() {
  if (!isStudentOnly.value) {
    form.advisorTeacherId = null
    return
  }
  if (form.advisorTeacherId && advisorTeacherOptions.value.some((item) => item.id === form.advisorTeacherId)) {
    return
  }
  const organizerAsTeacher = users.value.find(
    (user) => user.id === form.organizerId && user.status === 'ENABLED' && user.roleCode === 'TEACHER'
  )
  form.advisorTeacherId = organizerAsTeacher?.id ?? advisorTeacherOptions.value[0]?.id ?? null
}

function organizerText(organizerId: number) {
  const matched = users.value.find((user) => user.id === organizerId)
  return matched ? `${matched.realName} · ${matched.studentNo}` : '发起人信息待补充'
}

function resolveParticipantTypeLabel(participantType: CompetitionParticipantType) {
  return participantType === 'STUDENT_ONLY' ? '仅学生参加' : '仅老师参加'
}

function resolveAdvisorTeacherText(item: CompetitionManageItem) {
  if (item.participantType !== 'STUDENT_ONLY') {
    return '指导老师：无需指定'
  }
  return `指导老师：${item.advisorTeacherName || '待指定'}`
}

function handleParticipantTypeChange() {
  applyDefaultAdvisorTeacher()
  clearNotice()
}

async function loadPageData(preferId?: number | null) {
  listLoading.value = true
  try {
    const [nextCompetitions, nextUsers] = await Promise.all([fetchManagedCompetitions(), fetchUsers()])
    competitions.value = nextCompetitions
    users.value = nextUsers
    const targetId = preferId ?? selectedCompetitionId.value
    applyDefaultOrganizer()
    applyDefaultAdvisorTeacher()
    if (!targetId) {
      if (competitions.value.length > 0) {
        applyCompetition(competitions.value[0])
        return
      }
      selectedCompetitionId.value = null
      return
    }

    const matched = competitions.value.find((item) => item.id === targetId)
    if (matched) {
      applyCompetition(matched)
      return
    }

    if (competitions.value.length > 0) {
      applyCompetition(competitions.value[0])
      return
    }

    selectedCompetitionId.value = null
  } catch (loadError) {
    error.value = loadError instanceof Error ? loadError.message : '加载比赛管理列表失败'
  } finally {
    listLoading.value = false
  }
}

function assertValidForm() {
  const validationMessage = validateCompetitionForm(form)
  if (validationMessage) {
    error.value = validationMessage
    return false
  }
  return true
}

async function submitCompetition() {
  clearNotice()
  if (!assertValidForm()) {
    return
  }

  loading.value = true
  try {
    const payload = buildPublishPayload(form, featureForm)
    if (selectedCompetitionId.value) {
      await updateCompetition(selectedCompetitionId.value, {
        ...payload,
        status: 'PUBLISHED'
      })
      success.value = `比赛 #${selectedCompetitionId.value} 更新成功`
      await loadPageData(selectedCompetitionId.value)
    } else {
      const competitionId = await publishCompetition(payload)
      success.value = `比赛发布成功，编号 #${competitionId}`
      await loadPageData(competitionId)
    }
  } catch (submitError) {
    error.value = submitError instanceof Error ? submitError.message : '发布或更新比赛失败'
  } finally {
    loading.value = false
  }
}

async function submitDraft() {
  clearNotice()
  if (!assertValidForm()) {
    return
  }

  loading.value = true
  try {
    const payload = buildPublishPayload(form, featureForm)
    if (selectedCompetitionId.value) {
      await updateCompetition(selectedCompetitionId.value, {
        ...payload,
        status: 'DRAFT'
      })
      success.value = `草稿更新成功，编号 #${selectedCompetitionId.value}`
      await loadPageData(selectedCompetitionId.value)
    } else {
      const competitionId = await saveCompetitionDraft(payload)
      success.value = `草稿保存成功，编号 #${competitionId}`
      await loadPageData(competitionId)
    }
  } catch (submitError) {
    error.value = submitError instanceof Error ? submitError.message : '保存草稿失败'
  } finally {
    loading.value = false
  }
}

async function submitFeature() {
  clearNotice()
  if (!selectedCompetitionId.value) {
    error.value = '请先选择一个比赛，再设置推荐和置顶状态'
    return
  }

  featureLoading.value = true
  try {
    await updateCompetitionFeature(selectedCompetitionId.value, {
      recommended: featureForm.recommended,
      pinned: featureForm.pinned
    })
    success.value = `比赛 #${selectedCompetitionId.value} 的展示状态已更新`
    await loadPageData(selectedCompetitionId.value)
  } catch (submitError) {
    error.value = submitError instanceof Error ? submitError.message : '更新推荐置顶状态失败'
  } finally {
    featureLoading.value = false
  }
}

async function submitOffline() {
  clearNotice()
  if (!selectedCompetitionId.value) {
    error.value = '请先选择一个比赛，再执行下架'
    return
  }

  featureLoading.value = true
  try {
    await offlineCompetition(selectedCompetitionId.value)
    success.value = `比赛 #${selectedCompetitionId.value} 已下架`
    await loadPageData(selectedCompetitionId.value)
  } catch (submitError) {
    error.value = submitError instanceof Error ? submitError.message : '下架比赛失败'
  } finally {
    featureLoading.value = false
  }
}

onMounted(() => {
  void loadPageData()
})
</script>

<template>
  <main class="page">
    <header class="header">
      <div>
        <p class="eyebrow">Competition Manage</p>
        <h1>比赛发布与管理</h1>
        <p>这一页现在同时支持发布新比赛、保存草稿、编辑已有比赛、设置推荐/置顶和下架。</p>
      </div>
      <RouterLink class="page-link" to="/competition/registrations">进入报名管理</RouterLink>
    </header>

    <section class="layout">
      <section class="panel form-panel">
        <div class="panel-head">
          <div>
            <h2>{{ hasSelection ? `编辑比赛 #${selectedCompetitionId}` : '新建比赛' }}</h2>
            <p>老师端和后台管理共用同一套真实接口。</p>
          </div>
          <button class="ghost-button" type="button" @click="resetForm">重置为新建</button>
        </div>

        <p v-if="error" class="message error-message">{{ error }}</p>
        <p v-if="success" class="message success-message">{{ success }}</p>

        <div class="field">
          <label>发起人账号</label>
          <select v-model.number="form.organizerId" @change="applyDefaultAdvisorTeacher">
            <option :value="0" disabled>{{ organizerOptions.length ? '请选择发起人' : '暂无可选发起账号' }}</option>
            <option v-for="item in organizerOptions" :key="item.id" :value="item.id">
              {{ item.label }}
            </option>
          </select>
          <p v-if="selectedOrganizer" class="field-hint">联系电话：{{ selectedOrganizer.phone }}</p>
        </div>
        <div class="field">
          <label>比赛名称</label>
          <input v-model="form.title" type="text" placeholder="请输入比赛名称" />
        </div>
        <div class="field">
          <label>比赛说明</label>
          <textarea v-model="form.description" placeholder="请输入比赛说明"></textarea>
        </div>
        <div class="field">
          <label>参赛类型</label>
          <div class="participant-type-grid">
            <label v-for="item in participantTypeOptions" :key="item.value" class="toggle-card option-card">
              <input v-model="form.participantType" type="radio" :value="item.value" @change="handleParticipantTypeChange" />
              <span>{{ item.label }}</span>
              <small>{{ item.hint }}</small>
            </label>
          </div>
        </div>
        <div v-if="isStudentOnly" class="field">
          <label>指导老师</label>
          <select v-model="form.advisorTeacherId">
            <option :value="null" disabled>{{ advisorTeacherOptions.length ? '请选择指导老师' : '暂无可选指导老师' }}</option>
            <option v-for="item in advisorTeacherOptions" :key="item.id" :value="item.id">
              {{ item.label }}
            </option>
          </select>
          <p v-if="selectedAdvisorTeacher" class="field-hint">联系电话：{{ selectedAdvisorTeacher.phone }}</p>
        </div>
        <div class="grid">
          <div class="field">
            <label>报名开始</label>
            <input v-model="form.signupStartAt" type="datetime-local" />
          </div>
          <div class="field">
            <label>报名截止</label>
            <input v-model="form.signupEndAt" type="datetime-local" />
          </div>
          <div class="field">
            <label>比赛开始</label>
            <input v-model="form.startAt" type="datetime-local" />
          </div>
          <div class="field">
            <label>比赛结束</label>
            <input v-model="form.endAt" type="datetime-local" />
          </div>
        </div>
        <div class="field">
          <label>名额</label>
          <input v-model.number="form.quota" type="number" min="1" placeholder="请输入比赛名额" />
        </div>

        <div class="feature-grid">
          <label class="toggle-card">
            <input v-model="featureForm.recommended" type="checkbox" />
            <span>推荐展示</span>
          </label>
          <label class="toggle-card">
            <input v-model="featureForm.pinned" type="checkbox" />
            <span>置顶展示</span>
          </label>
        </div>

        <div class="action-row">
          <button class="secondary-button" :disabled="loading" type="button" @click="submitDraft">
            {{ loading ? '处理中...' : '保存草稿' }}
          </button>
          <button class="primary-button" :disabled="loading" type="button" @click="submitCompetition">
            {{ loading ? '处理中...' : submitLabel }}
          </button>
        </div>
        <div class="action-row">
          <button class="ghost-button" :disabled="featureLoading || !hasSelection" type="button" @click="submitFeature">
            {{ featureLoading ? '处理中...' : '保存推荐/置顶' }}
          </button>
          <button class="danger-button" :disabled="featureLoading || !hasSelection" type="button" @click="submitOffline">
            {{ featureLoading ? '处理中...' : '下架比赛' }}
          </button>
        </div>
        <p class="action-hint">
          {{
            hasSelection
              ? '推荐/置顶和下架针对当前选中的比赛生效。'
              : '推荐/置顶和下架针对当前选中的比赛生效；新建比赛请先发布，或先点击右侧比赛后再操作。'
          }}
        </p>
      </section>

      <aside class="panel list-panel">
        <div class="panel-head">
          <div>
            <h2>比赛管理列表</h2>
            <p>点击任一比赛可载入到左侧编辑。</p>
          </div>
          <button class="ghost-button" type="button" @click="loadPageData()">刷新列表</button>
        </div>

        <div v-if="listLoading" class="state-box">
          正在加载比赛管理列表...
        </div>
        <div v-else-if="competitions.length === 0" class="state-box">
          当前还没有比赛，先在左侧创建一个吧。
        </div>
        <div v-else class="competition-list">
          <button
            v-for="item in competitions"
            :key="item.id"
            class="competition-card"
            :class="{ active: item.id === selectedCompetitionId }"
            type="button"
            @click="applyCompetition(item)"
          >
            <div class="card-head">
              <strong>{{ item.title }}</strong>
              <span class="status-badge">{{ item.status }}</span>
            </div>
            <p class="card-desc">{{ item.description }}</p>
            <p class="card-meta">{{ organizerText(item.organizerId) }} · 名额 {{ item.quota }}</p>
            <p class="card-meta">{{ resolveParticipantTypeLabel(item.participantType) }} · {{ resolveAdvisorTeacherText(item) }}</p>
            <p class="card-meta">推荐：{{ item.recommended ? '是' : '否' }} · 置顶：{{ item.pinned ? '是' : '否' }}</p>
          </button>
        </div>
      </aside>
    </section>
  </main>
</template>

<style scoped>
.page {
  min-height: 100vh;
  padding: 40px;
  background:
    radial-gradient(circle at top right, rgba(255, 191, 122, 0.18), transparent 26%),
    linear-gradient(180deg, #f8f1e8 0%, #eef4f8 100%);
}

.header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 24px;
}

.eyebrow {
  margin: 0 0 8px;
  color: #b1612a;
  text-transform: uppercase;
  letter-spacing: 0.12em;
  font-size: 12px;
}

h1,
h2 {
  margin: 0;
  color: #1d2a3a;
}

.header p:last-child,
.panel-head p,
.card-desc,
.card-meta {
  color: #536274;
}

.page-link {
  padding: 12px 18px;
  border-radius: 999px;
  background: #eef3f8;
  color: #45586c;
  text-decoration: none;
}

.layout {
  display: grid;
  grid-template-columns: minmax(0, 2fr) minmax(320px, 1fr);
  gap: 24px;
  align-items: start;
}

.panel {
  padding: 24px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 24px 60px rgba(32, 48, 66, 0.08);
}

.panel-head {
  display: flex;
  align-items: start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
}

.message {
  margin: 0 0 16px;
  padding: 12px 14px;
  border-radius: 14px;
  font-size: 14px;
}

.error-message {
  background: #fff1ed;
  color: #b14a2f;
}

.success-message {
  background: #eef8f1;
  color: #2f7a49;
}

.grid,
.participant-type-grid,
.feature-grid,
.action-row {
  display: grid;
  gap: 16px;
}

.grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.participant-type-grid,
.feature-grid,
.action-row {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.field {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

.field-hint {
  margin: 0;
  color: #607082;
  font-size: 13px;
}

.action-hint {
  margin: 12px 2px 0;
  color: #607082;
  font-size: 13px;
  line-height: 1.6;
}

label {
  font-size: 14px;
  color: #233243;
}

input,
select,
textarea,
button {
  font: inherit;
}

input,
select,
textarea {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid #d7dee6;
  border-radius: 14px;
  background: #fff;
}

textarea {
  min-height: 120px;
  resize: vertical;
}

.toggle-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 16px;
  border: 1px solid #d7dee6;
  border-radius: 16px;
  color: #233243;
}

.option-card {
  align-items: flex-start;
  flex-direction: column;
}

.option-card small {
  color: #607082;
  line-height: 1.5;
}

.primary-button,
.secondary-button,
.ghost-button,
.danger-button {
  padding: 12px 18px;
  border-radius: 999px;
  border: 0;
  cursor: pointer;
}

.primary-button {
  background: #c86c31;
  color: #fff;
}

.secondary-button {
  background: #f2dfcf;
  color: #9d6432;
}

.ghost-button {
  background: #eef3f8;
  color: #45586c;
}

.danger-button {
  background: #b14a2f;
  color: #fff;
}

.primary-button:disabled,
.secondary-button:disabled,
.ghost-button:disabled,
.danger-button:disabled {
  cursor: not-allowed;
  opacity: 0.68;
}

.state-box {
  padding: 18px;
  border-radius: 16px;
  background: #f5f7fa;
  color: #536274;
}

.competition-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.competition-card {
  width: 100%;
  padding: 16px;
  border: 1px solid #d9e0e8;
  border-radius: 16px;
  background: #fff;
  text-align: left;
}

.competition-card.active {
  border-color: #c86c31;
  box-shadow: 0 12px 24px rgba(200, 108, 49, 0.12);
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.status-badge {
  padding: 4px 10px;
  border-radius: 999px;
  background: #f2dfcf;
  color: #9d6432;
  font-size: 12px;
}

@media (max-width: 1080px) {
  .layout {
    grid-template-columns: 1fr;
  }
}
</style>
