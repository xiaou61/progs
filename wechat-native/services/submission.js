const { request } = require('./http')
const { uploadLocalFile } = require('./upload')

function submitCompetitionWork(payload) {
  return request('/api/app/submissions', {
    method: 'POST',
    body: payload
  }).then((result) => result.submissionId)
}

function uploadCompetitionWorkFile(file) {
  return uploadLocalFile('/api/app/submission-files', file)
}

function fetchCompetitionSubmissions(competitionId) {
  return request(`/api/app/submissions/competition/${competitionId}`)
}

module.exports = {
  fetchCompetitionSubmissions,
  submitCompetitionWork,
  uploadCompetitionWorkFile
}
