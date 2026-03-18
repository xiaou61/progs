<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { exportUserLogsCsv, fetchUserLogs, type OperationLogItem } from '@/api/users'

const logs = ref<OperationLogItem[]>([])
const loading = ref(false)
const exporting = ref(false)
const error = ref('')
const success = ref('')

const query = reactive({
  operatorName: '',
  action: '',
  target: ''
})

async function loadLogs() {
  loading.value = true
  error.value = ''

  try {
    logs.value = await fetchUserLogs(query)
  } catch (loadError) {
    error.value = loadError instanceof Error ? loadError.message : '加载日志失败'
  } finally {
    loading.value = false
  }
}

async function exportLogs() {
  exporting.value = true
  error.value = ''
  success.value = ''

  try {
    const csv = await exportUserLogsCsv(query)
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8' })
    const url = URL.createObjectURL(blob)
    const anchor = document.createElement('a')
    anchor.href = url
    anchor.download = 'admin-logs.csv'
    anchor.click()
    URL.revokeObjectURL(url)
    success.value = '日志导出成功'
  } catch (exportError) {
    error.value = exportError instanceof Error ? exportError.message : '导出日志失败'
  } finally {
    exporting.value = false
  }
}

function formatDateTime(value: string) {
  return value.replace('T', ' ')
}

onMounted(() => {
  void loadLogs()
})
</script>

<template>
  <main class="log-page">
    <section class="hero">
      <div>
        <p class="eyebrow">Operation Log</p>
        <h1>操作日志</h1>
        <p>这里汇总后台治理动作，支持按操作人、动作和目标筛选，并导出 CSV 留档。</p>
      </div>
      <div class="hero-actions">
        <button class="secondary-button" type="button" :disabled="loading" @click="loadLogs">
          {{ loading ? '刷新中...' : '刷新日志' }}
        </button>
        <button class="primary-button" type="button" :disabled="exporting" @click="exportLogs">
          {{ exporting ? '导出中...' : '导出 CSV' }}
        </button>
      </div>
    </section>

    <section class="filters">
      <label>
        操作人
        <input v-model="query.operatorName" type="text" placeholder="例如 系统管理员" />
      </label>
      <label>
        动作
        <input v-model="query.action" type="text" placeholder="例如 VIOLATION_MARK" />
      </label>
      <label>
        目标
        <input v-model="query.target" type="text" placeholder="例如 S20260001" />
      </label>
      <button class="primary-button" type="button" :disabled="loading" @click="loadLogs">查询</button>
    </section>

    <p v-if="error" class="message error">{{ error }}</p>
    <p v-if="success" class="message success">{{ success }}</p>

    <section class="table-card">
      <table v-if="logs.length">
        <thead>
          <tr>
            <th>操作人</th>
            <th>动作</th>
            <th>目标</th>
            <th>详情</th>
            <th>时间</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in logs" :key="item.id">
            <td>{{ item.operatorName }}</td>
            <td>{{ item.action }}</td>
            <td>{{ item.target }}</td>
            <td>{{ item.detail || '—' }}</td>
            <td>{{ formatDateTime(item.createdAt) }}</td>
          </tr>
        </tbody>
      </table>
      <div v-else class="empty-state">
        {{ loading ? '正在加载日志...' : '当前没有符合条件的日志记录' }}
      </div>
    </section>
  </main>
</template>

<style scoped>
.log-page {
  display: grid;
  gap: 20px;
  padding: 28px;
  color: #233645;
}

.hero,
.filters,
.table-card {
  background: #ffffff;
  border: 1px solid #dde5ec;
  border-radius: 24px;
  box-shadow: 0 18px 45px rgba(35, 54, 69, 0.08);
}

.hero {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 24px;
}

.eyebrow {
  margin: 0 0 8px;
  font-size: 12px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: #8a6a37;
}

.hero h1 {
  margin: 0;
  font-size: 30px;
}

.hero p:last-child {
  margin-bottom: 0;
  max-width: 640px;
  line-height: 1.6;
  color: #5e7182;
}

.hero-actions {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.filters {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  padding: 20px 24px;
  align-items: end;
}

.filters label {
  display: grid;
  gap: 8px;
  font-size: 14px;
  color: #5d7080;
}

.filters input {
  height: 42px;
  padding: 0 12px;
  border: 1px solid #d5dde5;
  border-radius: 14px;
}

.primary-button,
.secondary-button {
  height: 42px;
  padding: 0 18px;
  border-radius: 999px;
  border: none;
  cursor: pointer;
  font-weight: 600;
}

.primary-button {
  background: linear-gradient(135deg, #c86c31, #dd8b47);
  color: #fff;
}

.secondary-button {
  background: #eef3f7;
  color: #314657;
}

.message {
  margin: 0;
  font-size: 14px;
}

.message.error {
  color: #b0432f;
}

.message.success {
  color: #2f7d54;
}

.table-card {
  overflow: hidden;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th,
td {
  padding: 14px 18px;
  text-align: left;
  border-bottom: 1px solid #edf1f4;
}

thead {
  background: #f6f8fb;
}

.empty-state {
  padding: 32px 24px;
  text-align: center;
  color: #7b8d9a;
}

@media (max-width: 960px) {
  .hero {
    flex-direction: column;
  }

  .filters {
    grid-template-columns: 1fr;
  }
}
</style>
