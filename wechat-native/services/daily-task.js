const { request } = require('./http')

function fetchDailyTaskOverview(userId) {
  return request(`/api/app/points/tasks/${userId}`)
}

function completeDailyCheckin(userId) {
  return request('/api/app/points/tasks/checkin', {
    method: 'POST',
    body: { userId }
  })
}

function completeCompetitionShare(userId, competitionId) {
  return request('/api/app/points/tasks/share', {
    method: 'POST',
    body: { userId, competitionId }
  })
}

module.exports = {
  completeCompetitionShare,
  completeDailyCheckin,
  fetchDailyTaskOverview
}
