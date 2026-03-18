const { buildHomeMenus } = require('./utils/home')
const { loadSession, saveSession, clearSession } = require('./utils/session')

App({
  globalData: {
    session: null,
    homeMenus: buildHomeMenus('STUDENT'),
    apiBaseUrl: 'http://127.0.0.1:8080'
  },

  onLaunch() {
    const session = loadSession()
    this.globalData.session = session
    this.globalData.homeMenus = buildHomeMenus(session ? session.roleCode : 'STUDENT')
  },

  setSession(session) {
    this.globalData.session = session
    this.globalData.homeMenus = buildHomeMenus(session.roleCode)
    saveSession(session)
  },

  clearSession() {
    this.globalData.session = null
    this.globalData.homeMenus = buildHomeMenus('STUDENT')
    clearSession()
  }
})
