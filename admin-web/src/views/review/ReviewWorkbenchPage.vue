<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import {
  fetchManagedCompetitions,
  type CompetitionManageItem
} from '@/api/competition-manage'
import { fetchUsers, type UserItem } from '@/api/users'
import {
  fetchCompetitionScores,
  fetchReviewTasks,
  publishCompetitionResult,
  submitReviewTask,
  type CompetitionScoreItem,
  type ReviewTaskItem
} from '@/api/review'
import { useAdminSessionStore } from '@/stores/admin-session'
import { validatePublishResultForm } from '@/utils/score-publish-form'

const sessionStore = useAdminSessionStore()
const defaultReviewerName = sessionStore.displayName.trim()
const competitions = ref<CompetitionManageItem[]>([])
const users = ref<UserItem[]>([])
const selectedCompetitionId = ref(0)
const competitionLoading = ref(false)
const loading = ref(false)
const error = ref('')
const publishError = ref('')
const publishSuccess = ref('')
const publishing = ref(false)
const reviewError = ref('')
const reviewSuccess = ref('')
const reviewing = ref(false)
const tasks = ref<ReviewTaskItem[]>([])
const scores = ref<CompetitionScoreItem[]>([])
const reviewForm = reactive({
  submissionId: 0,
  studentId: 0,
  reviewerName: defaultReviewerName,
  reviewComment: '',
  suggestedScore: 90
})
const publishForm = reactive({
  studentId: 0,
  score: 95,
  rank: 1,
  awardName: '一等奖',
  points: 30,
  reviewerName: defaultReviewerName,
  reviewComment: ''
})
const selectedCompetition = computed(() =>
  competitions.value.find((item) => item.id === selectedCompetitionId.value) ?? null
)
const taskOptions = computed(() =>
  tasks.value.map((item) => ({
    submissionId: item.submissionId,
    label: `作品 #${item.submissionId} · 学生 ${item.studentId} · ${item.status}`
  }))
)

function organizerText(organizerId: number) {
  const matched = users.value.find((user) => user.id === organizerId)
  return matched ? `${matched.realName} · ${matched.studentNo}` : '发起人信息待补充'
}

function resetForms() {
  reviewForm.submissionId = 0
  reviewForm.studentId = 0
  reviewForm.reviewerName = defaultReviewerName
  reviewForm.reviewComment = ''
  reviewForm.suggestedScore = 90
  publishForm.studentId = 0
  publishForm.score = 95
  publishForm.rank = 1
  publishForm.awardName = '一等奖'
  publishForm.points = 30
  publishForm.reviewerName = defaultReviewerName
  publishForm.reviewComment = ''
}

async function loadCompetitions(preferId?: number) {
  competitionLoading.value = true
  error.value = ''
  try {
    const [nextCompetitions, nextUsers] = await Promise.all([fetchManagedCompetitions(), fetchUsers()])
    competitions.value = nextCompetitions
    users.value = nextUsers
    const nextCompetitionId = preferId || selectedCompetitionId.value || competitions.value[0]?.id || 0
    selectedCompetitionId.value = nextCompetitionId
    if (!nextCompetitionId) {
      tasks.value = []
      scores.value = []
      resetForms()
      return
    }
    await loadWorkbench(nextCompetitionId)
  } catch (loadError) {
    error.value = loadError instanceof Error ? loadError.message : '加载比赛列表失败'
  } finally {
    competitionLoading.value = false
  }
}

async function loadWorkbench(competitionId = selectedCompetitionId.value) {
  if (!competitionId) {
    tasks.value = []
    scores.value = []
    return
  }
  loading.value = true
  error.value = ''

  try {
    const [taskItems, scoreItems] = await Promise.all([
      fetchReviewTasks(competitionId),
      fetchCompetitionScores(competitionId)
    ])
    tasks.value = taskItems
    scores.value = scoreItems
    if (!taskItems.some((item) => item.submissionId === reviewForm.submissionId)) {
      reviewForm.submissionId = 0
      reviewForm.studentId = 0
    }
  } catch (loadError) {
    error.value = loadError instanceof Error ? loadError.message : '加载评审数据失败'
  } finally {
    loading.value = false
  }
}

function formatDateTime(value: string | null) {
  if (!value) {
    return ''
  }
  return value.replace('T', ' ')
}

function fillStudentId(studentId: number) {
  publishForm.studentId = studentId
  publishSuccess.value = ''
  publishError.value = ''
}

function resolveReviewerName(value: string | null) {
  return value?.trim() ? value : '未分配'
}

function handleTaskSelection() {
  const matchedTask = tasks.value.find((item) => item.submissionId === reviewForm.submissionId)
  if (!matchedTask) {
    reviewForm.studentId = 0
    return
  }
  pickTask(matchedTask)
}

function pickTask(item: ReviewTaskItem) {
  reviewForm.submissionId = item.submissionId
  reviewForm.studentId = item.studentId
  reviewForm.reviewerName = item.reviewerName?.trim() || defaultReviewerName
  reviewForm.reviewComment = item.reviewComment ?? ''
  reviewForm.suggestedScore = item.suggestedScore ?? 90
  publishForm.studentId = item.studentId
  publishForm.reviewerName = reviewForm.reviewerName
  publishForm.reviewComment = reviewForm.reviewComment
  if (item.suggestedScore !== null) {
    publishForm.score = item.suggestedScore
  }
  reviewError.value = ''
  reviewSuccess.value = ''
}

async function handleCompetitionChange() {
  resetForms()
  publishError.value = ''
  publishSuccess.value = ''
  reviewError.value = ''
  reviewSuccess.value = ''
  await loadWorkbench()
}

async function submitReview() {
  reviewError.value = ''
  reviewSuccess.value = ''
  if (!selectedCompetitionId.value) {
    reviewError.value = '请先选择比赛'
    return
  }
  if (!reviewForm.submissionId || !reviewForm.studentId) {
    reviewError.value = '请先从右侧待评作品中选择一条记录'
    return
  }
  if (!reviewForm.reviewerName.trim()) {
    reviewError.value = '评审老师不能为空'
    return
  }
  if (!reviewForm.reviewComment.trim()) {
    reviewError.value = '评审意见不能为空'
    return
  }
  if (reviewForm.suggestedScore < 0) {
    reviewError.value = '建议分数不能为负数'
    return
  }

  reviewing.value = true
  try {
    await submitReviewTask({
      competitionId: selectedCompetitionId.value,
      submissionId: reviewForm.submissionId,
      studentId: reviewForm.studentId,
      reviewerName: reviewForm.reviewerName,
      reviewComment: reviewForm.reviewComment,
      suggestedScore: reviewForm.suggestedScore
    })
    publishForm.studentId = reviewForm.studentId
    publishForm.reviewerName = reviewForm.reviewerName
    publishForm.reviewComment = reviewForm.reviewComment
    publishForm.score = reviewForm.suggestedScore
    reviewSuccess.value = `作品 #${reviewForm.submissionId} 评审已保存`
    await loadWorkbench()
  } catch (submitError) {
    reviewError.value = submitError instanceof Error ? submitError.message : '提交评审失败'
  } finally {
    reviewing.value = false
  }
}

async function submitResult() {
  publishError.value = ''
  publishSuccess.value = ''

  const validationMessage = validatePublishResultForm({
    competitionId: selectedCompetitionId.value,
    studentId: publishForm.studentId,
    score: publishForm.score,
    rank: publishForm.rank,
    awardName: publishForm.awardName,
    points: publishForm.points
  })
  if (validationMessage) {
    publishError.value = validationMessage
    return
  }

  publishing.value = true
  try {
    const scoreId = await publishCompetitionResult({
      competitionId: selectedCompetitionId.value,
      studentId: publishForm.studentId,
      score: publishForm.score,
      rank: publishForm.rank,
      awardName: publishForm.awardName,
      points: publishForm.points,
      reviewerName: publishForm.reviewerName,
      reviewComment: publishForm.reviewComment
    })
    publishSuccess.value = `结果发布成功，记录编号 #${scoreId}，电子奖状已自动生成`
    await loadWorkbench()
  } catch (submitError) {
    publishError.value = submitError instanceof Error ? submitError.message : '发布结果失败'
  } finally {
    publishing.value = false
  }
}

onMounted(() => {
  void loadCompetitions()
})
</script>

<template>
  <section class="page">
    <header class="hero">
      <p class="eyebrow">Review</p>
      <h1>评审工作台</h1>
      <p>当前支持先提交评审意见，再发布结果。发布后会自动生成电子奖状编号，并同步展示到学生结果页。</p>
    </header>

    <section class="toolbar">
      <label class="field">
        <span>当前比赛</span>
        <select
          v-model.number="selectedCompetitionId"
          :disabled="competitionLoading || loading || competitions.length === 0"
          @change="handleCompetitionChange"
        >
          <option :value="0" disabled>{{ competitions.length ? '请选择比赛' : '暂无可评审比赛' }}</option>
          <option v-for="item in competitions" :key="item.id" :value="item.id">
            #{{ item.id }} {{ item.title }}
          </option>
        </select>
      </label>
      <button type="button" :disabled="loading || competitionLoading" @click="loadCompetitions(selectedCompetitionId)">
        {{ loading || competitionLoading ? '加载中...' : '刷新工作台' }}
      </button>
    </section>
    <p v-if="selectedCompetition" class="toolbar-hint">
      {{ selectedCompetition.title }} · {{ selectedCompetition.status }} · {{ organizerText(selectedCompetition.organizerId) }}
    </p>
    <p v-else-if="!competitionLoading" class="toolbar-hint">当前没有可评审的比赛，请先完成比赛发布。</p>

    <section class="board">
      <div class="publish-card">
        <h2>评审与结果发布</h2>
        <p v-if="error" class="error-text">{{ error }}</p>
        <p v-else>待评作品 {{ tasks.length }} 条，已发布结果 {{ scores.length }} 条。</p>
        <p>建议先选择待评作品并保存评审意见，再将建议分数和评语带入结果发布表单。</p>

        <div class="publish-form review-form">
          <label class="form-field">
            <span>作品编号</span>
            <select
              v-model.number="reviewForm.submissionId"
              :disabled="loading || tasks.length === 0"
              @change="handleTaskSelection"
            >
              <option :value="0" disabled>{{ tasks.length ? '请选择待评作品' : '当前没有可选作品' }}</option>
              <option v-for="item in taskOptions" :key="item.submissionId" :value="item.submissionId">
                {{ item.label }}
              </option>
            </select>
          </label>
          <label class="form-field">
            <span>学生编号</span>
            <input :value="reviewForm.studentId || ''" type="number" min="1" readonly />
          </label>
          <label class="form-field">
            <span>评审老师</span>
            <input v-model="reviewForm.reviewerName" type="text" />
          </label>
          <label class="form-field">
            <span>建议分数</span>
            <input v-model.number="reviewForm.suggestedScore" type="number" min="0" />
          </label>
          <label class="form-field full-width">
            <span>评审意见</span>
            <textarea v-model="reviewForm.reviewComment" rows="3" placeholder="请输入作品评审意见"></textarea>
          </label>
        </div>
        <p v-if="reviewError" class="error-text">{{ reviewError }}</p>
        <p v-if="reviewSuccess" class="success-text">{{ reviewSuccess }}</p>
        <button type="button" :disabled="reviewing" @click="submitReview">
          {{ reviewing ? '提交中...' : '先保存评审意见' }}
        </button>

        <div class="publish-form">
          <label class="form-field">
            <span>学生编号</span>
            <input :value="publishForm.studentId || ''" type="number" min="1" readonly />
          </label>
          <label class="form-field">
            <span>成绩</span>
            <input v-model.number="publishForm.score" type="number" min="0" />
          </label>
          <label class="form-field">
            <span>名次</span>
            <input v-model.number="publishForm.rank" type="number" min="1" />
          </label>
          <label class="form-field">
            <span>奖项</span>
            <input v-model="publishForm.awardName" type="text" />
          </label>
          <label class="form-field">
            <span>积分</span>
            <input v-model.number="publishForm.points" type="number" min="1" />
          </label>
          <label class="form-field">
            <span>评审老师</span>
            <input v-model="publishForm.reviewerName" type="text" />
          </label>
          <label class="form-field full-width">
            <span>发布时携带的评审意见</span>
            <textarea v-model="publishForm.reviewComment" rows="3" placeholder="发布结果时一并展示给学生"></textarea>
          </label>
        </div>
        <p v-if="publishError" class="error-text">{{ publishError }}</p>
        <p v-if="publishSuccess" class="success-text">{{ publishSuccess }}</p>
        <button type="button" :disabled="publishing" @click="submitResult">
          {{ publishing ? '发布中...' : '发布比赛结果' }}
        </button>
      </div>

      <div class="task-list">
        <article v-if="!loading && tasks.length === 0" class="task-card empty-card">
          <h3>暂无待评作品</h3>
          <p>当前比赛还没有提交记录，待有作品上传后即可在这里完成评审。</p>
        </article>

        <article v-for="item in tasks" :key="item.submissionId" class="task-card">
          <h3>作品 #{{ item.submissionId }}</h3>
          <p>学生 {{ item.studentId }} · {{ resolveReviewerName(item.reviewerName) }}</p>
          <strong>{{ item.status }}</strong>
          <p v-if="item.reviewComment" class="review-note">{{ item.reviewComment }}</p>
          <small v-if="item.suggestedScore !== null">建议分数 {{ item.suggestedScore }}</small>
          <small v-if="item.reviewedAt">完成时间 {{ formatDateTime(item.reviewedAt) }}</small>
          <button type="button" class="mini-button" @click="pickTask(item)">带入评审与发布表单</button>
          <button type="button" class="mini-button secondary-mini" @click="fillStudentId(item.studentId)">只带入学生</button>
        </article>
      </div>

      <div class="score-list">
        <article v-if="!loading && scores.length === 0" class="task-card empty-card">
          <h3>暂无已发布结果</h3>
          <p>完成评分并发布后，结果会在这里实时展示。</p>
        </article>

        <article v-for="item in scores" :key="item.id" class="task-card">
          <h3>学生 {{ item.studentId }}</h3>
          <p>{{ item.awardName }} · 第 {{ item.rank }} 名 · {{ item.score }} 分</p>
          <strong>奖励 {{ item.points }} 积分</strong>
          <p v-if="item.reviewComment" class="review-note">评审意见：{{ item.reviewComment }}</p>
          <small v-if="item.certificateNo">奖状：{{ item.certificateTitle }} · {{ item.certificateNo }}</small>
          <small>{{ formatDateTime(item.publishedAt) }}</small>
        </article>
      </div>
    </section>
  </section>
</template>

<style scoped>
.page {
  min-height: 100vh;
  padding: 32px;
  background: linear-gradient(180deg, #f4efe8 0%, #eef4f7 100%);
  color: #1f2f3c;
}

.hero {
  margin-bottom: 24px;
}

.eyebrow {
  margin: 0 0 12px;
  color: #9f6230;
  font-size: 14px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.hero h1 {
  margin: 0 0 12px;
  font-size: 36px;
}

.hero p {
  max-width: 760px;
  color: #56697a;
  line-height: 1.7;
}

.board {
  display: grid;
  gap: 24px;
}

.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: end;
  margin-bottom: 24px;
}

.field {
  display: grid;
  gap: 8px;
}

.field span {
  color: #56697a;
  font-size: 14px;
}

.field input,
.field select {
  width: 180px;
  padding: 12px 16px;
  border: 1px solid #d8e0e7;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.88);
  color: #1f2f3c;
}

.toolbar-hint {
  margin: -10px 0 24px;
  color: #56697a;
  font-size: 14px;
}

.publish-card,
.task-card {
  padding: 24px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 24px 60px rgba(33, 50, 68, 0.08);
}

.publish-card button {
  margin-top: 16px;
  padding: 12px 20px;
  border: none;
  border-radius: 999px;
  background: #c86c31;
  color: #fff;
  cursor: pointer;
}

.publish-form {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 12px;
  margin-top: 16px;
}

.form-field {
  display: grid;
  gap: 8px;
}

.form-field span {
  color: #56697a;
  font-size: 13px;
}

.form-field input,
.form-field select,
.form-field textarea {
  padding: 11px 14px;
  border: 1px solid #d8e0e7;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.88);
  color: #1f2f3c;
  resize: vertical;
}

.full-width {
  grid-column: 1 / -1;
}

.task-list,
.score-list {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
}

.task-card h3 {
  margin: 0 0 10px;
}

.task-card p {
  margin: 0 0 12px;
  color: #56697a;
}

.task-card strong {
  color: #9f6230;
}

.task-card small {
  display: block;
  margin-top: 12px;
  color: #7a8793;
}

.review-note {
  margin-top: 12px;
}

.error-text {
  color: #b14a2f;
}

.success-text {
  color: #2f7a49;
}

.empty-card {
  border: 1px dashed #d6dde5;
  box-shadow: none;
}

.mini-button {
  width: fit-content;
  margin-top: 10px;
  padding: 8px 14px;
  border: none;
  border-radius: 999px;
  background: #f3e1cf;
  color: #9f6230;
  cursor: pointer;
}

.secondary-mini {
  margin-left: 8px;
  background: #eef3f8;
  color: #4f6273;
}
</style>
