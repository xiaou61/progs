const { fetchCompetitions } = require('../../../services/competition')
const {
  cancelRegistration,
  fetchUserRegistration,
  registerCompetition
} = require('../../../services/registration')
const { getRoleCode, getSession, requireLogin } = require('../../../utils/auth')
const { resolveCompetitionId } = require('../../../utils/competition')

function buildStatusText(registration) {
  if (!registration) {
    return '未报名'
  }
  if (registration.status === 'REGISTERED') {
    return '已报名'
  }
  if (registration.status === 'CANCELLED') {
    return '已取消'
  }
  return '已驳回'
}

function buildAttendanceText(registration) {
  if (!registration) {
    return '待签到'
  }
  if (registration.attendanceStatus === 'PRESENT') {
    return '已到场'
  }
  if (registration.attendanceStatus === 'ABSENT') {
    return '已缺席'
  }
  return '待签到'
}

Page({
  data: {
    competitionId: 1,
    competitionTitle: '比赛 #1',
    competitionDesc: '系统将使用当前账号资料完成比赛报名。',
    loading: false,
    submitting: false,
    error: '',
    success: '',
    userId: 0,
    roleCode: 'STUDENT',
    registration: null,
    statusText: '未报名',
    attendanceText: '待签到',
    hasActiveRegistration: false
  },

  onLoad(options) {
    this.setData({
      competitionId: resolveCompetitionId(options)
    })
  },

  onShow() {
    const redirectUrl = `/pages/competition/register/index?competitionId=${this.data.competitionId}`
    if (!requireLogin(redirectUrl)) {
      return
    }

    const session = getSession()
    this.setData({
      userId: session.userId,
      roleCode: getRoleCode()
    })
    this.loadPageData()
  },

  async loadPageData() {
    await Promise.all([this.loadCompetition(), this.loadRegistration()])
  },

  async loadCompetition() {
    this.setData({
      loading: true,
      error: ''
    })

    try {
      const competitions = await fetchCompetitions()
      const current = competitions.find((item) => item.id === this.data.competitionId)
      if (current) {
        this.setData({
          competitionTitle: current.title,
          competitionDesc: current.description
        })
      }
    } catch (error) {
      this.setData({
        error: error instanceof Error ? error.message : '加载比赛信息失败'
      })
    } finally {
      this.setData({ loading: false })
    }
  },

  async loadRegistration() {
    const session = getSession()
    try {
      const registration = await fetchUserRegistration(this.data.competitionId, session.userId)
      this.setData({
        registration,
        statusText: buildStatusText(registration),
        attendanceText: buildAttendanceText(registration),
        hasActiveRegistration: Boolean(registration && registration.status === 'REGISTERED')
      })
    } catch (error) {
      this.setData({
        error: error instanceof Error ? error.message : '加载报名状态失败'
      })
    }
  },

  async submitRegistration() {
    const session = getSession()
    this.setData({
      submitting: true,
      error: '',
      success: ''
    })

    try {
      const registrationId = await registerCompetition({
        competitionId: this.data.competitionId,
        userId: session.userId
      })
      this.setData({
        success: `报名成功，记录编号 #${registrationId}`
      })
      await this.loadRegistration()
    } catch (error) {
      this.setData({
        error: error instanceof Error ? error.message : '提交报名失败'
      })
    } finally {
      this.setData({ submitting: false })
    }
  },

  async submitCancel() {
    const session = getSession()
    if (!this.data.registration) {
      return
    }

    this.setData({
      submitting: true,
      error: '',
      success: ''
    })

    try {
      await cancelRegistration(this.data.registration.id, session.userId)
      this.setData({
        success: `已取消报名，记录编号 #${this.data.registration.id}`
      })
      await this.loadRegistration()
    } catch (error) {
      this.setData({
        error: error instanceof Error ? error.message : '取消报名失败'
      })
    } finally {
      this.setData({ submitting: false })
    }
  }
})
