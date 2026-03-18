const USER_SESSION_STORAGE_KEY = 'campus-competition-session'

function getStorageRuntime() {
  if (typeof wx !== 'undefined') {
    return wx
  }
  if (typeof globalThis !== 'undefined' && globalThis.wx) {
    return globalThis.wx
  }
  return null
}

function isSession(value) {
  return Boolean(
    value &&
      typeof value === 'object' &&
      typeof value.userId === 'number' &&
      (value.roleCode === 'STUDENT' || value.roleCode === 'TEACHER') &&
      typeof value.token === 'string'
  )
}

function parseSession(rawValue) {
  if (!rawValue) {
    return null
  }

  if (typeof rawValue === 'string') {
    try {
      const parsed = JSON.parse(rawValue)
      return isSession(parsed) ? parsed : null
    } catch (error) {
      return null
    }
  }

  return isSession(rawValue) ? rawValue : null
}

function loadSession() {
  const storage = getStorageRuntime()
  if (!storage || typeof storage.getStorageSync !== 'function') {
    return null
  }
  return parseSession(storage.getStorageSync(USER_SESSION_STORAGE_KEY))
}

function saveSession(session) {
  const storage = getStorageRuntime()
  if (!storage || typeof storage.setStorageSync !== 'function') {
    return
  }
  storage.setStorageSync(USER_SESSION_STORAGE_KEY, session)
}

function clearSession() {
  const storage = getStorageRuntime()
  if (!storage || typeof storage.removeStorageSync !== 'function') {
    return
  }
  storage.removeStorageSync(USER_SESSION_STORAGE_KEY)
}

function getAccessToken() {
  const session = loadSession()
  return session ? session.token : ''
}

module.exports = {
  USER_SESSION_STORAGE_KEY,
  clearSession,
  getAccessToken,
  loadSession,
  saveSession
}
