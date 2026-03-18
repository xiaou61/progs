const { request } = require('./http')

function login(payload) {
  return request('/api/app/auth/login', {
    method: 'POST',
    body: payload
  })
}

module.exports = {
  login
}
