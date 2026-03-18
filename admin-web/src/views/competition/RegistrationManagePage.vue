<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { fetchManagedCompetitions, type CompetitionManageItem } from '@/api/competition-manage'
import {
  fetchCompetitionRegistrations,
  manualAddRegistration,
  markRegistrationAttendance,
  rejectRegistration,
  type RegistrationManageItem
} from '@/api/registration-manage'

const competitions = ref<CompetitionManageItem[]>([])
const registrations = ref<RegistrationManageItem[]>([])
const selectedCompetitionId = ref<number | null>(null)
const competitionLoading = ref(false)
const registrationLoading = ref(false)
const actionLoading = ref(false)
const error = ref('')
const success = ref('')

const manualForm = reactive({
  userId: 0,
  remark: ''
})

const selectedCompetition = computed(() =>
  competitions.value.find((item) => item.id === selectedCompetitionId.value) ?? null
)

const registeredCount = computed(() =>
  registrations.value.filter((item) => item.status === 'REGISTERED').length
)

const presentCount = computed(() =>
  registrations.value.filter((item) => item.attendanceStatus === 'PRESENT').length
)

const absentCount = computed(() =>
  registrations.value.filter((item) => item.attendanceStatus === 'ABSENT').length
)

function clearNotice() {
  error.value = ''
  success.value = ''
}

function resolveStatusLabel(status: RegistrationManageItem['status']) {
  if (status === 'REGISTERED') {
    return '已报名'
  }
  if (status === 'CANCELLED') {
    return '已取消'
  }
  return '已驳回'
}

function resolveAttendanceLabel(attendanceStatus: RegistrationManageItem['attendanceStatus']) {
  if (attendanceStatus === 'PRESENT') {
    return '已到场'
  }
  if (attendanceStatus === 'ABSENT') {
    return '已缺席'
  }
  return '待签到'
}

async function loadCompetitions(preferId?: number | null) {
  competitionLoading.value = true
  try {
    competitions.value = await fetchManagedCompetitions()
    const nextCompetitionId = preferId ?? selectedCompetitionId.value ?? competitions.value[0]?.id ?? null
    selectedCompetitionId.value = nextCompetitionId
    if (nextCompetitionId) {
      await loadRegistrations(nextCompetitionId)
    } else {
      registrations.value = []
    }
  } catch (loadError) {
    error.value = loadError instanceof Error ? loadError.message : '加载比赛列表失败'
  } finally {
    competitionLoading.value = false
  }
}

async function loadRegistrations(competitionId = selectedCompetitionId.value) {
  if (!competitionId) {
    registrations.value = []
    return
  }

  registrationLoading.value = true
  clearNotice()
  try {
    registrations.value = await fetchCompetitionRegistrations(competitionId)
  } catch (loadError) {
    error.value = loadError instanceof Error ? loadError.message : '加载报名记录失败'
  } finally {
    registrationLoading.value = false
  }
}

async function handleCompetitionChange() {
  clearNotice()
  await loadRegistrations()
}

async function handleManualAdd() {
  clearNotice()
  if (!selectedCompetitionId.value) {
    error.value = '请先选择比赛'
    return
  }
  if (manualForm.userId <= 0) {
    error.value = '请填写有效的用户编号'
    return
  }

  actionLoading.value = true
  try {
    const registrationId = await manualAddRegistration({
      competitionId: selectedCompetitionId.value,
      userId: manualForm.userId,
      remark: manualForm.remark
    })
    success.value = `补录成功，记录编号 #${registrationId}`
    manualForm.userId = 0
    manualForm.remark = ''
    await loadRegistrations(selectedCompetitionId.value)
  } catch (submitError) {
    error.value = submitError instanceof Error ? submitError.message : '手动补录失败'
  } finally {
    actionLoading.value = false
  }
}

async function handleReject(registrationId: number) {
  clearNotice()
  const reason = globalThis.window?.prompt?.('请输入驳回原因', '资料不完整')?.trim()
  if (!reason) {
    return
  }

  actionLoading.value = true
  try {
    await rejectRegistration(registrationId, reason)
    success.value = `报名记录 #${registrationId} 已驳回`
    await loadRegistrations(selectedCompetitionId.value)
  } catch (submitError) {
    error.value = submitError instanceof Error ? submitError.message : '驳回报名失败'
  } finally {
    actionLoading.value = false
  }
}

async function handleAttendance(registrationId: number, attendanceStatus: 'PRESENT' | 'ABSENT') {
  clearNotice()
  actionLoading.value = true
  try {
    await markRegistrationAttendance(registrationId, attendanceStatus)
    success.value = `报名记录 #${registrationId} 已更新为${attendanceStatus === 'PRESENT' ? '到场' : '缺席'}`
    await loadRegistrations(selectedCompetitionId.value)
  } catch (submitError) {
    error.value = submitError instanceof Error ? submitError.message : '更新到场状态失败'
  } finally {
    actionLoading.value = false
  }
}

onMounted(() => {
  void loadCompetitions()
})
</script>

<template>
  <main class="page">
    <header class="header">
      <div>
        <p class="eyebrow">Registration Manage</p>
        <h1>报名与参赛管理</h1>
        <p>后台可直接查看报名记录、手动补录、驳回报名，并完成到场/缺席标记。</p>
      </div>
      <RouterLink class="nav-link" to="/competition/editor">返回比赛发布页</RouterLink>
    </header>

    <section class="layout">
      <section class="panel sidebar-panel">
        <div class="panel-head">
          <div>
            <h2>选择比赛</h2>
            <p>先选比赛，再处理报名记录。</p>
          </div>
          <button class="ghost-button" type="button" @click="loadCompetitions()">刷新</button>
        </div>

        <div v-if="competitionLoading" class="state-box">
          正在加载比赛列表...
        </div>
        <div v-else-if="competitions.length === 0" class="state-box">
          还没有可管理的比赛，请先去比赛发布页创建。
        </div>
        <div v-else class="competition-selector">
          <label class="field">
            <span>当前比赛</span>
            <select v-model.number="selectedCompetitionId" @change="handleCompetitionChange">
              <option v-for="item in competitions" :key="item.id" :value="item.id">
                #{{ item.id }} {{ item.title }}
              </option>
            </select>
          </label>

          <div v-if="selectedCompetition" class="summary-card">
            <strong>{{ selectedCompetition.title }}</strong>
            <p>{{ selectedCompetition.description }}</p>
            <span>状态：{{ selectedCompetition.status }} · 名额 {{ selectedCompetition.quota }}</span>
          </div>
        </div>

        <div class="stats-grid">
          <div class="stat-card">
            <strong>{{ registrations.length }}</strong>
            <span>总报名数</span>
          </div>
          <div class="stat-card">
            <strong>{{ registeredCount }}</strong>
            <span>有效报名</span>
          </div>
          <div class="stat-card">
            <strong>{{ presentCount }}</strong>
            <span>已到场</span>
          </div>
          <div class="stat-card">
            <strong>{{ absentCount }}</strong>
            <span>已缺席</span>
          </div>
        </div>

        <div class="manual-card">
          <div class="panel-head compact-head">
            <div>
              <h2>手动补录</h2>
              <p>适合老师现场补录或后台补加参赛人。</p>
            </div>
          </div>
          <label class="field">
            <span>用户编号</span>
            <input v-model.number="manualForm.userId" type="number" min="1" placeholder="请输入学生用户编号" />
          </label>
          <label class="field">
            <span>备注</span>
            <textarea v-model="manualForm.remark" placeholder="例如：后台补录、现场补录"></textarea>
          </label>
          <button class="primary-button" type="button" :disabled="actionLoading" @click="handleManualAdd">
            {{ actionLoading ? '处理中...' : '执行补录' }}
          </button>
        </div>
      </section>

      <section class="panel table-panel">
        <div class="panel-head">
          <div>
            <h2>报名记录</h2>
            <p>驳回后会失效，到场标记仅对有效报名开放。</p>
          </div>
        </div>

        <p v-if="error" class="message error-message">{{ error }}</p>
        <p v-if="success" class="message success-message">{{ success }}</p>

        <div v-if="registrationLoading" class="state-box">
          正在加载报名记录...
        </div>
        <div v-else-if="registrations.length === 0" class="state-box">
          当前比赛还没有报名记录。
        </div>
        <div v-else class="registration-list">
          <article v-for="item in registrations" :key="item.id" class="registration-card">
            <div class="registration-head">
              <div>
                <strong>记录 #{{ item.id }}</strong>
                <p>用户 #{{ item.userId }} · {{ resolveStatusLabel(item.status) }}</p>
              </div>
              <span class="attendance-badge" :class="`attendance-${item.attendanceStatus.toLowerCase()}`">
                {{ resolveAttendanceLabel(item.attendanceStatus) }}
              </span>
            </div>
            <p class="remark-line">
              备注：{{ item.remark || '暂无备注' }}
            </p>
            <div class="action-row">
              <button
                class="ghost-button"
                type="button"
                :disabled="actionLoading || item.status !== 'REGISTERED'"
                @click="handleReject(item.id)"
              >
                驳回报名
              </button>
              <button
                class="secondary-button"
                type="button"
                :disabled="actionLoading || item.status !== 'REGISTERED'"
                @click="handleAttendance(item.id, 'PRESENT')"
              >
                标记到场
              </button>
              <button
                class="danger-button"
                type="button"
                :disabled="actionLoading || item.status !== 'REGISTERED'"
                @click="handleAttendance(item.id, 'ABSENT')"
              >
                标记缺席
              </button>
            </div>
          </article>
        </div>
      </section>
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

.header,
.panel-head,
.registration-head,
.action-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.header {
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
h2,
strong {
  margin: 0;
  color: #1d2a3a;
}

.header p:last-child,
.panel-head p,
.summary-card p,
.state-box,
.remark-line {
  color: #536274;
}

.nav-link {
  padding: 12px 18px;
  border-radius: 999px;
  background: #eef3f8;
  color: #45586c;
  text-decoration: none;
}

.layout {
  display: grid;
  grid-template-columns: minmax(320px, 360px) minmax(0, 1fr);
  gap: 24px;
  align-items: start;
}

.panel,
.summary-card,
.stat-card,
.manual-card,
.registration-card {
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 24px 60px rgba(32, 48, 66, 0.08);
}

.panel {
  padding: 24px;
}

.sidebar-panel {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.competition-selector,
.stats-grid,
.registration-list {
  display: grid;
  gap: 16px;
}

.stats-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.state-box,
.summary-card,
.stat-card,
.manual-card,
.registration-card {
  padding: 18px;
}

.summary-card span,
.stat-card span {
  display: block;
  margin-top: 8px;
}

.stat-card strong {
  display: block;
  font-size: 28px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.field span {
  font-size: 14px;
  color: #233243;
}

input,
textarea,
select,
button {
  font: inherit;
}

input,
textarea,
select {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid #d7dee6;
  border-radius: 14px;
  background: #fff;
}

textarea {
  min-height: 88px;
  resize: vertical;
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

.registration-head {
  margin-bottom: 10px;
}

.registration-head p {
  margin: 8px 0 0;
  color: #536274;
}

.attendance-badge {
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 12px;
}

.attendance-pending {
  background: #eef3f8;
  color: #45586c;
}

.attendance-present {
  background: #eef8f1;
  color: #2f7a49;
}

.attendance-absent {
  background: #fff1ed;
  color: #b14a2f;
}

.remark-line {
  margin: 0 0 14px;
}

.compact-head {
  margin-bottom: 14px;
}

@media (max-width: 1080px) {
  .layout {
    grid-template-columns: 1fr;
  }
}
</style>
