const { request } = require('./http')

function checkInCompetition(payload) {
  return request('/api/app/checkins', {
    method: 'POST',
    body: payload
  }).then((result) => result.checked)
}

function fetchCompetitionCheckins(competitionId) {
  return request(`/api/app/checkins/competition/${competitionId}`)
}

module.exports = {
  checkInCompetition,
  fetchCompetitionCheckins
}
