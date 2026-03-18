const { fetchCompetitions } = require('../../../services/competition')
const {
  buildCompetitionRoute,
  formatCompetitionWindow,
  resolveCompetitionStatusLabel
} = require('../../../utils/competition')

Page({
  data: {
    loading: false,
    error: '',
    competitions: []
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
      const competitions = await fetchCompetitions()
      this.setData({
        competitions: competitions.map((item) => ({
          ...item,
          statusLabel: resolveCompetitionStatusLabel(item),
          signupWindow: formatCompetitionWindow(item.signupStartAt, item.signupEndAt)
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
