const { completeDailyCheckin, fetchDailyTaskOverview } = require('../../services/daily-task')
const { buildUrl } = require('../../services/http')
const { fetchHomeBanners } = require('../../services/system')
const { getRoleCode, getSession, logoutSession, requireLogin } = require('../../utils/auth')
const { buildHomeMenus, HOME_MENU_TITLE_MAP, resolveHomeRoute, resolveOverviewRoute } = require('../../utils/home')
const { buildOverviewCards, resolveDailyCheckinLabel, resolveShareTaskLabel, summarizeTaskProgress } = require('../../utils/task')
const { buildLoginRoute } = require('../../utils/routes')

function buildMenuCards(roleCode) {
  return buildHomeMenus(roleCode).map((key) => ({
    key,
    title: HOME_MENU_TITLE_MAP[key] || key
  }))
}

function resolveDisplayName(session) {
  if (!session) {
    return '未登录用户'
  }
  const realName = typeof session.realName === 'string' ? session.realName.trim() : ''
  if (realName) {
    return realName
  }
  const studentNo = typeof session.studentNo === 'string' ? session.studentNo.trim() : ''
  if (studentNo) {
    return studentNo
  }
  return `用户 ${session.userId}`
}

Page({
  data: {
    loading: false,
    error: '',
    success: '',
    roleCode: 'STUDENT',
    userId: 0,
    displayName: '',
    menuCards: [],
    banners: [],
    taskProgress: '登录后可查看今日任务状态',
    checkinLabel: '立即签到',
    shareLabel: '分享比赛得积分',
    overviewCards: []
  },

  onShow() {
    if (!requireLogin('/pages/home/index')) {
      return
    }

    const session = getSession()
    const roleCode = getRoleCode()
    this.setData({
      roleCode,
      userId: session.userId,
      displayName: resolveDisplayName(session),
      menuCards: buildMenuCards(roleCode)
    })
    this.loadBanners()
    this.loadDailyOverview()
  },

  async loadBanners() {
    try {
      const banners = await fetchHomeBanners()
      this.setData({
        banners: banners.map((item) => ({
          ...item,
          imageUrl: item.imageUrl ? buildUrl(item.imageUrl) : '',
          jumpPath: item.jumpPath || ''
        }))
      })
    } catch (error) {
      this.setData({
        banners: []
      })
    }
  },

  async loadDailyOverview() {
    const session = getSession()
    this.setData({
      loading: true,
      error: '',
      success: ''
    })

    try {
      const result = await fetchDailyTaskOverview(session.userId)
      this.setData({
        taskProgress: summarizeTaskProgress(result.task),
        checkinLabel: resolveDailyCheckinLabel(Boolean(result.task.dailyCheckinDone)),
        shareLabel: resolveShareTaskLabel(Boolean(result.task.competitionShareDone)),
        overviewCards: buildOverviewCards(result.overview)
      })
    } catch (error) {
      this.setData({
        error: error instanceof Error ? error.message : '加载每日任务失败'
      })
    } finally {
      this.setData({ loading: false })
    }
  },

  openMenu(event) {
    const menuKey = event.currentTarget.dataset.key
    wx.navigateTo({
      url: resolveHomeRoute(menuKey)
    })
  },

  openOverviewCard(event) {
    const cardKey = event.currentTarget.dataset.key
    wx.navigateTo({
      url: resolveOverviewRoute(cardKey)
    })
  },

  openBanner(event) {
    const jumpPath = event.currentTarget.dataset.path
    if (!jumpPath) {
      return
    }
    wx.navigateTo({
      url: jumpPath
    })
  },

  async handleDailyCheckin() {
    const session = getSession()
    if (this.data.checkinLabel === '今日已签到') {
      this.setData({ success: '今日签到已完成' })
      return
    }

    this.setData({
      loading: true,
      error: '',
      success: ''
    })

    try {
      const result = await completeDailyCheckin(session.userId)
      this.setData({
        success: `签到成功，已到账 ${result.changeAmount} 积分`
      })
      await this.loadDailyOverview()
    } catch (error) {
      this.setData({
        error: error instanceof Error ? error.message : '签到失败'
      })
    } finally {
      this.setData({ loading: false })
    }
  },

  openCompetitionList() {
    wx.navigateTo({
      url: resolveHomeRoute('competition-list')
    })
  },

  logout() {
    logoutSession()
    wx.reLaunch({
      url: buildLoginRoute()
    })
  }
})
