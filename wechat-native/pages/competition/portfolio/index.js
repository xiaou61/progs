const { fetchCompetitions } = require('../../../services/competition')
const { fetchStudentOverview } = require('../../../services/result')
const { fetchUserSubmissions } = require('../../../services/submission')
const { getSession, requireLogin } = require('../../../utils/auth')
const { buildPortfolioState, resolvePortfolioTab } = require('../../../utils/portfolio')

Page({
  data: {
    loading: false,
    error: '',
    activeTab: 'works',
    sections: [],
    workCards: [],
    awardCards: []
  },

  onLoad(options) {
    this.setData({
      activeTab: resolvePortfolioTab(options && options.tab)
    })
  },

  onShow() {
    const redirectUrl = `/pages/competition/portfolio/index?tab=${this.data.activeTab}`
    if (!requireLogin(redirectUrl)) {
      return
    }
    this.loadPortfolio()
  },

  async loadPortfolio() {
    const session = getSession()
    this.setData({
      loading: true,
      error: ''
    })

    try {
      const [competitions, submissions, overview] = await Promise.all([
        fetchCompetitions(),
        fetchUserSubmissions(session.userId),
        fetchStudentOverview(session.userId)
      ])
      this.setData(buildPortfolioState({
        tab: this.data.activeTab,
        competitions,
        submissions,
        results: overview.results
      }))
    } catch (error) {
      this.setData({
        error: error instanceof Error ? error.message : '加载我的成果失败'
      })
    } finally {
      this.setData({ loading: false })
    }
  },

  switchTab(event) {
    const nextTab = resolvePortfolioTab(event.currentTarget.dataset.tab)
    this.setData({
      activeTab: nextTab
    })
    this.loadPortfolio()
  },

  openWorkCard(event) {
    const url = event.currentTarget.dataset.url
    if (!url) {
      return
    }
    wx.navigateTo({ url })
  },

  openAwardCard(event) {
    const url = event.currentTarget.dataset.url
    if (!url) {
      return
    }
    wx.navigateTo({ url })
  }
})
