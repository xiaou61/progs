const { request } = require('./http')

function fetchStudentOverview(studentId) {
  return request(`/api/app/results/student/${studentId}`)
}

module.exports = {
  fetchStudentOverview
}
