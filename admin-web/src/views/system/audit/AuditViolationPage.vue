<script setup lang="ts">
import { onMounted, ref } from 'vue'
import {
  fetchAuditRules,
  fetchViolationRecords,
  type AuditRuleSummary,
  type ViolationRecordItem
} from '@/api/audit'

const loading = ref(false)
const error = ref('')
const rules = ref<AuditRuleSummary | null>(null)
const records = ref<ViolationRecordItem[]>([])

function sceneLabel(scene: string) {
  if (scene === 'COMPETITION') {
    return '比赛内容'
  }
  if (scene === 'MESSAGE') {
    return '消息内容'
  }
  if (scene === 'SUBMISSION') {
    return '作品上传'
  }
  return scene
}

async function loadAuditData() {
  loading.value = true
  error.value = ''
  try {
    const [ruleSummary, violationRecords] = await Promise.all([
      fetchAuditRules(),
      fetchViolationRecords()
    ])
    rules.value = ruleSummary
    records.value = violationRecords
  } catch (loadError) {
    error.value = loadError instanceof Error ? loadError.message : '加载审核数据失败'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  void loadAuditData()
})
</script>

<template>
  <main class="page">
    <header class="header">
      <div>
        <p class="eyebrow">Local Audit</p>
        <h1>本地审核与违规记录</h1>
        <p>当前版本不接第三方审核，先用本地敏感词和文件白名单覆盖比赛、消息、作品三条主链路。</p>
      </div>
      <button class="refresh-button" type="button" :disabled="loading" @click="loadAuditData">
        {{ loading ? '刷新中...' : '刷新记录' }}
      </button>
    </header>

    <p v-if="error" class="message error-message">{{ error }}</p>

    <section class="rule-grid">
      <article class="rule-card">
        <h2>敏感词</h2>
        <div class="tag-list">
          <span v-for="word in rules?.sensitiveWords ?? []" :key="word" class="tag danger-tag">
            {{ word }}
          </span>
        </div>
      </article>

      <article class="rule-card">
        <h2>作品白名单</h2>
        <div class="tag-list">
          <span
            v-for="extension in rules?.allowedSubmissionExtensions ?? []"
            :key="extension"
            class="tag safe-tag"
          >
            {{ extension }}
          </span>
        </div>
      </article>

      <article class="rule-card">
        <h2>重点拦截后缀</h2>
        <div class="tag-list">
          <span
            v-for="extension in rules?.blockedSubmissionExtensions ?? []"
            :key="extension"
            class="tag block-tag"
          >
            {{ extension }}
          </span>
        </div>
      </article>
    </section>

    <section class="table-card">
      <div class="table-head">
        <div>
          <h2>违规记录</h2>
          <p>按最近命中时间倒序展示，方便运营快速回溯比赛、聊天和作品的异常提交。</p>
        </div>
      </div>

      <div v-if="!loading && records.length === 0" class="empty-state">
        暂无违规记录
      </div>

      <div v-else class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>场景</th>
              <th>用户</th>
              <th>原因</th>
              <th>命中内容</th>
              <th>命中词</th>
              <th>时间</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="record in records" :key="record.id">
              <td>{{ sceneLabel(record.scene) }}</td>
              <td>{{ record.userName || `用户#${record.userId ?? '-'}` }}</td>
              <td>{{ record.reason }}</td>
              <td class="snippet-cell">{{ record.contentSnippet }}</td>
              <td>
                <div class="tag-list compact">
                  <span v-for="word in record.hitWords" :key="`${record.id}-${word}`" class="tag mini-tag">
                    {{ word }}
                  </span>
                </div>
              </td>
              <td>{{ record.createdAt.replace('T', ' ') }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </main>
</template>

<style scoped>
.page {
  min-height: 100vh;
  padding: 40px;
  background:
    radial-gradient(circle at top right, rgba(198, 70, 70, 0.12), transparent 24%),
    linear-gradient(180deg, #f5efe9 0%, #eef4f8 100%);
}

.header,
.table-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 24px;
}

.header {
  margin-bottom: 20px;
}

.eyebrow {
  margin: 0 0 8px;
  color: #b5533d;
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
.table-head p {
  margin: 8px 0 0;
  color: #5d6978;
}

.refresh-button {
  border: none;
  border-radius: 14px;
  padding: 12px 18px;
  background: linear-gradient(135deg, #8b2d24 0%, #c3563d 100%);
  color: #fff;
  font: inherit;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 18px 34px rgba(140, 46, 38, 0.18);
}

.refresh-button:disabled {
  cursor: not-allowed;
  opacity: 0.72;
}

.message {
  margin: 0 0 18px;
  padding: 12px 14px;
  border-radius: 14px;
}

.error-message {
  background: rgba(194, 62, 46, 0.12);
  color: #9f2e21;
}

.rule-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
  margin-bottom: 20px;
}

.rule-card,
.table-card {
  background: rgba(255, 255, 255, 0.94);
  border-radius: 24px;
  box-shadow: 0 24px 60px rgba(32, 48, 66, 0.08);
}

.rule-card {
  padding: 22px;
}

.rule-card h2 {
  margin-bottom: 14px;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tag {
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  padding: 6px 12px;
  font-size: 13px;
  font-weight: 600;
}

.danger-tag {
  background: rgba(194, 62, 46, 0.12);
  color: #992d1f;
}

.safe-tag {
  background: rgba(38, 130, 85, 0.12);
  color: #1f704a;
}

.block-tag,
.mini-tag {
  background: rgba(139, 45, 36, 0.1);
  color: #7d261e;
}

.compact {
  gap: 6px;
}

.table-card {
  padding: 24px;
}

.table-wrap {
  overflow-x: auto;
  margin-top: 18px;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th,
td {
  text-align: left;
  padding: 14px 12px;
  border-bottom: 1px solid #e8edf2;
  color: #314153;
  vertical-align: top;
}

th {
  color: #667487;
  font-size: 13px;
  font-weight: 700;
}

.snippet-cell {
  min-width: 260px;
  color: #4f5d6d;
}

.empty-state {
  margin-top: 18px;
  padding: 32px 12px;
  border-radius: 18px;
  background: #f8fafc;
  color: #7a8795;
  text-align: center;
}

@media (max-width: 1080px) {
  .rule-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .page {
    padding: 24px;
  }

  .header,
  .table-head {
    display: grid;
  }
}
</style>
