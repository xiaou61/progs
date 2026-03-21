const { checkInCompetition, fetchCompetitionCheckins } = require('../../../services/checkin')
const { fetchCompetitions } = require('../../../services/competition')
const { fetchUserRegistration } = require('../../../services/registration')
const { formatCompetitionDateTime, resolveCompetitionId } = require('../../../utils/competition')
const { getSession, requireLogin } = require('../../../utils/auth')

function buildCheckinViewState(registration, currentCheckin) {
  if (!registration || registration.status !== 'REGISTERED') {
    return {
      statusLabel: '未报名',
      statusDesc: '请先完成报名，再提交签到申请。',
      submitButtonText: '暂不可签到',
      canSubmit: false
    }
  }
  if (registration.attendanceStatus === 'PRESENT' || (currentCheckin && currentCheckin.status === 'APPROVED')) {
    return {
      statusLabel: '已确认到场',
      statusDesc: '老师已经确认你的签到申请，本场比赛签到完成。',
      submitButtonText: '已确认到场',
      canSubmit: false
    }
  }
  if (registration.attendanceStatus === 'ABSENT') {
    return {
      statusLabel: '已缺席',
      statusDesc: '老师已将当前报名标记为缺席，如有疑问请联系老师处理。',
      submitButtonText: '已标记缺席',
      canSubmit: false
    }
  }
  if (currentCheckin && currentCheckin.status === 'PENDING') {
    return {
      statusLabel: '待老师确认',
      statusDesc: '签到申请已提交，请等待老师在后台确认。',
      submitButtonText: '已提交申请',
      canSubmit: false
    }
  }
  if (currentCheckin && currentCheckin.status === 'REJECTED') {
    return {
      statusLabel: '申请已驳回',
      statusDesc: '申请已驳回，可重新提交。',
      submitButtonText: '重新提交签到申请',
      canSubmit: true
    }
  }
  return {
    statusLabel: '待提交',
    statusDesc: '到达现场后提交签到申请，等待老师确认后才算到场。',
    submitButtonText: '提交签到申请',
    canSubmit: true
  }
}

Page({
  data: {
    competitionId: 0,
    competitionTitle: '未选择比赛',
    competitionDesc: '请从比赛列表进入后再完成签到。',
    checkedAt: '',
    reviewedAt: '',
    reviewRemark: '',
    loading: false,
    submitting: false,
    error: '',
    statusLabel: '待提交',
    statusDesc: '到达现场后提交签到申请，等待老师确认后才算到场。',
    submitButtonText: '提交签到申请',
    canSubmit: true,
    tips: ['提交申请后不会立即算到场', '老师确认后后台才会显示已到场', '若申请被驳回，可查看原因后重新提交']
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
    const redirectUrl = `/pages/competition/checkin/index?competitionId=${this.data.competitionId}`
    if (!requireLogin(redirectUrl)) {
      return
    }
    this.loadCheckinStatus()
  },

  async loadCheckinStatus() {
    const session = getSession()
    this.setData({
      loading: true,
      error: ''
    })

    try {
      const [competitions, checkins, registration] = await Promise.all([
        fetchCompetitions(),
        fetchCompetitionCheckins(this.data.competitionId),
        fetchUserRegistration(this.data.competitionId, session.userId)
      ])
      const currentCompetition = competitions.find((item) => item.id === this.data.competitionId)
      const currentCheckin = checkins.find((item) => item.userId === session.userId)
      const viewState = buildCheckinViewState(registration, currentCheckin)
      this.setData({
        competitionTitle: currentCompetition ? currentCompetition.title : this.data.competitionTitle,
        competitionDesc: currentCompetition ? currentCompetition.description : this.data.competitionDesc,
        checkedAt: currentCheckin ? formatCompetitionDateTime(currentCheckin.checkedAt) : '',
        reviewedAt: currentCheckin && currentCheckin.reviewedAt ? formatCompetitionDateTime(currentCheckin.reviewedAt) : '',
        reviewRemark: currentCheckin && currentCheckin.reviewRemark ? currentCheckin.reviewRemark : '',
        statusLabel: viewState.statusLabel,
        statusDesc: viewState.statusDesc,
        submitButtonText: viewState.submitButtonText,
        canSubmit: viewState.canSubmit
      })
    } catch (error) {
      this.setData({
        error: error instanceof Error ? error.message : '加载签到状态失败'
      })
    } finally {
      this.setData({ loading: false })
    }
  },

  async submitCheckin() {
    const session = getSession()
    this.setData({
      submitting: true,
      error: ''
    })

    try {
      await checkInCompetition({
        competitionId: this.data.competitionId,
        userId: session.userId,
        method: 'QRCODE'
      })
      this.setData({
        reviewRemark: ''
      })
      await this.loadCheckinStatus()
    } catch (error) {
      this.setData({
        error: error instanceof Error ? error.message : '签到失败'
      })
    } finally {
      this.setData({ submitting: false })
    }
  }
})
