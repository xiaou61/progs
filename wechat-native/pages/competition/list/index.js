const { fetchCompetitions } = require('../../../services/competition')
const { fetchUserRegistrations } = require('../../../services/registration')
const { getSession } = require('../../../utils/auth')
const {
  buildCompetitionRoute,
  formatCompetitionWindow,
  resolveCompetitionStatusLabel
} = require('../../../utils/competition')

function resolveParticipantTypeLabel(participantType) {
  return participantType === 'TEACHER_ONLY' ? '仅老师参加' : '仅学生参加'
}

Page({
  data: {
    loading: false,
    error: '',
    competitions: [],
    scope: 'all',
    titleText: '比赛列表',
    descText: '这里展示可参与的比赛，以及当前的报名状态和时间窗口。',
    emptyText: '当前还没有可展示的比赛。'
  },

  onLoad(options) {
    const scope = options && options.scope === 'my' ? 'my' : 'all'
    const titleText = scope === 'my' ? '我的比赛' : '比赛列表'
    const descText = scope === 'my'
      ? '这里展示你已报名的比赛，可继续签到、上传作品和查看结果。'
      : '这里展示可参与的比赛，以及当前的报名状态和时间窗口。'
    const emptyText = scope === 'my' ? '你还没有报名任何比赛。' : '当前还没有可展示的比赛。'
    this.setData({
      scope,
      titleText,
      descText,
      emptyText
    })
    if (typeof wx !== 'undefined' && typeof wx.setNavigationBarTitle === 'function') {
      wx.setNavigationBarTitle({
        title: titleText
      })
    }
  },

  onShow() {
    this.loadCompetitions()
  },

  async loadCompetitions() {
    this.setData({
      loading: true,
      error: ''
    })

    try {
      const session = this.data.scope === 'my' ? getSession() : null
      if (this.data.scope === 'my' && (!session || !session.userId)) {
        throw new Error('请先登录后查看我的比赛')
      }
      const [competitions, registrations] = await Promise.all([
        fetchCompetitions(),
        this.data.scope === 'my'
          ? fetchUserRegistrations(session.userId)
          : Promise.resolve([])
      ])
      const activeCompetitionIds = new Set(
        registrations
          .filter((item) => item.status === 'REGISTERED')
          .map((item) => item.competitionId)
      )
      this.setData({
        competitions: competitions
          .filter((item) => this.data.scope !== 'my' || activeCompetitionIds.has(item.id))
          .map((item) => ({
            ...item,
            statusLabel: resolveCompetitionStatusLabel(item),
            signupWindow: formatCompetitionWindow(item.signupStartAt, item.signupEndAt),
            participantTypeLabel: resolveParticipantTypeLabel(item.participantType),
            advisorTeacherText: item.participantType === 'STUDENT_ONLY'
              ? (item.advisorTeacherName || '指导老师待公布')
              : '无需指定指导老师'
          }))
      })
    } catch (error) {
      this.setData({
        error: error instanceof Error ? error.message : '加载比赛失败'
      })
    } finally {
      this.setData({ loading: false })
    }
  },

  openCompetitionDetail(event) {
    const competitionId = event.currentTarget.dataset.id
    wx.navigateTo({
      url: buildCompetitionRoute('detail', competitionId)
    })
  }
})
