const { request } = require('./http')

function registerCompetition(payload) {
  return request('/api/app/registrations', {
    method: 'POST',
    body: payload
  }).then((result) => result.registrationId)
}

function fetchUserRegistration(competitionId, userId) {
  return request(`/api/app/registrations/competition/${competitionId}/user/${userId}`)
}

function fetchUserRegistrations(userId) {
  return request(`/api/app/registrations/user/${userId}`)
}

function cancelRegistration(registrationId, userId) {
  return request(`/api/app/registrations/${registrationId}/cancel`, {
    method: 'POST',
    body: { userId }
  }).then((result) => result.cancelled)
}

module.exports = {
  cancelRegistration,
  fetchUserRegistration,
  fetchUserRegistrations,
  registerCompetition
}
