const { checkInCompetition, fetchCompetitionCheckins } = require('../../../services/checkin')
const { fetchCompetitions } = require('../../../services/competition')
const { formatCompetitionDateTime, resolveCompetitionId } = require('../../../utils/competition')
const { getSession, requireLogin } = require('../../../utils/auth')

Page({
  data: {
    competitionId: 0,
    competitionTitle: '未选择比赛',
    competitionDesc: '请从比赛列表进入后再完成签到。',
    checkedAt: '',
    loading: false,
    submitting: false,
    error: '',
    statusLabel: '待签到',
    tips: ['到达现场后扫码签到', '签到成功后可继续提交作品', '若状态异常请联系带队老师']
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
      const [competitions, checkins] = await Promise.all([
        fetchCompetitions(),
        fetchCompetitionCheckins(this.data.competitionId)
      ])
      const currentCompetition = competitions.find((item) => item.id === this.data.competitionId)
      const currentCheckin = checkins.find((item) => item.userId === session.userId)
      this.setData({
        competitionTitle: currentCompetition ? currentCompetition.title : this.data.competitionTitle,
        competitionDesc: currentCompetition ? currentCompetition.description : this.data.competitionDesc,
        checkedAt: currentCheckin ? formatCompetitionDateTime(currentCheckin.checkedAt) : '',
        statusLabel: currentCheckin ? '已签到' : '待签到'
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
