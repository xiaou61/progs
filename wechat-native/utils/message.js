function buildMessageChatRoute(options) {
  const params = [`type=${encodeURIComponent(options.type)}`]
  if (options.type === 'PRIVATE') {
    params.push(`peerUserId=${encodeURIComponent(String(options.peerUserId))}`)
  } else {
    params.push(`competitionId=${encodeURIComponent(String(options.competitionId))}`)
  }
  if (options.title) {
    params.push(`title=${encodeURIComponent(options.title)}`)
  }
  return `/pages/message/chat/index?${params.join('&')}`
}

function buildConversationBadges(conversation) {
  return [
    ...(conversation.pinned ? ['置顶'] : []),
    ...(conversation.muted ? ['免打扰'] : [])
  ]
}

function buildConversationPreview(conversation) {
  return conversation.lastMessage && conversation.lastMessage.trim()
    ? conversation.lastMessage
    : '会话已清空，等待新消息'
}

module.exports = {
  buildConversationBadges,
  buildConversationPreview,
  buildMessageChatRoute
}
