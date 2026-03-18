const { fetchDailyTaskOverview } = require('../../services/daily-task')
const { fetchStudentOverview } = require('../../services/result')
const { getSession, requireLogin } = require('../../utils/auth')
const { buildOverviewCards, formatTaskTime, summarizeTaskProgress } = require('../../utils/task')
const { formatCompetitionDateTime } = require('../../utils/competition')

function formatAmount(value) {
  return value > 0 ? `+${value}` : `${value}`
}

Page({
  data: {
    loading: false,
    error: '',
    account: {
      availablePoints: 0,
      totalPoints: 0
    },
    taskProgress: '',
    checkinTime: '今日未完成',
    shareTime: '今日未完成',
    overviewCards: [],
    recordCards: []
  },

  onShow() {
    if (!requireLogin('/pages/points/index')) {
      return
    }
    this.loadPoints()
  },

  async loadPoints() {
    const session = getSession()
    this.setData({
      loading: true,
      error: ''
    })

    try {
      const [overview, dailyOverview] = await Promise.all([
        fetchStudentOverview(session.userId),
        fetchDailyTaskOverview(session.userId)
      ])
      this.setData({
        account: overview.account,
        taskProgress: summarizeTaskProgress(dailyOverview.task),
        checkinTime: formatTaskTime(dailyOverview.task.lastCheckinAt),
        shareTime: formatTaskTime(dailyOverview.task.lastShareAt),
        overviewCards: buildOverviewCards(dailyOverview.overview),
        recordCards: overview.records.map((item) => ({
          id: item.id,
          title: item.remark,
          amount: formatAmount(item.changeAmount),
          time: formatCompetitionDateTime(item.createdAt)
        }))
      })
    } catch (error) {
      this.setData({
        error: error instanceof Error ? error.message : '加载积分数据失败'
      })
    } finally {
      this.setData({ loading: false })
    }
  }
})
