const { request } = require('./http')

function fetchSystemMessages(userId) {
  return request(`/api/app/messages/system?userId=${userId}`)
}

function fetchConversations(userId) {
  return request(`/api/app/messages/conversations?userId=${userId}`)
}

function consultCompetitionOrganizer(payload) {
  return request('/api/app/messages/consult', {
    method: 'POST',
    body: payload
  })
}

function fetchPrivateMessages(userId, peerUserId) {
  return request(`/api/app/messages/private?userId=${userId}&peerUserId=${peerUserId}`)
}

function sendPrivateMessage(payload) {
  return request('/api/app/messages/private', {
    method: 'POST',
    body: payload
  }).then((result) => result.messageId)
}

function fetchGroupMessages(competitionId, userId) {
  return request(`/api/app/messages/group/${competitionId}?userId=${userId}`)
}

function sendGroupMessage(payload) {
  return request('/api/app/messages/group', {
    method: 'POST',
    body: payload
  }).then((result) => result.messageId)
}

function setConversationPinned(conversationId, userId, enabled) {
  return request(`/api/app/messages/conversations/${conversationId}/pin`, {
    method: 'POST',
    body: { userId, enabled }
  })
}

function setConversationMuted(conversationId, userId, enabled) {
  return request(`/api/app/messages/conversations/${conversationId}/mute`, {
    method: 'POST',
    body: { userId, enabled }
  })
}

function clearConversation(conversationId, userId) {
  return request(`/api/app/messages/conversations/${conversationId}/clear`, {
    method: 'POST',
    body: { userId }
  })
}

module.exports = {
  clearConversation,
  consultCompetitionOrganizer,
  fetchConversations,
  fetchGroupMessages,
  fetchPrivateMessages,
  fetchSystemMessages,
  sendGroupMessage,
  sendPrivateMessage,
  setConversationMuted,
  setConversationPinned
}
