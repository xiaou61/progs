<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import type { RoleItem } from '@/api/roles'
import { fetchRoles } from '@/api/roles'
import {
  assignUserRole,
  fetchUsers,
  freezeUser,
  resetUserPassword,
  type UserItem,
  unfreezeUser
} from '@/api/users'
import { validateResetPassword } from '@/utils/role-governance'

const users = ref<UserItem[]>([])
const roles = ref<RoleItem[]>([])
const loading = ref(false)
const actionLoading = ref(false)
const error = ref('')
const success = ref('')
const selectedUserId = ref<number | null>(null)

const form = reactive({
  roleCode: '',
  freezeReason: '后台复核中，账号暂时停用',
  newPassword: ''
})

const selectedUser = computed(() => users.value.find((user) => user.id === selectedUserId.value) ?? null)

function clearNotice() {
  error.value = ''
  success.value = ''
}

function applySelection(user: UserItem | null) {
  selectedUserId.value = user?.id ?? null
  form.roleCode = user?.roleCode ?? ''
  form.newPassword = ''
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
    await freezeUser(selectedUser.value.id, form.freezeReason.trim() || '后台冻结账号')
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

  const validationMessage = validateResetPassword(form.newPassword)
  if (validationMessage) {
    error.value = validationMessage
    return
  }

  actionLoading.value = true
  clearNotice()
  try {
    await resetUserPassword(selectedUser.value.id, form.newPassword.trim())
    success.value = `已重置 ${selectedUser.value.realName} 的密码`
    form.newPassword = ''
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

  if (!form.roleCode) {
    error.value = '请选择一个角色'
    return
  }

  actionLoading.value = true
  clearNotice()
  try {
    const updatedUser = await assignUserRole(selectedUser.value.id, form.roleCode)
    success.value = `已将 ${updatedUser.realName} 调整为 ${updatedUser.roleCode}`
    await loadData(updatedUser.id)
  } catch (actionError) {
    error.value = actionError instanceof Error ? actionError.message : '分配角色失败'
  } finally {
    actionLoading.value = false
  }
}

function statusLabel(status: string) {
  if (status === 'DISABLED') {
    return '已冻结'
  }
  if (status === 'DELETED') {
    return '已注销'
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
        <h1>用户治理</h1>
        <p>这里现在接的是后台真实接口，可以直接查看用户状态、调整角色、冻结账号和重置密码。</p>
      </div>
      <button class="ghost-button" type="button" :disabled="loading" @click="loadData(selectedUserId)">
        {{ loading ? '刷新中...' : '刷新列表' }}
      </button>
    </header>

    <p v-if="error" class="message error-message">{{ error }}</p>
    <p v-if="success" class="message success-message">{{ success }}</p>

    <section class="layout">
      <section class="panel table-panel">
        <div class="panel-head">
          <div>
            <h2>用户列表</h2>
            <p>{{ users.length }} 个账号</p>
          </div>
          <span class="muted">{{ loading ? '正在拉取最新数据' : '点击行可切换右侧操作对象' }}</span>
        </div>

        <table class="table">
          <thead>
            <tr>
              <th>学号</th>
              <th>姓名</th>
              <th>角色</th>
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
                <span class="status-chip" :class="user.status.toLowerCase()">
                  {{ statusLabel(user.status) }}
                </span>
              </td>
            </tr>
            <tr v-if="!loading && users.length === 0">
              <td colspan="4" class="empty-cell">暂无用户数据</td>
            </tr>
          </tbody>
        </table>
      </section>

      <section class="panel detail-panel">
        <div class="panel-head">
          <div>
            <h2>{{ selectedUser ? `${selectedUser.realName} 的治理面板` : '请选择一个用户' }}</h2>
            <p v-if="selectedUser">学号 {{ selectedUser.studentNo }} · 当前角色 {{ selectedUser.roleCode }}</p>
          </div>
        </div>

        <template v-if="selectedUser">
          <dl class="profile-grid">
            <div>
              <dt>手机号</dt>
              <dd>{{ selectedUser.phone }}</dd>
            </div>
            <div>
              <dt>账号状态</dt>
              <dd>{{ statusLabel(selectedUser.status) }}</dd>
            </div>
          </dl>

          <div class="field">
            <label>角色分配</label>
            <select v-model="form.roleCode">
              <option disabled value="">请选择角色</option>
              <option v-for="role in roles" :key="role.roleCode" :value="role.roleCode">
                {{ role.roleName }} / {{ role.roleCode }}
              </option>
            </select>
            <button class="primary-button" type="button" :disabled="actionLoading" @click="handleAssignRole">
              {{ actionLoading ? '提交中...' : '更新角色' }}
            </button>
          </div>

          <div class="field">
            <label>冻结原因</label>
            <textarea v-model="form.freezeReason" rows="3" placeholder="请输入冻结原因，便于运营留痕"></textarea>
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
            <input v-model="form.newPassword" type="password" placeholder="请输入 8 位及以上新密码" />
            <button class="primary-button" type="button" :disabled="actionLoading" @click="handleResetPassword">
              {{ actionLoading ? '处理中...' : '重置密码' }}
            </button>
          </div>
        </template>

        <p v-else class="empty-detail">从左侧选择一个用户后，这里会显示具体治理动作。</p>
      </section>
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
.empty-detail {
  margin: 8px 0 0;
  color: #5a6878;
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
  grid-template-columns: 1.2fr 0.9fr;
  gap: 20px;
}

.panel {
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 24px 60px rgba(32, 48, 66, 0.08);
}

.table-panel {
  overflow: hidden;
}

.detail-panel {
  padding: 24px;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
  padding: 24px 24px 16px;
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
  background: rgba(234, 216, 194, 0.5);
}

.sub-text {
  display: block;
  margin-top: 6px;
  color: #687789;
  font-size: 13px;
}

.status-chip {
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

.status-chip.disabled {
  background: rgba(194, 62, 46, 0.12);
  color: #9f2e21;
}

.status-chip.deleted {
  background: rgba(90, 104, 120, 0.12);
  color: #4a5563;
}

.profile-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  margin: 18px 0 24px;
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
}

.field {
  display: grid;
  gap: 10px;
  margin-bottom: 18px;
}

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

.primary-button {
  background: linear-gradient(135deg, #cc6d37 0%, #e58f45 100%);
  color: #fff;
}

.secondary-button {
  background: rgba(23, 96, 61, 0.12);
  color: #17603d;
}

.danger-button {
  background: rgba(194, 62, 46, 0.12);
  color: #9f2e21;
}

.empty-cell {
  text-align: center;
  color: #7b8796;
}

@media (max-width: 1080px) {
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
  .profile-grid {
    grid-template-columns: 1fr;
    display: grid;
  }

  th,
  td {
    padding: 14px 16px;
  }
}
</style>
