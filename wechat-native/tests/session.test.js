const test = require('node:test')
const assert = require('node:assert/strict')

test('session utilities should persist and restore login session', () => {
  global.wx = {
    _storage: {},
    getStorageSync(key) {
      return this._storage[key]
    },
    setStorageSync(key, value) {
      this._storage[key] = value
    },
    removeStorageSync(key) {
      delete this._storage[key]
    }
  }

  const {
    saveSession,
    loadSession,
    clearSession,
    getAccessToken
  } = require('../utils/session')

  saveSession({
    userId: 1001,
    roleCode: 'TEACHER',
    token: 'token-1001',
    studentNo: 'T20260001',
    realName: '王老师'
  })

  assert.deepEqual(loadSession(), {
    userId: 1001,
    roleCode: 'TEACHER',
    token: 'token-1001',
    studentNo: 'T20260001',
    realName: '王老师'
  })
  assert.equal(getAccessToken(), 'token-1001')

  clearSession()
  assert.equal(loadSession(), null)
  assert.equal(getAccessToken(), '')
})

test('route utilities should build redirect routes for protected pages', () => {
  const {
    LOGIN_PAGE_ROUTE,
    HOME_PAGE_ROUTE,
    buildLoginRoute,
    resolveProtectedRoute,
    resolvePostLoginRoute
  } = require('../utils/routes')

  assert.equal(LOGIN_PAGE_ROUTE, '/pages/login/index')
  assert.equal(HOME_PAGE_ROUTE, '/pages/home/index')
  assert.equal(
    buildLoginRoute('/pages/competition/detail/index?competitionId=1'),
    '/pages/login/index?redirect=%2Fpages%2Fcompetition%2Fdetail%2Findex%3FcompetitionId%3D1'
  )
  assert.equal(
    resolveProtectedRoute('/pages/profile/index', false),
    '/pages/login/index?redirect=%2Fpages%2Fprofile%2Findex'
  )
  assert.equal(resolveProtectedRoute('/pages/profile/index', true), '/pages/profile/index')
  assert.equal(resolvePostLoginRoute('/pages/profile/index'), '/pages/profile/index')
  assert.equal(resolvePostLoginRoute('http://example.com'), '/pages/home/index')
})
