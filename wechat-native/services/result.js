const { request } = require('./http')

function fetchCompetitionResults(competitionId) {
  return request(`/api/app/results/competition/${competitionId}`)
}

function fetchStudentOverview(studentId) {
  return request(`/api/app/results/student/${studentId}`)
}

module.exports = {
  fetchCompetitionResults,
  fetchStudentOverview
}
