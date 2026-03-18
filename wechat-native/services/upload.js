const { getAccessToken } = require('../utils/session')

function getBaseUrl() {
  const app = typeof getApp === 'function' ? getApp() : null
  const baseUrl = app && app.globalData && app.globalData.apiBaseUrl
  return (baseUrl || 'http://127.0.0.1:8080').replace(/\/$/, '')
}

function parseUploadResponse(payload) {
  const rawPayload = typeof payload === 'string' ? JSON.parse(payload) : payload
  if (rawPayload && typeof rawPayload === 'object' && Object.prototype.hasOwnProperty.call(rawPayload, 'code')) {
    if (rawPayload.code !== 0) {
      throw new Error(rawPayload.message || '上传失败')
    }
    return rawPayload.data
  }
  return rawPayload
}

function uploadLocalFile(path, file) {
  return new Promise((resolve, reject) => {
    if (typeof wx === 'undefined' || typeof wx.uploadFile !== 'function') {
      reject(new Error('当前环境不支持文件上传'))
      return
    }

    const token = getAccessToken()
    const headers = {}
    if (token) {
      headers.Authorization = `Bearer ${token}`
    }

    wx.uploadFile({
      url: `${getBaseUrl()}${path}`,
      filePath: file.filePath,
      name: 'file',
      header: headers,
      success(response) {
        if (response.statusCode < 200 || response.statusCode >= 300) {
          reject(new Error(`上传失败：${response.statusCode}`))
          return
        }

        try {
          resolve(parseUploadResponse(response.data))
        } catch (error) {
          reject(error)
        }
      },
      fail(error) {
        reject(new Error((error && error.errMsg) || '上传失败'))
      }
    })
  })
}

module.exports = {
  uploadLocalFile
}
