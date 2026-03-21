const { getAccessToken } = require('../utils/session')

function getBaseUrl() {
  const app = typeof getApp === 'function' ? getApp() : null
  const baseUrl = app && app.globalData && app.globalData.apiBaseUrl
  return (baseUrl || 'http://127.0.0.1:8080').replace(/\/$/, '')
}

function buildUrl(path) {
  return `${getBaseUrl()}${path}`
}

function unwrapPayload(payload) {
  if (payload && typeof payload === 'object' && Object.prototype.hasOwnProperty.call(payload, 'code')) {
    if (payload.code !== 0) {
      throw new Error(payload.message || '请求失败')
    }
    return payload.data
  }
  return payload
}

function buildHeaders(extraHeaders) {
  const headers = Object.assign({}, extraHeaders)
  const token = getAccessToken()
  if (token) {
    headers.Authorization = `Bearer ${token}`
  }
  return headers
}

function request(path, options) {
  const nextOptions = options || {}
  return new Promise((resolve, reject) => {
    if (typeof wx === 'undefined' || typeof wx.request !== 'function') {
      reject(new Error('当前环境不支持网络请求'))
      return
    }

    const headers = buildHeaders(nextOptions.header)
    if (nextOptions.body !== undefined && !headers['Content-Type']) {
      headers['Content-Type'] = 'application/json'
    }

    wx.request({
      url: buildUrl(path),
      method: nextOptions.method || 'GET',
      data: nextOptions.body,
      header: headers,
      success(response) {
        if (response.statusCode < 200 || response.statusCode >= 300) {
          reject(new Error(`请求失败：${response.statusCode}`))
          return
        }

        try {
          resolve(unwrapPayload(response.data))
        } catch (error) {
          reject(error)
        }
      },
      fail(error) {
        reject(new Error((error && error.errMsg) || '网络请求失败'))
      }
    })
  })
}

function requestText(path, options) {
  const nextOptions = options || {}
  return new Promise((resolve, reject) => {
    if (typeof wx === 'undefined' || typeof wx.request !== 'function') {
      reject(new Error('当前环境不支持网络请求'))
      return
    }

    const headers = buildHeaders(nextOptions.header)
    wx.request({
      url: buildUrl(path),
      method: nextOptions.method || 'GET',
      data: nextOptions.body,
      header: headers,
      success(response) {
        if (response.statusCode < 200 || response.statusCode >= 300) {
          reject(new Error(`请求失败：${response.statusCode}`))
          return
        }
        resolve(String(response.data || ''))
      },
      fail(error) {
        reject(new Error((error && error.errMsg) || '网络请求失败'))
      }
    })
  })
}

module.exports = {
  buildUrl,
  request,
  requestText
}
