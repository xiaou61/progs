const { fetchCompetitions } = require('../../../services/competition')
const { fetchCompetitionResults, fetchStudentOverview } = require('../../../services/result')
const { resolveCompetitionId } = require('../../../utils/competition')
const { getSession, requireLogin } = require('../../../utils/auth')
const { buildAwardCards } = require('../../../utils/result')

Page({
  data: {
    competitionId: 0,
    loading: false,
    error: '',
    awards: []
  },

  onLoad(options) {
    this.setData({
      competitionId: resolveCompetitionId(options)
    })
  },

  onShow() {
    const redirectUrl = this.data.competitionId
      ? `/pages/competition/result/index?competitionId=${this.data.competitionId}`
      : '/pages/competition/result/index'
    if (!requireLogin(redirectUrl)) {
      return
    }
    this.loadResults()
  },

  async loadResults() {
    const session = getSession()
    const userId = session.userId
    this.setData({
      loading: true,
      error: ''
    })

    try {
      const [results, competitions] = await Promise.all([
        this.data.competitionId
          ? fetchCompetitionResults(this.data.competitionId)
          : fetchStudentOverview(userId).then((overview) => overview.results),
        fetchCompetitions()
      ])
      this.setData({
        awards: buildAwardCards({
          competitionId: this.data.competitionId,
          results,
          competitions
        })
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
