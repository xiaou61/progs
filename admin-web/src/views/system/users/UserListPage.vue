<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import type { RoleItem } from '@/api/roles'
import { fetchRoles } from '@/api/roles'
import {
  assignUserRole,
  clearUserViolation,
  createUser,
  fetchUsers,
  freezeUser,
  markUserViolation,
  resetUserPassword,
  type UserItem,
  unfreezeUser
} from '@/api/users'
import { validateResetPassword } from '@/utils/role-governance'
import {
  buildCreateUserPayload,
  createEmptyUserCreateForm,
  type UserCreateFormValues,
  validateCreateUserForm
} from '@/utils/user-governance'

const users = ref<UserItem[]>([])
const roles = ref<RoleItem[]>([])
const loading = ref(false)
const actionLoading = ref(false)
const createLoading = ref(false)
const error = ref('')
const success = ref('')
const selectedUserId = ref<number | null>(null)

const governanceForm = reactive({
  roleCode: '',
  freezeReason: '后台复核中，账号暂时停用',
  newPassword: '',
  violationReason: '存在异常行为，已进入人工复核'
})

const createForm = reactive<UserCreateFormValues>(createEmptyUserCreateForm())

const selectedUser = computed(() => users.value.find((user) => user.id === selectedUserId.value) ?? null)
const roleOptions = computed(() => roles.value.map((role) => ({
  code: role.roleCode,
  label: `${role.roleName} / ${role.roleCode}`
})))
const summaryCards = computed(() => {
  const total = users.value.length
  const enabled = users.value.filter((user) => user.status === 'ENABLED').length
  const disabled = users.value.filter((user) => user.status === 'DISABLED').length
  const flagged = users.value.filter((user) => user.violationMarked).length
  return [
    { label: '账号总量', value: `${total}` },
    { label: '正常账号', value: `${enabled}` },
    { label: '冻结账号', value: `${disabled}` },
    { label: '违规标记', value: `${flagged}` }
  ]
})

function clearNotice() {
  error.value = ''
  success.value = ''
}

function resetCreateForm() {
  Object.assign(createForm, createEmptyUserCreateForm())
}

function applySelection(user: UserItem | null) {
  selectedUserId.value = user?.id ?? null
  governanceForm.roleCode = user?.roleCode ?? ''
  governanceForm.newPassword = ''
  governanceForm.violationReason = user?.violationReason?.trim() || '存在异常行为，已进入人工复核'
  clearNotice()
}

function syncSelection(nextUsers: UserItem[]) {
  if (nextUsers.length === 0) {
    applySelection(null)
    return
  }

  const current = nextUsers.find((user) => user.id === selectedUserId.value)
  applySelection(current ?? nextUsers[0] ?? null)
}

async function loadData(preferredUserId?: number | null) {
  loading.value = true
  try {
    const [nextUsers, nextRoles] = await Promise.all([fetchUsers(), fetchRoles()])
    users.value = nextUsers
    roles.value = nextRoles

    if (!nextRoles.some((role) => role.roleCode === createForm.roleCode) && nextRoles.length > 0) {
      createForm.roleCode = nextRoles[0]?.roleCode ?? createForm.roleCode
    }

    if (preferredUserId != null) {
      const preferredUser = nextUsers.find((user) => user.id === preferredUserId) ?? null
      applySelection(preferredUser)
      return
    }

    syncSelection(nextUsers)
  } catch (loadError) {
    error.value = loadError instanceof Error ? loadError.message : '加载用户治理数据失败'
  } finally {
    loading.value = false
  }
}

async function handleFreeze() {
  if (!selectedUser.value) {
    error.value = '请先选择一个用户'
    return
  }

  actionLoading.value = true
  clearNotice()
  try {
    await freezeUser(selectedUser.value.id, governanceForm.freezeReason.trim() || '后台冻结账号')
    success.value = `已冻结 ${selectedUser.value.realName}`
    await loadData(selectedUser.value.id)
  } catch (actionError) {
    error.value = actionError instanceof Error ? actionError.message : '冻结账号失败'
  } finally {
    actionLoading.value = false
  }
}

async function handleUnfreeze() {
  if (!selectedUser.value) {
    error.value = '请先选择一个用户'
    return
  }

  actionLoading.value = true
  clearNotice()
  try {
    await unfreezeUser(selectedUser.value.id)
    success.value = `已恢复 ${selectedUser.value.realName} 的账号`
    await loadData(selectedUser.value.id)
  } catch (actionError) {
    error.value = actionError instanceof Error ? actionError.message : '恢复账号失败'
  } finally {
    actionLoading.value = false
  }
}

async function handleResetPassword() {
  if (!selectedUser.value) {
    error.value = '请先选择一个用户'
    return
  }

  const validationMessage = validateResetPassword(governanceForm.newPassword)
  if (validationMessage) {
    error.value = validationMessage
    return
  }

  actionLoading.value = true
  clearNotice()
  try {
    await resetUserPassword(selectedUser.value.id, governanceForm.newPassword.trim())
    success.value = `已重置 ${selectedUser.value.realName} 的密码`
    governanceForm.newPassword = ''
    await loadData(selectedUser.value.id)
  } catch (actionError) {
    error.value = actionError instanceof Error ? actionError.message : '重置密码失败'
  } finally {
    actionLoading.value = false
  }
}

async function handleAssignRole() {
  if (!selectedUser.value) {
    error.value = '请先选择一个用户'
    return
  }
  if (!governanceForm.roleCode) {
    error.value = '请选择一个角色'
    return
  }

  actionLoading.value = true
  clearNotice()
  try {
    const updatedUser = await assignUserRole(selectedUser.value.id, governanceForm.roleCode)
    success.value = `已将 ${updatedUser.realName} 调整为 ${updatedUser.roleCode}`
    await loadData(updatedUser.id)
  } catch (actionError) {
    error.value = actionError instanceof Error ? actionError.message : '分配角色失败'
  } finally {
    actionLoading.value = false
  }
}

async function handleMarkViolation() {
  if (!selectedUser.value) {
    error.value = '请先选择一个用户'
    return
  }
  if (!governanceForm.violationReason.trim()) {
    error.value = '请输入违规原因'
    return
  }

  actionLoading.value = true
  clearNotice()
  try {
    await markUserViolation(selectedUser.value.id, governanceForm.violationReason.trim())
    success.value = `已为 ${selectedUser.value.realName} 添加违规标记`
    await loadData(selectedUser.value.id)
  } catch (actionError) {
    error.value = actionError instanceof Error ? actionError.message : '违规标记失败'
  } finally {
    actionLoading.value = false
  }
}

async function handleClearViolation() {
  if (!selectedUser.value) {
    error.value = '请先选择一个用户'
    return
  }
  if (!governanceForm.violationReason.trim()) {
    error.value = '请输入解除说明'
    return
  }

  actionLoading.value = true
  clearNotice()
  try {
    await clearUserViolation(selectedUser.value.id, governanceForm.violationReason.trim())
    success.value = `已解除 ${selectedUser.value.realName} 的违规标记`
    await loadData(selectedUser.value.id)
  } catch (actionError) {
    error.value = actionError instanceof Error ? actionError.message : '解除违规标记失败'
  } finally {
    actionLoading.value = false
  }
}

async function handleCreateUser() {
  clearNotice()
  const validationMessage = validateCreateUserForm(createForm)
  if (validationMessage) {
    error.value = validationMessage
    return
  }

  createLoading.value = true
  try {
    const createdUser = await createUser(buildCreateUserPayload(createForm))
    success.value = `已创建账号 ${createdUser.studentNo}`
    resetCreateForm()
    await loadData(createdUser.id)
  } catch (createError) {
    error.value = createError instanceof Error ? createError.message : '创建用户失败'
  } finally {
    createLoading.value = false
  }
}

function statusLabel(status: string) {
  if (status === 'DISABLED') {
    return '已冻结'
  }
  if (status === 'CANCELLED') {
    return '已注销'
  }
  if (status === 'DELETED') {
    return '已删除'
  }
  return '正常'
}

onMounted(() => {
  void loadData()
})
</script>

<template>
  <main class="page">
    <header class="header">
      <div>
        <p class="eyebrow">System Governance</p>
        <h1>用户治理工作台</h1>
        <p>账号开通、角色调整、冻结恢复、密码重置和违规治理都收敛在这里，方便管理员一站式处理。</p>
      </div>
      <button class="ghost-button" type="button" :disabled="loading" @click="loadData(selectedUserId)">
        {{ loading ? '刷新中...' : '刷新数据' }}
      </button>
    </header>

    <section class="summary-grid">
      <article v-for="item in summaryCards" :key="item.label" class="summary-card">
        <strong>{{ item.value }}</strong>
        <span>{{ item.label }}</span>
      </article>
    </section>

    <p v-if="error" class="message error-message">{{ error }}</p>
    <p v-if="success" class="message success-message">{{ success }}</p>

    <section class="layout">
      <section class="panel table-panel">
        <div class="panel-head">
          <div>
            <h2>用户列表</h2>
            <p>{{ users.length }} 个账号，点击行切换右侧治理对象。</p>
          </div>
          <span class="muted">{{ loading ? '正在拉取最新数据' : '支持账号开通后立即选中处理' }}</span>
        </div>

        <table class="table">
          <thead>
            <tr>
              <th>学号</th>
              <th>姓名</th>
              <th>角色</th>
              <th>违规</th>
              <th>状态</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="user in users"
              :key="user.id"
              :class="{ active: user.id === selectedUserId }"
              @click="applySelection(user)"
            >
              <td>
                <strong>{{ user.studentNo }}</strong>
                <span class="sub-text">{{ user.phone }}</span>
              </td>
              <td>{{ user.realName }}</td>
              <td>{{ user.roleCode }}</td>
              <td>
                <span class="flag-chip" :class="{ flagged: user.violationMarked }">
                  {{ user.violationMarked ? '已标记' : '正常' }}
                </span>
              </td>
              <td>
                <span class="status-chip" :class="user.status.toLowerCase()">
                  {{ statusLabel(user.status) }}
                </span>
              </td>
            </tr>
            <tr v-if="!loading && users.length === 0">
              <td colspan="5" class="empty-cell">暂无用户数据</td>
            </tr>
          </tbody>
        </table>
      </section>

      <aside class="side-column">
        <section class="panel detail-panel">
          <div class="panel-head compact-head">
            <div>
              <h2>{{ selectedUser ? `${selectedUser.realName} 的治理面板` : '请选择一个用户' }}</h2>
              <p v-if="selectedUser">学号 {{ selectedUser.studentNo }} · 当前角色 {{ selectedUser.roleCode }}</p>
            </div>
          </div>

          <template v-if="selectedUser">
            <dl class="profile-grid">
              <div class="profile-card">
                <dt>手机号</dt>
                <dd>{{ selectedUser.phone }}</dd>
              </div>
              <div class="profile-card">
                <dt>账号状态</dt>
                <dd>{{ statusLabel(selectedUser.status) }}</dd>
              </div>
              <div class="profile-card">
                <dt>违规标记</dt>
                <dd>{{ selectedUser.violationMarked ? '已标记' : '未标记' }}</dd>
              </div>
              <div class="profile-card">
                <dt>治理重点</dt>
                <dd>{{ selectedUser.violationReason || '当前无违规说明' }}</dd>
              </div>
            </dl>

            <div v-if="selectedUser.violationMarked" class="notice-card danger-notice">
              <strong>当前处于违规观察状态</strong>
              <p>{{ selectedUser.violationReason || '请补充治理说明' }}</p>
            </div>

            <div class="field">
              <label>角色分配</label>
              <select v-model="governanceForm.roleCode">
                <option disabled value="">请选择角色</option>
                <option v-for="role in roleOptions" :key="role.code" :value="role.code">
                  {{ role.label }}
                </option>
              </select>
              <button class="primary-button" type="button" :disabled="actionLoading" @click="handleAssignRole">
                {{ actionLoading ? '处理中...' : '更新角色' }}
              </button>
            </div>

            <div class="field">
              <label>违规治理说明</label>
              <textarea
                v-model="governanceForm.violationReason"
                rows="3"
                placeholder="请输入违规原因或解除说明，便于留痕"
              />
              <div class="action-row">
                <button class="warning-button" type="button" :disabled="actionLoading" @click="handleMarkViolation">
                  {{ actionLoading ? '处理中...' : '标记违规' }}
                </button>
                <button class="secondary-button" type="button" :disabled="actionLoading" @click="handleClearViolation">
                  {{ actionLoading ? '处理中...' : '解除违规' }}
                </button>
              </div>
            </div>

            <div class="field">
              <label>冻结原因</label>
              <textarea
                v-model="governanceForm.freezeReason"
                rows="3"
                placeholder="请输入冻结原因，便于运营留痕"
              />
            </div>

            <div class="action-row">
              <button
                v-if="selectedUser.status !== 'DISABLED'"
                class="danger-button"
                type="button"
                :disabled="actionLoading"
                @click="handleFreeze"
              >
                {{ actionLoading ? '处理中...' : '冻结账号' }}
              </button>
              <button
                v-else
                class="secondary-button"
                type="button"
                :disabled="actionLoading"
                @click="handleUnfreeze"
              >
                {{ actionLoading ? '处理中...' : '恢复账号' }}
              </button>
            </div>

            <div class="field">
              <label>重置密码</label>
              <input v-model="governanceForm.newPassword" type="password" placeholder="请输入 8 位及以上新密码" />
              <button class="primary-button" type="button" :disabled="actionLoading" @click="handleResetPassword">
                {{ actionLoading ? '处理中...' : '重置密码' }}
              </button>
            </div>
          </template>

          <p v-else class="empty-detail">从左侧选择一个用户后，这里会显示具体治理动作。</p>
        </section>

        <section class="panel create-panel">
          <div class="panel-head compact-head">
            <div>
              <h2>开通新账号</h2>
              <p>账号由后台开通，用户端只负责登录和后续资料维护。</p>
            </div>
            <button class="ghost-button small-button" type="button" @click="resetCreateForm">重置表单</button>
          </div>

          <div class="field-grid">
            <label class="field">
              <span>学号</span>
              <input v-model="createForm.studentNo" type="text" placeholder="例如 S20260123" />
            </label>
            <label class="field">
              <span>姓名</span>
              <input v-model="createForm.realName" type="text" placeholder="请输入姓名" />
            </label>
            <label class="field">
              <span>手机号</span>
              <input v-model="createForm.phone" type="text" placeholder="请输入手机号" />
            </label>
            <label class="field">
              <span>角色</span>
              <select v-model="createForm.roleCode">
                <option v-for="role in roleOptions" :key="role.code" :value="role.code">
                  {{ role.label }}
                </option>
              </select>
            </label>
          </div>

          <label class="field">
            <span>初始密码</span>
            <input v-model="createForm.password" type="password" placeholder="请输入 8 位及以上初始密码" />
          </label>

          <button class="primary-button full-button" type="button" :disabled="createLoading" @click="handleCreateUser">
            {{ createLoading ? '创建中...' : '创建用户账号' }}
          </button>
        </section>
      </aside>
    </section>
  </main>
</template>

<style scoped>
.page {
  min-height: 100vh;
  padding: 40px;
  background:
    radial-gradient(circle at top right, rgba(222, 133, 64, 0.12), transparent 22%),
    linear-gradient(180deg, #f8f1e8 0%, #eef4f8 100%);
}

.header {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  align-items: flex-start;
  margin-bottom: 20px;
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
.muted,
.empty-detail,
.notice-card p,
.summary-card span {
  margin: 8px 0 0;
  color: #5a6878;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.summary-card,
.panel {
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 24px 60px rgba(32, 48, 66, 0.08);
}

.summary-card {
  padding: 22px 24px;
}

.summary-card strong {
  display: block;
  margin-bottom: 8px;
  color: #173149;
  font-size: 34px;
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
  grid-template-columns: minmax(0, 1.3fr) minmax(360px, 0.95fr);
  gap: 20px;
  align-items: start;
}

.side-column {
  display: grid;
  gap: 20px;
}

.table-panel {
  overflow: hidden;
}

.detail-panel,
.create-panel {
  padding: 24px;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
  padding: 24px 24px 16px;
}

.compact-head {
  padding: 0 0 18px;
}

.table {
  width: 100%;
  border-collapse: collapse;
}

th,
td {
  padding: 16px 24px;
  text-align: left;
  border-top: 1px solid #edf0f3;
  vertical-align: top;
}

tbody tr {
  cursor: pointer;
  transition: background-color 0.18s ease;
}

tbody tr:hover {
  background: rgba(245, 236, 226, 0.74);
}

tbody tr.active {
  background: linear-gradient(90deg, rgba(240, 224, 208, 0.6), rgba(255, 255, 255, 0.9));
}

.sub-text {
  display: block;
  margin-top: 6px;
  color: #687789;
  font-size: 13px;
}

.status-chip,
.flag-chip {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 600;
}

.status-chip.enabled {
  background: rgba(45, 133, 91, 0.12);
  color: #17603d;
}

.status-chip.disabled,
.status-chip.cancelled,
.status-chip.deleted {
  background: rgba(194, 62, 46, 0.12);
  color: #9f2e21;
}

.flag-chip {
  background: rgba(96, 114, 132, 0.12);
  color: #4d6072;
}

.flag-chip.flagged {
  background: rgba(177, 74, 47, 0.14);
  color: #9f2e21;
}

.profile-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
  margin: 0 0 20px;
}

.profile-card {
  padding: 16px;
  border-radius: 18px;
  background: #f8fafc;
}

dt {
  margin-bottom: 8px;
  color: #8b98a8;
  font-size: 13px;
}

dd {
  margin: 0;
  color: #1d2a3a;
  font-weight: 600;
  line-height: 1.6;
}

.notice-card {
  margin-bottom: 18px;
  padding: 16px 18px;
  border-radius: 18px;
}

.notice-card strong {
  display: block;
  color: #1d2a3a;
}

.danger-notice {
  background: rgba(194, 62, 46, 0.1);
}

.field-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.field {
  display: grid;
  gap: 10px;
  margin-bottom: 18px;
}

.field span,
label {
  color: #3a4655;
  font-weight: 600;
}

input,
select,
textarea {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid #d9e1e8;
  border-radius: 14px;
  background: #fbfdff;
  color: #1d2a3a;
  font: inherit;
  box-sizing: border-box;
}

textarea {
  resize: vertical;
}

.action-row {
  display: flex;
  gap: 12px;
  margin-bottom: 18px;
}

button {
  border: none;
  border-radius: 14px;
  padding: 11px 16px;
  font: inherit;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

button:hover:enabled {
  transform: translateY(-1px);
}

button:disabled {
  cursor: not-allowed;
  opacity: 0.7;
}

.ghost-button {
  background: rgba(255, 255, 255, 0.9);
  color: #334154;
  box-shadow: 0 14px 30px rgba(32, 48, 66, 0.08);
}

.small-button {
  padding-inline: 14px;
}

.primary-button {
  background: linear-gradient(135deg, #cc6d37 0%, #e58f45 100%);
  color: #fff;
}

.secondary-button {
  background: rgba(23, 96, 61, 0.12);
  color: #17603d;
}

.warning-button {
  background: rgba(181, 114, 38, 0.14);
  color: #9a5d16;
}

.danger-button {
  background: rgba(194, 62, 46, 0.12);
  color: #9f2e21;
}

.full-button {
  width: 100%;
}

.empty-cell {
  text-align: center;
  color: #7b8796;
}

@media (max-width: 1180px) {
  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .page {
    padding: 24px;
  }

  .header,
  .panel-head,
  .profile-grid,
  .field-grid {
    grid-template-columns: 1fr;
    display: grid;
  }

  th,
  td {
    padding: 14px 16px;
  }
}
</style>
