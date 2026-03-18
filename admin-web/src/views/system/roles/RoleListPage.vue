<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import {
  createRole,
  fetchRoles,
  updateRole,
  type RoleItem
} from '@/api/roles'
import {
  buildRolePayload,
  createEmptyRoleForm,
  permissionCatalog,
  validateRoleForm,
  type RoleFormValues
} from '@/utils/role-governance'

const roles = ref<RoleItem[]>([])
const loading = ref(false)
const saving = ref(false)
const error = ref('')
const success = ref('')
const selectedRoleCode = ref<string | null>(null)
const form = reactive<RoleFormValues>(createEmptyRoleForm())

const selectedRole = computed(() => roles.value.find((role) => role.roleCode === selectedRoleCode.value) ?? null)
const isEditing = computed(() => selectedRole.value !== null)

function clearNotice() {
  error.value = ''
  success.value = ''
}

function applyRole(role: RoleItem | null) {
  selectedRoleCode.value = role?.roleCode ?? null
  if (!role) {
    Object.assign(form, createEmptyRoleForm())
    clearNotice()
    return
  }

  form.roleCode = role.roleCode
  form.roleName = role.roleName
  form.description = role.description ?? ''
  form.permissionCodes = [...role.permissionCodes]
  clearNotice()
}

async function loadRoles(preferredRoleCode?: string | null) {
  loading.value = true
  try {
    const nextRoles = await fetchRoles()
    roles.value = nextRoles

    if (preferredRoleCode) {
      applyRole(nextRoles.find((role) => role.roleCode === preferredRoleCode) ?? null)
      return
    }

    if (selectedRoleCode.value) {
      applyRole(nextRoles.find((role) => role.roleCode === selectedRoleCode.value) ?? null)
    }
  } catch (loadError) {
    error.value = loadError instanceof Error ? loadError.message : '加载角色列表失败'
  } finally {
    loading.value = false
  }
}

function resetForm() {
  applyRole(null)
}

async function submitRole() {
  clearNotice()
  const validationMessage = validateRoleForm(form)
  if (validationMessage) {
    error.value = validationMessage
    return
  }

  saving.value = true
  try {
    const payload = buildRolePayload(form)
    if (selectedRoleCode.value) {
      const updatedRole = await updateRole(selectedRoleCode.value, {
        roleName: payload.roleName,
        description: payload.description,
        permissionCodes: payload.permissionCodes
      })
      success.value = `角色 ${updatedRole.roleName} 更新成功`
      await loadRoles(updatedRole.roleCode)
      return
    }

    const createdRole = await createRole(payload)
    success.value = `角色 ${createdRole.roleName} 创建成功`
    await loadRoles(createdRole.roleCode)
  } catch (submitError) {
    error.value = submitError instanceof Error ? submitError.message : '保存角色失败'
  } finally {
    saving.value = false
  }
}

function permissionLabel(code: string) {
  return permissionCatalog.find((item) => item.code === code)?.label ?? code
}

onMounted(() => {
  void loadRoles()
})
</script>

<template>
  <main class="page">
    <header class="header">
      <div>
        <p class="eyebrow">Role Governance</p>
        <h1>角色权限</h1>
        <p>内置角色和自定义角色都已经接上真实后台接口，这里可以查看角色人数、编辑权限并新增运营角色。</p>
      </div>
      <div class="header-actions">
        <button class="ghost-button" type="button" :disabled="loading" @click="loadRoles(selectedRoleCode)">
          {{ loading ? '刷新中...' : '刷新角色' }}
        </button>
        <button class="secondary-button" type="button" @click="resetForm">新建角色</button>
      </div>
    </header>

    <p v-if="error" class="message error-message">{{ error }}</p>
    <p v-if="success" class="message success-message">{{ success }}</p>

    <section class="layout">
      <section class="cards">
        <article
          v-for="role in roles"
          :key="role.roleCode"
          class="card"
          :class="{ active: role.roleCode === selectedRoleCode }"
          @click="applyRole(role)"
        >
          <div class="card-head">
            <div>
              <h2>{{ role.roleName }}</h2>
              <p>{{ role.roleCode }}</p>
            </div>
            <span class="pill" :class="role.builtIn ? 'built-in' : 'custom'">
              {{ role.builtIn ? '内置' : '自定义' }}
            </span>
          </div>

          <p class="desc">{{ role.description || '暂无描述' }}</p>

          <div class="meta-row">
            <span>状态：{{ role.status === 'ENABLED' ? '启用' : role.status }}</span>
            <span>用户数：{{ role.userCount }}</span>
          </div>

          <div class="tag-list">
            <span v-for="code in role.permissionCodes" :key="`${role.roleCode}-${code}`" class="tag">
              {{ permissionLabel(code) }}
            </span>
          </div>
        </article>

        <article v-if="!loading && roles.length === 0" class="card empty-card">
          暂无角色数据
        </article>
      </section>

      <section class="panel form-panel">
        <div class="panel-head">
          <div>
            <h2>{{ isEditing ? `编辑角色 ${selectedRoleCode}` : '创建新角色' }}</h2>
            <p>{{ isEditing ? '修改角色名称、描述与权限范围。' : '新增一个运营或评审类角色。' }}</p>
          </div>
        </div>

        <div class="field">
          <label>角色编码</label>
          <input
            v-model="form.roleCode"
            type="text"
            maxlength="32"
            :disabled="isEditing"
            placeholder="例如 JUDGE、OPERATOR"
          />
        </div>

        <div class="field">
          <label>角色名称</label>
          <input v-model="form.roleName" type="text" maxlength="24" placeholder="请输入角色名称" />
        </div>

        <div class="field">
          <label>角色说明</label>
          <textarea v-model="form.description" rows="4" placeholder="请输入角色定位和职责"></textarea>
        </div>

        <div class="field">
          <label>权限选择</label>
          <div class="permission-grid">
            <label v-for="permission in permissionCatalog" :key="permission.code" class="permission-item">
              <input v-model="form.permissionCodes" type="checkbox" :value="permission.code" />
              <span>{{ permission.label }}</span>
              <small>{{ permission.code }}</small>
            </label>
          </div>
        </div>

        <button class="primary-button" type="button" :disabled="saving" @click="submitRole">
          {{ saving ? '提交中...' : isEditing ? '保存角色' : '创建角色' }}
        </button>
      </section>
    </section>
  </main>
</template>

<style scoped>
.page {
  min-height: 100vh;
  padding: 40px;
  background:
    radial-gradient(circle at top left, rgba(204, 109, 55, 0.1), transparent 22%),
    linear-gradient(180deg, #f8f1e8 0%, #eef4f8 100%);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 24px;
  margin-bottom: 20px;
}

.header-actions {
  display: flex;
  gap: 12px;
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
.card-head p,
.desc {
  margin: 8px 0 0;
  color: #5c6a79;
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
  grid-template-columns: 1.05fr 0.95fr;
  gap: 20px;
}

.cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 18px;
  align-content: start;
}

.card,
.panel {
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 24px 60px rgba(32, 48, 66, 0.08);
}

.card {
  padding: 22px;
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.card:hover {
  transform: translateY(-2px);
  box-shadow: 0 28px 64px rgba(32, 48, 66, 0.1);
}

.card.active {
  outline: 2px solid rgba(204, 109, 55, 0.28);
  background: linear-gradient(180deg, rgba(255, 247, 240, 0.96) 0%, rgba(255, 255, 255, 0.96) 100%);
}

.panel {
  padding: 24px;
}

.card-head,
.panel-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.pill {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.pill.built-in {
  background: rgba(16, 101, 164, 0.12);
  color: #0d5b95;
}

.pill.custom {
  background: rgba(204, 109, 55, 0.12);
  color: #a55221;
}

.meta-row {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  margin-top: 16px;
  color: #617082;
  font-size: 13px;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 16px;
}

.tag {
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(236, 241, 246, 0.92);
  color: #3a4655;
  font-size: 12px;
}

.empty-card {
  display: grid;
  place-items: center;
  min-height: 160px;
  color: #7b8796;
  cursor: default;
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

.permission-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.permission-item {
  display: grid;
  gap: 6px;
  padding: 14px;
  border: 1px solid #e3e9ef;
  border-radius: 16px;
  background: #fbfdff;
  cursor: pointer;
}

.permission-item input {
  width: auto;
  margin: 0;
}

.permission-item small {
  color: #8a97a6;
  font-size: 12px;
  font-weight: 500;
}

button {
  border: none;
  border-radius: 14px;
  padding: 11px 16px;
  font: inherit;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.18s ease;
}

button:hover:enabled {
  transform: translateY(-1px);
}

button:disabled {
  cursor: not-allowed;
  opacity: 0.72;
}

.ghost-button {
  background: rgba(255, 255, 255, 0.9);
  color: #334154;
  box-shadow: 0 14px 30px rgba(32, 48, 66, 0.08);
}

.secondary-button {
  background: rgba(13, 91, 149, 0.12);
  color: #0d5b95;
}

.primary-button {
  background: linear-gradient(135deg, #cc6d37 0%, #e58f45 100%);
  color: #fff;
}

@media (max-width: 1120px) {
  .layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .page {
    padding: 24px;
  }

  .header {
    display: grid;
  }

  .header-actions,
  .permission-grid {
    grid-template-columns: 1fr;
    display: grid;
  }
}
</style>
