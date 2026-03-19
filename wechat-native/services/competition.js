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

function fetchTeacherManagedCompetitions(teacherId) {
  return request(`/api/app/teachers/${teacherId}/competitions`)
}

function publishCompetition(payload) {
  return request('/api/admin/competitions', {
    method: 'POST',
    body: payload
  }).then((result) => result.competitionId)
}

function publishTeacherCompetition(teacherId, payload) {
  return request(`/api/app/teachers/${teacherId}/competitions`, {
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

function saveTeacherCompetitionDraft(teacherId, payload) {
  return request(`/api/app/teachers/${teacherId}/competitions/draft`, {
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

function updateTeacherCompetition(teacherId, competitionId, payload) {
  return request(`/api/app/teachers/${teacherId}/competitions/${competitionId}`, {
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

function updateTeacherCompetitionFeature(teacherId, competitionId, payload) {
  return request(`/api/app/teachers/${teacherId}/competitions/${competitionId}/feature`, {
    method: 'POST',
    body: payload
  })
}

function offlineCompetition(competitionId) {
  return request(`/api/admin/competitions/${competitionId}/offline`, {
    method: 'POST'
  }).then((result) => result.offline)
}

function offlineTeacherCompetition(teacherId, competitionId) {
  return request(`/api/app/teachers/${teacherId}/competitions/${competitionId}/offline`, {
    method: 'POST'
  }).then((result) => result.offline)
}

module.exports = {
  fetchCompetitionDetail,
  fetchCompetitions,
  fetchManagedCompetitions,
  fetchTeacherManagedCompetitions,
  offlineCompetition,
  offlineTeacherCompetition,
  publishCompetition,
  publishTeacherCompetition,
  saveCompetitionDraft,
  saveTeacherCompetitionDraft,
  updateCompetition,
  updateCompetitionFeature,
  updateTeacherCompetition,
  updateTeacherCompetitionFeature
}
