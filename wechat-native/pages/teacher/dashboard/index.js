const { exportTeacherDashboardCsv, fetchTeacherDashboard } = require('../../../services/dashboard')
const { getRoleCode, getSession, requireLogin } = require('../../../utils/auth')

function buildOverviewCards(summary) {
  return [
    { label: '我的比赛', value: `${summary.overview.competitionCount} 场` },
    { label: '已发布', value: `${summary.overview.publishedCompetitionCount} 场` },
    { label: '报名人次', value: `${summary.overview.totalRegistrationCount} 人` },
    { label: '作品提交', value: `${summary.overview.totalSubmissionCount} 份` },
    { label: '获奖记录', value: `${summary.overview.totalAwardCount} 条` },
    { label: '奖励积分', value: `${summary.overview.totalAwardPoints} 分` }
  ]
}

Page({
  data: {
    loading: false,
    errorMessage: '',
    successMessage: '',
    summary: null,
    overviewCards: []
  },

  onShow() {
    if (!requireLogin('/pages/teacher/dashboard/index')) {
      return
    }
    this.loadDashboard()
  },

  async loadDashboard() {
    if (getRoleCode() !== 'TEACHER') {
      this.setData({
        errorMessage: '当前账号不是老师，暂不支持查看老师看板',
        summary: null,
        overviewCards: []
      })
      return
    }

    const session = getSession()
    this.setData({
      loading: true,
      errorMessage: '',
      successMessage: ''
    })

    try {
      const summary = await fetchTeacherDashboard(session.userId)
      this.setData({
        summary,
        overviewCards: buildOverviewCards(summary)
      })
    } catch (error) {
      this.setData({
        errorMessage: error instanceof Error ? error.message : '加载老师看板失败'
      })
    } finally {
      this.setData({ loading: false })
    }
  },

  async copyCsv() {
    if (getRoleCode() !== 'TEACHER') {
      return
    }

    const session = getSession()
    this.setData({
      errorMessage: '',
      successMessage: ''
    })

    try {
      const csv = await exportTeacherDashboardCsv(session.userId)
      wx.setClipboardData({
        data: csv,
        success: () => {
          this.setData({
            successMessage: '看板 CSV 已复制到剪贴板'
          })
        },
        fail: () => {
          this.setData({
            errorMessage: '复制导出内容失败'
          })
        }
      })
    } catch (error) {
      this.setData({
        errorMessage: error instanceof Error ? error.message : '导出老师看板失败'
      })
    }
  }
})
