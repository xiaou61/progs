const { request } = require('./http')

function fetchCompetitions() {
  return request('/api/app/competitions')
}

function fetchCompetitionDetail(competitionId) {
  return request(`/api/app/competitions/${competitionId}`)
}

function fetchManagedCompetitions() {
  return request('/api/admin/competitions')
}

function publishCompetition(payload) {
  return request('/api/admin/competitions', {
    method: 'POST',
    body: payload
  }).then((result) => result.competitionId)
}

function saveCompetitionDraft(payload) {
  return request('/api/admin/competitions/draft', {
    method: 'POST',
    body: payload
  }).then((result) => result.competitionId)
}

function updateCompetition(competitionId, payload) {
  return request(`/api/admin/competitions/${competitionId}`, {
    method: 'PUT',
    body: payload
  })
}

function updateCompetitionFeature(competitionId, payload) {
  return request(`/api/admin/competitions/${competitionId}/feature`, {
    method: 'POST',
    body: payload
  })
}

function offlineCompetition(competitionId) {
  return request(`/api/admin/competitions/${competitionId}/offline`, {
    method: 'POST'
  }).then((result) => result.offline)
}

module.exports = {
  fetchCompetitionDetail,
  fetchCompetitions,
  fetchManagedCompetitions,
  offlineCompetition,
  publishCompetition,
  saveCompetitionDraft,
  updateCompetition,
  updateCompetitionFeature
}
