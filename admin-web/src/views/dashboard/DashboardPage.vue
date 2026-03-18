<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { exportAdminDashboardCsv, fetchAdminDashboardSummary, type AdminDashboardSummary } from '@/api/dashboard'

const loading = ref(false)
const errorMessage = ref('')
const summary = ref<AdminDashboardSummary | null>(null)

const overviewCards = computed(() => {
  if (!summary.value) {
    return []
  }

  const overview = summary.value.overview
  return [
    { label: '比赛总数', value: `${overview.totalCompetitionCount}` },
    { label: '已发布', value: `${overview.publishedCompetitionCount}` },
    { label: '报名总量', value: `${overview.totalRegistrationCount}` },
    { label: '作品提交', value: `${overview.totalSubmissionCount}` },
    { label: '获奖记录', value: `${overview.totalAwardCount}` },
    { label: '发放积分', value: `${overview.totalAwardPoints}` },
    { label: '老师账号', value: `${overview.teacherCount}` },
    { label: '学生账号', value: `${overview.studentCount}` }
  ]
})

async function loadDashboard() {
  loading.value = true
  errorMessage.value = ''
  try {
    summary.value = await fetchAdminDashboardSummary()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '加载后台看板失败'
  } finally {
    loading.value = false
  }
}

async function downloadCsv() {
  try {
    const csv = await exportAdminDashboardCsv()
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8' })
    const url = window.URL.createObjectURL(blob)
    const anchor = document.createElement('a')
    anchor.href = url
    anchor.download = 'admin-dashboard.csv'
    anchor.click()
    window.URL.revokeObjectURL(url)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '导出后台看板失败'
  }
}

onMounted(() => {
  void loadDashboard()
})
</script>

<template>
  <main class="dashboard">
    <section class="hero-card">
      <div>
        <p class="eyebrow">Dashboard</p>
        <h1>平台运营总览</h1>
        <p class="hero-desc">这里汇总了比赛、参赛、作品、获奖和账号规模，方便后台快速掌握整个平台运行状态。</p>
      </div>
      <div class="hero-actions">
        <button class="ghost-button" :disabled="loading" @click="loadDashboard">
          {{ loading ? '刷新中...' : '刷新数据' }}
        </button>
        <button class="primary-button" @click="downloadCsv">导出 CSV</button>
      </div>
    </section>

    <p v-if="errorMessage" class="error-banner">{{ errorMessage }}</p>

    <section v-if="overviewCards.length" class="overview-grid">
      <article v-for="item in overviewCards" :key="item.label" class="metric-card">
        <p class="metric-value">{{ item.value }}</p>
        <p class="metric-label">{{ item.label }}</p>
      </article>
    </section>

    <section class="panel-grid">
      <article class="panel-card">
        <div class="panel-header">
          <h2>比赛状态分布</h2>
          <span>按当前平台比赛状态聚合</span>
        </div>
        <div v-if="summary?.statusDistribution.length" class="status-list">
          <div v-for="item in summary.statusDistribution" :key="item.status" class="status-item">
            <span class="status-label">{{ item.label }}</span>
            <strong class="status-value">{{ item.count }}</strong>
          </div>
        </div>
        <p v-else class="empty-text">暂无状态分布数据</p>
      </article>

      <article class="panel-card">
        <div class="panel-header">
          <h2>重点比赛</h2>
          <span>按报名量、作品量和获奖量综合排序</span>
        </div>
        <div v-if="summary?.topCompetitions.length" class="competition-list">
          <div v-for="item in summary.topCompetitions" :key="item.competitionId" class="competition-item">
            <div class="competition-top">
              <div>
                <h3>{{ item.title }}</h3>
                <p>{{ item.status }}</p>
              </div>
              <div class="competition-flags">
                <span v-if="item.pinned">置顶</span>
                <span v-if="item.recommended">推荐</span>
              </div>
            </div>
            <div class="competition-stats">
              <span>报名 {{ item.registrationCount }}</span>
              <span>作品 {{ item.submissionCount }}</span>
              <span>获奖 {{ item.awardCount }}</span>
              <span>积分 {{ item.awardPoints }}</span>
            </div>
          </div>
        </div>
        <p v-else-if="loading" class="empty-text">正在加载比赛数据...</p>
        <p v-else class="empty-text">暂无比赛数据</p>
      </article>
    </section>
  </main>
</template>

<style scoped>
.dashboard {
  min-height: 100vh;
  padding: 40px;
  background:
    radial-gradient(circle at top left, rgba(109, 165, 205, 0.2), transparent 30%),
    linear-gradient(160deg, #f4f7fb 0%, #eef4f8 100%);
}

.hero-card,
.metric-card,
.panel-card {
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 24px 60px rgba(23, 47, 74, 0.08);
}

.hero-card {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  padding: 32px;
  border-radius: 28px;
}

.eyebrow {
  margin: 0 0 10px;
  color: #487093;
  text-transform: uppercase;
  letter-spacing: 0.14em;
  font-size: 12px;
}

h1,
h2,
h3,
.hero-desc,
.metric-value,
.metric-label,
.status-label,
.status-value,
.empty-text,
.error-banner,
.competition-stats span,
.competition-top p,
.panel-header span {
  margin: 0;
}

h1 {
  margin-bottom: 12px;
  font-size: 40px;
  line-height: 1.1;
  color: #18324a;
}

.hero-desc {
  max-width: 760px;
  color: #546779;
  font-size: 17px;
  line-height: 1.7;
}

.hero-actions {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.ghost-button,
.primary-button {
  border: none;
  border-radius: 999px;
  padding: 12px 18px;
  font-size: 14px;
  cursor: pointer;
}

.ghost-button {
  background: #dbe8f2;
  color: #426682;
}

.primary-button {
  background: #4b7897;
  color: #f8fbff;
}

.error-banner {
  margin-top: 18px;
  color: #b14a2f;
  font-size: 14px;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
  margin-top: 24px;
}

.metric-card,
.panel-card {
  border-radius: 24px;
  padding: 24px;
}

.metric-value {
  margin-bottom: 8px;
  color: #18324a;
  font-size: 34px;
  font-weight: 700;
}

.metric-label,
.competition-top p,
.competition-stats span,
.panel-header span,
.empty-text {
  color: #637688;
  font-size: 14px;
}

.panel-grid {
  display: grid;
  grid-template-columns: minmax(260px, 320px) minmax(0, 1fr);
  gap: 18px;
  margin-top: 18px;
}

.panel-header {
  margin-bottom: 18px;
}

.panel-header h2 {
  margin: 0 0 8px;
  color: #18324a;
  font-size: 20px;
}

.status-list,
.competition-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.status-item,
.competition-item {
  border-radius: 18px;
  background: #f6f9fc;
}

.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 18px;
}

.status-value {
  color: #18324a;
  font-size: 24px;
}

.competition-item {
  padding: 18px;
}

.competition-top {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.competition-top h3 {
  margin: 0 0 6px;
  color: #18324a;
  font-size: 18px;
}

.competition-flags {
  display: flex;
  gap: 8px;
}

.competition-flags span {
  align-self: flex-start;
  padding: 6px 10px;
  border-radius: 999px;
  background: #dce8f1;
  color: #466c88;
  font-size: 12px;
}

.competition-stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

@media (max-width: 1100px) {
  .overview-grid,
  .competition-stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .panel-grid,
  .hero-card {
    grid-template-columns: 1fr;
    flex-direction: column;
  }
}

@media (max-width: 720px) {
  .dashboard {
    padding: 20px;
  }

  .overview-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
