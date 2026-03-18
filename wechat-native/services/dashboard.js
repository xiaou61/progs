const { request, requestText } = require('./http')

function fetchTeacherDashboard(teacherId) {
  return request(`/api/app/dashboard/teachers/${teacherId}`)
}

function exportTeacherDashboardCsv(teacherId) {
  return requestText(`/api/app/dashboard/teachers/${teacherId}/export`)
}

module.exports = {
  exportTeacherDashboardCsv,
  fetchTeacherDashboard
}
