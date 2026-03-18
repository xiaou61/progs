const { fetchCompetitionDetail } = require('../../../services/competition')
const { completeCompetitionShare } = require('../../../services/daily-task')
const { consultCompetitionOrganizer } = require('../../../services/message')
const { getSession, isLoggedIn, requireLogin } = require('../../../utils/auth')
const {
  buildCompetitionRoute,
  formatCompetitionWindow,
  resolveCompetitionId,
  resolveCompetitionStatusLabel
} = require('../../../utils/competition')
const { buildMessageChatRoute } = require('../../../utils/message')

Page({
  data: {
    competitionId: 1,
    loading: false,
    error: '',
    shareLoading: false,
    shareMessage: '',
    shareError: '',
    consultLoading: false,
    consultError: '',
    detail: null,
    statusLabel: '',
    signupWindow: '',
    competitionWindow: '',
    actionButtons: [
      { key: 'register', label: '立即报名' },
      { key: 'checkin', label: '现场签到' },
      { key: 'submission', label: '上传作品' },
      { key: 'result', label: '查看结果' }
    ]
  },

  onLoad(options) {
    this.setData({
      competitionId: resolveCompetitionId(options)
    })
  },

  onShow() {
    this.loadCompetitionDetail()
  },

  async loadCompetitionDetail() {
    this.setData({
      loading: true,
      error: ''
    })

    try {
      const detail = await fetchCompetitionDetail(this.data.competitionId)
      this.setData({
        detail,
        statusLabel: resolveCompetitionStatusLabel(detail),
        signupWindow: formatCompetitionWindow(detail.signupStartAt, detail.signupEndAt),
        competitionWindow: formatCompetitionWindow(detail.startAt, detail.endAt)
      })
    } catch (error) {
      this.setData({
        error: error instanceof Error ? error.message : '加载比赛详情失败'
      })
    } finally {
      this.setData({ loading: false })
    }
  },

  openAction(event) {
    const target = event.currentTarget.dataset.target
    const targetUrl = buildCompetitionRoute(target, this.data.competitionId)
    wx.navigateTo({
      url: isLoggedIn() ? targetUrl : `/pages/login/index?redirect=${encodeURIComponent(targetUrl)}`
    })
  },

  async handleShareTask() {
    const redirectUrl = `/pages/competition/detail/index?competitionId=${this.data.competitionId}`
    if (!requireLogin(redirectUrl)) {
      return
    }

    const session = getSession()
    this.setData({
      shareLoading: true,
      shareError: '',
      shareMessage: ''
    })

    try {
      const result = await completeCompetitionShare(session.userId, this.data.competitionId)
      this.setData({
        shareMessage: `分享任务完成，已到账 ${result.changeAmount} 积分`
      })
    } catch (error) {
      this.setData({
        shareError: error instanceof Error ? error.message : '分享积分领取失败'
      })
    } finally {
      this.setData({ shareLoading: false })
    }
  },

  async handleConsultTeacher() {
    const redirectUrl = `/pages/competition/detail/index?competitionId=${this.data.competitionId}`
    if (!requireLogin(redirectUrl)) {
      return
    }
    if (!this.data.detail) {
      return
    }

    const session = getSession()
    this.setData({
      consultLoading: true,
      consultError: ''
    })

    try {
      const result = await consultCompetitionOrganizer({
        competitionId: this.data.competitionId,
        userId: session.userId,
        content: `老师您好，我想咨询《${this.data.detail.title}》的报名要求。`
      })
      wx.navigateTo({
        url: buildMessageChatRoute({
          type: 'PRIVATE',
          peerUserId: result.peerUserId,
          title: result.peerName
        })
      })
    } catch (error) {
      this.setData({
        consultError: error instanceof Error ? error.message : '发起咨询失败'
      })
    } finally {
      this.setData({ consultLoading: false })
    }
  }
})
