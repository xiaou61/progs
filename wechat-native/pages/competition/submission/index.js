const { fetchCompetitions } = require('../../../services/competition')
const {
  fetchCompetitionSubmissions,
  submitCompetitionWork
} = require('../../../services/submission')
const { formatCompetitionDateTime, resolveCompetitionId } = require('../../../utils/competition')
const { getSession, requireLogin } = require('../../../utils/auth')

function buildVersionCards(items, userId) {
  return items
    .filter((item) => item.userId === userId)
    .sort((left, right) => right.versionNo - left.versionNo)
    .map((item, index) => ({
      id: item.id,
      label: `v${item.versionNo}`,
      fileName: item.fileUrl,
      status: index === 0 ? '当前版本' : '历史版本',
      submittedAt: formatCompetitionDateTime(item.submittedAt)
    }))
}

Page({
  data: {
    competitionId: 0,
    competitionTitle: '未选择比赛',
    competitionDesc: '支持重新上传并保留版本号，最终以最新版本作为评审依据。',
    fileUrl: '',
    reuploadAllowed: false,
    loading: false,
    submitting: false,
    error: '',
    success: '',
    versionCards: []
  },

  onLoad(options) {
    this.setData({
      competitionId: resolveCompetitionId(options)
    })
  },

  onShow() {
    if (!this.data.competitionId) {
      this.setData({
        error: '缺少比赛编号，请从比赛列表重新进入。'
      })
      return
    }
    const redirectUrl = `/pages/competition/submission/index?competitionId=${this.data.competitionId}`
    if (!requireLogin(redirectUrl)) {
      return
    }
    this.loadSubmissionState()
  },

  updateField(event) {
    const field = event.currentTarget.dataset.field
    const value = event.detail.value
    this.setData({
      [field]: value
    })
  },

  async loadSubmissionState() {
    const session = getSession()
    this.setData({
      loading: true,
      error: ''
    })

    try {
      const [competitions, submissionItems] = await Promise.all([
        fetchCompetitions(),
        fetchCompetitionSubmissions(this.data.competitionId)
      ])
      const currentCompetition = competitions.find((item) => item.id === this.data.competitionId)
      const versionCards = buildVersionCards(submissionItems, session.userId)
      this.setData({
        competitionTitle: currentCompetition ? currentCompetition.title : this.data.competitionTitle,
        competitionDesc: currentCompetition ? currentCompetition.description : this.data.competitionDesc,
        versionCards,
        fileUrl: versionCards.length > 0 ? versionCards[0].fileName : ''
      })
    } catch (error) {
      this.setData({
        error: error instanceof Error ? error.message : '加载作品记录失败'
      })
    } finally {
      this.setData({ loading: false })
    }
  },

  async submitWork() {
    const session = getSession()
    const trimmedFileUrl = this.data.fileUrl.trim()
    this.setData({
      submitting: true,
      error: '',
      success: ''
    })

    if (!trimmedFileUrl) {
      this.setData({
        submitting: false,
        error: '请输入作品文件地址'
      })
      return
    }

    try {
      const submissionId = await submitCompetitionWork({
        competitionId: this.data.competitionId,
        userId: session.userId,
        fileUrl: trimmedFileUrl,
        reuploadAllowed: this.data.reuploadAllowed
      })
      this.setData({
        success: `作品提交成功，记录编号 #${submissionId}`
      })
      await this.loadSubmissionState()
    } catch (error) {
      this.setData({
        error: error instanceof Error ? error.message : '作品提交失败'
      })
    } finally {
      this.setData({ submitting: false })
    }
  }
})
