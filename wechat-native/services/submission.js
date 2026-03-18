const { request } = require('./http')

function submitCompetitionWork(payload) {
  return request('/api/app/submissions', {
    method: 'POST',
    body: payload
  }).then((result) => result.submissionId)
}

function fetchCompetitionSubmissions(competitionId) {
  return request(`/api/app/submissions/competition/${competitionId}`)
}

module.exports = {
  fetchCompetitionSubmissions,
  submitCompetitionWork
}
