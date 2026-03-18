<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import {
  createCampus,
  fetchCampuses,
  type CampusItem,
  updateCampus
} from '@/api/campuses'

const campuses = ref<CampusItem[]>([])
const loading = ref(false)
const saving = ref(false)
const error = ref('')
const success = ref('')
const selectedCampusId = ref<number | null>(null)

const form = reactive({
  campusCode: '',
  campusName: '',
  status: 'ENABLED'
})

function clearNotice() {
  error.value = ''
  success.value = ''
}

function applyCampus(campus: CampusItem | null) {
  selectedCampusId.value = campus?.id ?? null
  form.campusCode = campus?.campusCode ?? ''
  form.campusName = campus?.campusName ?? ''
  form.status = campus?.status ?? 'ENABLED'
  clearNotice()
}

function resetForm() {
  applyCampus(null)
}

async function loadCampuses(preferredCampusId?: number | null) {
  loading.value = true
  error.value = ''
  try {
    const nextCampuses = await fetchCampuses()
    campuses.value = nextCampuses

    if (preferredCampusId != null) {
      applyCampus(nextCampuses.find((campus) => campus.id === preferredCampusId) ?? null)
      return
    }

    if (selectedCampusId.value != null) {
      applyCampus(nextCampuses.find((campus) => campus.id === selectedCampusId.value) ?? null)
    }
  } catch (loadError) {
    error.value = loadError instanceof Error ? loadError.message : '加载校区列表失败'
  } finally {
    loading.value = false
  }
}

function validateForm() {
  if (!form.campusCode.trim()) {
    return '校区编码不能为空'
  }
  if (!form.campusName.trim()) {
    return '校区名称不能为空'
  }
  if (!form.status.trim()) {
    return '校区状态不能为空'
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
    const payload = {
      campusCode: form.campusCode.trim().toUpperCase(),
      campusName: form.campusName.trim(),
      status: form.status
    }

    if (selectedCampusId.value != null) {
      const updated = await updateCampus(selectedCampusId.value, payload)
      success.value = `已更新校区 ${updated.campusName}`
      await loadCampuses(updated.id)
      return
    }

    const created = await createCampus(payload)
    success.value = `已新增校区 ${created.campusName}`
    await loadCampuses(created.id)
  } catch (submitError) {
    error.value = submitError instanceof Error ? submitError.message : '保存校区失败'
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  await loadCampuses()
})
</script>

<template>
  <main class="page">
    <header class="header">
      <div>
        <p class="eyebrow">System</p>
        <h1>校区管理</h1>
        <p>这里不再只是查看列表，管理员可以直接新增校区、调整名称和启停状态。</p>
      </div>
      <button class="ghost-button" type="button" :disabled="loading" @click="loadCampuses(selectedCampusId)">
        {{ loading ? '刷新中...' : '刷新校区' }}
      </button>
    </header>

    <p v-if="error" class="message error-message">{{ error }}</p>
    <p v-if="success" class="message success-message">{{ success }}</p>

    <section class="layout">
      <section class="panel list-panel">
        <div class="panel-head">
          <div>
            <h2>校区列表</h2>
            <p>{{ campuses.length }} 个校区，点击卡片可切到编辑态。</p>
          </div>
        </div>

        <section class="list">
          <article v-if="!loading && campuses.length === 0" class="item empty-item">
            <h2>暂无校区</h2>
            <p>当前没有可展示的校区数据。</p>
          </article>
          <button
            v-for="campus in campuses"
            :key="campus.id"
            class="item campus-card"
            :class="{ active: campus.id === selectedCampusId }"
            type="button"
            @click="applyCampus(campus)"
          >
            <h3>{{ campus.campusName }}</h3>
            <p>编码：{{ campus.campusCode }}</p>
            <span>{{ campus.status === 'ENABLED' ? '启用中' : '已停用' }}</span>
          </button>
        </section>
      </section>

      <section class="panel form-panel">
        <div class="panel-head compact-head">
          <div>
            <h2>{{ selectedCampusId != null ? `编辑校区 #${selectedCampusId}` : '新增校区' }}</h2>
            <p>适合维护多校区赛事运营配置和归属信息。</p>
          </div>
          <button class="ghost-button small-button" type="button" @click="resetForm">新建校区</button>
        </div>

        <label class="field">
          <span>校区编码</span>
          <input v-model="form.campusCode" type="text" placeholder="例如 MAIN / WEST" />
        </label>

        <label class="field">
          <span>校区名称</span>
          <input v-model="form.campusName" type="text" placeholder="请输入校区名称" />
        </label>

        <label class="field">
          <span>状态</span>
          <select v-model="form.status">
            <option value="ENABLED">启用</option>
            <option value="DISABLED">停用</option>
          </select>
        </label>

        <button class="primary-button full-button" type="button" :disabled="saving" @click="handleSubmit">
          {{ saving ? '保存中...' : selectedCampusId != null ? '保存校区' : '创建校区' }}
        </button>
      </section>
    </section>
  </main>
</template>

<style scoped>
.page {
  min-height: 100vh;
  padding: 40px;
  background: linear-gradient(180deg, #f8f1e8 0%, #eef4f8 100%);
}

.header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
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
h3 {
  margin: 0;
  color: #1d2a3a;
}

.header p:last-child,
.panel-head p,
.item p,
.item span {
  margin: 8px 0 0;
  color: #526171;
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

.small-button {
  padding-inline: 14px;
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
  grid-template-columns: minmax(0, 1.1fr) minmax(320px, 0.9fr);
  gap: 20px;
}

.panel {
  padding: 24px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 24px 60px rgba(32, 48, 66, 0.08);
}

.panel-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
  margin-bottom: 18px;
}

.compact-head {
  margin-bottom: 20px;
}

.list {
  display: grid;
  gap: 16px;
}

.item {
  padding: 20px;
  border-radius: 20px;
  background: #f8fafc;
  border: 1px solid transparent;
}

.campus-card {
  text-align: left;
  cursor: pointer;
}

.campus-card.active {
  border-color: rgba(204, 109, 55, 0.26);
  background: linear-gradient(180deg, rgba(255, 247, 240, 0.96), rgba(248, 250, 252, 0.96));
}

.empty-item {
  cursor: default;
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

input,
select {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid #d9e1e8;
  border-radius: 14px;
  background: #fbfdff;
  color: #1d2a3a;
  font: inherit;
  box-sizing: border-box;
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
