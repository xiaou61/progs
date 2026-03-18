const { request } = require('./http')
const { uploadLocalFile } = require('./upload')

function fetchProfile(userId) {
  return request(`/api/app/profile?userId=${userId}`)
}

function updateProfile(payload) {
  return request('/api/app/profile', {
    method: 'PUT',
    body: payload
  })
}

function changePassword(payload) {
  return request('/api/app/profile/password', {
    method: 'POST',
    body: payload
  })
}

function submitFeedback(payload) {
  return request('/api/app/profile/feedback', {
    method: 'POST',
    body: payload
  })
}

function cancelAccount(payload) {
  return request('/api/app/profile/cancel', {
    method: 'POST',
    body: payload
  })
}

function uploadProfileFile(file) {
  return uploadLocalFile('/api/app/profile/files', file)
}

module.exports = {
  cancelAccount,
  changePassword,
  fetchProfile,
  submitFeedback,
  updateProfile,
  uploadProfileFile
}
