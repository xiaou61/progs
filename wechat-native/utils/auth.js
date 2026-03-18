const { buildHomeMenus } = require('./home')
const { buildLoginRoute } = require('./routes')
const { clearSession, loadSession, saveSession } = require('./session')

function getAppSafe() {
  if (typeof getApp !== 'function') {
    return null
  }
  try {
    return getApp()
  } catch (error) {
    return null
  }
}

function getSession() {
  const app = getAppSafe()
  if (app && app.globalData && app.globalData.session) {
    return app.globalData.session
  }
  return loadSession()
}

function isLoggedIn() {
  const session = getSession()
  return Boolean(session && session.token)
}

function getRoleCode() {
  const session = getSession()
  return session ? session.roleCode : 'STUDENT'
}

function getUserId() {
  const session = getSession()
  return session ? session.userId : 0
}

function hydrateAppSession() {
  const app = getAppSafe()
  const session = loadSession()
  if (app && app.globalData) {
    app.globalData.session = session
    app.globalData.homeMenus = buildHomeMenus(session ? session.roleCode : 'STUDENT')
  }
  return session
}

function applyLoginSession(session) {
  const app = getAppSafe()
  saveSession(session)
  if (app && app.globalData) {
    app.globalData.session = session
    app.globalData.homeMenus = buildHomeMenus(session.roleCode)
  }
}

function updateSessionProfile(partial) {
  const session = getSession()
  if (!session) {
    return null
  }
  const nextSession = Object.assign({}, session, partial)
  const app = getAppSafe()
  saveSession(nextSession)
  if (app && app.globalData) {
    app.globalData.session = nextSession
  }
  return nextSession
}

function logoutSession() {
  const app = getAppSafe()
  clearSession()
  if (app && app.globalData) {
    app.globalData.session = null
    app.globalData.homeMenus = buildHomeMenus('STUDENT')
  }
}

function requireLogin(redirectUrl) {
  if (isLoggedIn()) {
    return true
  }
  wx.reLaunch({
    url: buildLoginRoute(redirectUrl)
  })
  return false
}

module.exports = {
  applyLoginSession,
  getRoleCode,
  getSession,
  getUserId,
  hydrateAppSession,
  isLoggedIn,
  logoutSession,
  requireLogin,
  updateSessionProfile
}
