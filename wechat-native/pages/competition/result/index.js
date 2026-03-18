const { fetchCompetitions } = require('../../../services/competition')
const { fetchStudentOverview } = require('../../../services/result')
const { formatCompetitionDateTime } = require('../../../utils/competition')
const { getSession, requireLogin } = require('../../../utils/auth')

Page({
  data: {
    loading: false,
    error: '',
    awards: []
  },

  onShow() {
    if (!requireLogin('/pages/competition/result/index')) {
      return
    }
    this.loadResults()
  },

  async loadResults() {
    const session = getSession()
    this.setData({
      loading: true,
      error: ''
    })

    try {
      const [overview, competitions] = await Promise.all([
        fetchStudentOverview(session.userId),
        fetchCompetitions()
      ])
      const titleMap = Object.fromEntries(competitions.map((item) => [item.id, item.title]))
      this.setData({
        awards: overview.results.map((item) => ({
          ...item,
          title: titleMap[item.competitionId] || `比赛 #${item.competitionId}`,
          rankText: `第 ${item.rank} 名`,
          publishedAtText: formatCompetitionDateTime(item.publishedAt)
        }))
      })
    } catch (error) {
      this.setData({
        error: error instanceof Error ? error.message : '加载比赛结果失败'
      })
    } finally {
      this.setData({ loading: false })
    }
  }
})
