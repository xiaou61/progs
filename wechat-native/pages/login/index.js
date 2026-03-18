const { login } = require('../../services/auth')
const { applyLoginSession, isLoggedIn } = require('../../utils/auth')
const { resolvePostLoginRoute } = require('../../utils/routes')

Page({
  data: {
    redirectUrl: '',
    loading: false,
    error: '',
    roleCode: 'STUDENT',
    studentNo: '',
    password: ''
  },

  onLoad(options) {
    this.setData({
      redirectUrl: typeof options.redirect === 'string' ? decodeURIComponent(options.redirect) : ''
    })
  },

  onShow() {
    if (isLoggedIn()) {
      wx.reLaunch({
        url: resolvePostLoginRoute(this.data.redirectUrl)
      })
    }
  },

  updateField(event) {
    const field = event.currentTarget.dataset.field
    this.setData({
      [field]: event.detail.value,
      error: ''
    })
  },

  changeRole(event) {
    this.setData({
      roleCode: event.detail.value,
      error: ''
    })
  },

  async submitLogin() {
    if (!this.data.studentNo.trim()) {
      this.setData({ error: '学号不能为空' })
      return
    }
    if (!this.data.password.trim()) {
      this.setData({ error: '密码不能为空' })
      return
    }

    this.setData({
      loading: true,
      error: ''
    })

    try {
      const result = await login({
        studentNo: this.data.studentNo.trim(),
        password: this.data.password,
        roleCode: this.data.roleCode
      })
      applyLoginSession(result)
      wx.reLaunch({
        url: resolvePostLoginRoute(this.data.redirectUrl)
      })
    } catch (error) {
      this.setData({
        error: error instanceof Error ? error.message : '登录失败'
      })
    } finally {
      this.setData({ loading: false })
    }
  }
})
