const { fetchDailyTaskOverview } = require('../../services/daily-task')
const {
  cancelAccount,
  changePassword,
  fetchProfile,
  submitFeedback,
  updateProfile
} = require('../../services/profile')
const { getSession, logoutSession, requireLogin } = require('../../utils/auth')
const { buildOverviewCards, formatTaskTime, summarizeTaskProgress } = require('../../utils/task')
const { buildLoginRoute } = require('../../utils/routes')

function buildEmptyProfile() {
  return {
    userId: 0,
    studentNo: '',
    realName: '',
    phone: '',
    avatarUrl: '',
    campusName: '',
    gradeName: '',
    majorName: '',
    departmentName: '',
    bio: '',
    roleCode: 'STUDENT',
    notifyResult: true,
    notifyPoints: true,
    allowPrivateMessage: true,
    publicCompetition: true,
    publicPoints: true,
    publicSubmission: true,
    status: 'ENABLED'
  }
}

Page({
  data: {
    loading: false,
    overviewLoading: false,
    saving: false,
    passwordSaving: false,
    feedbackSaving: false,
    cancelSaving: false,
    error: '',
    success: '',
    overviewError: '',
    taskProgress: '今日任务状态暂未加载',
    checkinTime: '今日未完成',
    shareTime: '今日未完成',
    overviewCards: [],
    profile: buildEmptyProfile(),
    passwordForm: {
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    },
    feedbackForm: {
      content: '',
      imageUrls: ''
    },
    cancelForm: {
      confirmText: ''
    }
  },

  onShow() {
    if (!requireLogin('/pages/profile/index')) {
      return
    }

    this.loadProfile()
    this.loadDailyOverview()
  },

  updateField(event) {
    const field = event.currentTarget.dataset.field
    this.setData({
      [field]: event.detail.value
    })
  },

  updateSwitch(event) {
    const field = event.currentTarget.dataset.field
    this.setData({
      [field]: event.detail.value
    })
  },

  resetNotice() {
    this.setData({
      error: '',
      success: ''
    })
  },

  async loadProfile() {
    const session = getSession()
    this.setData({
      loading: true,
      error: '',
      success: ''
    })

    try {
      const profile = await fetchProfile(session.userId)
      this.setData({ profile })
    } catch (error) {
      this.setData({
        error: error instanceof Error ? error.message : '加载个人资料失败'
      })
    } finally {
      this.setData({ loading: false })
    }
  },

  async loadDailyOverview() {
    const session = getSession()
    this.setData({
      overviewLoading: true,
      overviewError: ''
    })

    try {
      const result = await fetchDailyTaskOverview(session.userId)
      this.setData({
        taskProgress: summarizeTaskProgress(result.task),
        checkinTime: formatTaskTime(result.task.lastCheckinAt),
        shareTime: formatTaskTime(result.task.lastShareAt),
        overviewCards: buildOverviewCards(result.overview)
      })
    } catch (error) {
      this.setData({
        overviewError: error instanceof Error ? error.message : '加载比赛汇总失败'
      })
    } finally {
      this.setData({ overviewLoading: false })
    }
  },

  async saveProfile() {
    this.resetNotice()
    if (!this.data.profile.realName.trim()) {
      this.setData({ error: '姓名不能为空' })
      return
    }
    if (!this.data.profile.phone.trim()) {
      this.setData({ error: '手机号不能为空' })
      return
    }

    const session = getSession()
    this.setData({ saving: true })
    try {
      const profile = await updateProfile({
        userId: session.userId,
        realName: this.data.profile.realName.trim(),
        phone: this.data.profile.phone.trim(),
        avatarUrl: this.data.profile.avatarUrl.trim(),
        campusName: this.data.profile.campusName.trim(),
        gradeName: this.data.profile.gradeName.trim(),
        majorName: this.data.profile.majorName.trim(),
        departmentName: this.data.profile.departmentName.trim(),
        bio: this.data.profile.bio.trim(),
        notifyResult: this.data.profile.notifyResult,
        notifyPoints: this.data.profile.notifyPoints,
        allowPrivateMessage: this.data.profile.allowPrivateMessage,
        publicCompetition: this.data.profile.publicCompetition,
        publicPoints: this.data.profile.publicPoints,
        publicSubmission: this.data.profile.publicSubmission
      })
      this.setData({
        profile,
        success: '个人资料已保存'
      })
    } catch (error) {
      this.setData({
        error: error instanceof Error ? error.message : '保存个人资料失败'
      })
    } finally {
      this.setData({ saving: false })
    }
  },

  async savePassword() {
    this.resetNotice()
    if (!this.data.passwordForm.oldPassword.trim()) {
      this.setData({ error: '旧密码不能为空' })
      return
    }
    if (this.data.passwordForm.newPassword.length < 8) {
      this.setData({ error: '新密码长度不能少于 8 位' })
      return
    }
    if (this.data.passwordForm.newPassword !== this.data.passwordForm.confirmPassword) {
      this.setData({ error: '两次输入的新密码不一致' })
      return
    }

    const session = getSession()
    this.setData({ passwordSaving: true })
    try {
      await changePassword({
        userId: session.userId,
        oldPassword: this.data.passwordForm.oldPassword,
        newPassword: this.data.passwordForm.newPassword
      })
      this.setData({
        passwordForm: {
          oldPassword: '',
          newPassword: '',
          confirmPassword: ''
        },
        success: '密码修改成功'
      })
    } catch (error) {
      this.setData({
        error: error instanceof Error ? error.message : '修改密码失败'
      })
    } finally {
      this.setData({ passwordSaving: false })
    }
  },

  normalizeImageUrls() {
    return this.data.feedbackForm.imageUrls
      .split(/\r?\n|,/)
      .map((item) => item.trim())
      .filter(Boolean)
  },

  async saveFeedback() {
    this.resetNotice()
    if (!this.data.feedbackForm.content.trim()) {
      this.setData({ error: '反馈内容不能为空' })
      return
    }

    const session = getSession()
    this.setData({ feedbackSaving: true })
    try {
      await submitFeedback({
        userId: session.userId,
        content: this.data.feedbackForm.content.trim(),
        imageUrls: this.normalizeImageUrls()
      })
      this.setData({
        feedbackForm: {
          content: '',
          imageUrls: ''
        },
        success: '反馈已提交，感谢你的建议'
      })
    } catch (error) {
      this.setData({
        error: error instanceof Error ? error.message : '提交反馈失败'
      })
    } finally {
      this.setData({ feedbackSaving: false })
    }
  },

  async handleCancelAccount() {
    this.resetNotice()
    if (this.data.cancelForm.confirmText.trim() !== '确认注销') {
      this.setData({ error: '请输入确认注销' })
      return
    }

    const session = getSession()
    this.setData({ cancelSaving: true })
    try {
      await cancelAccount({
        userId: session.userId,
        confirmText: this.data.cancelForm.confirmText.trim()
      })
      logoutSession()
      wx.reLaunch({
        url: buildLoginRoute()
      })
    } catch (error) {
      this.setData({
        error: error instanceof Error ? error.message : '注销账号失败'
      })
    } finally {
      this.setData({ cancelSaving: false })
    }
  }
})
