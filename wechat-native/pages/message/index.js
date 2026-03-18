const {
  clearConversation,
  fetchConversations,
  fetchSystemMessages,
  setConversationMuted,
  setConversationPinned
} = require('../../services/message')
const { getSession, requireLogin } = require('../../utils/auth')
const {
  buildConversationBadges,
  buildConversationPreview,
  buildMessageChatRoute
} = require('../../utils/message')

function buildConversationCards(items) {
  return items.map((item) => ({
    ...item,
    badges: buildConversationBadges(item),
    preview: buildConversationPreview(item),
    typeLabel: item.conversationType === 'PRIVATE' ? '私信会话' : '比赛群聊'
  }))
}

Page({
  data: {
    loading: false,
    error: '',
    actionKey: '',
    systemMessages: [],
    conversations: []
  },

  findConversation(conversationId) {
    return this.data.conversations.find((item) => item.id === conversationId)
  },

  onShow() {
    if (!requireLogin('/pages/message/index')) {
      return
    }
    this.loadMessageCenter()
  },

  async loadMessageCenter() {
    const session = getSession()
    this.setData({
      loading: true,
      error: ''
    })

    try {
      const [systemMessages, conversations] = await Promise.all([
        fetchSystemMessages(session.userId),
        fetchConversations(session.userId)
      ])
      this.setData({
        systemMessages,
        conversations: buildConversationCards(conversations)
      })
    } catch (error) {
      this.setData({
        error: error instanceof Error ? error.message : '加载消息中心失败'
      })
    } finally {
      this.setData({ loading: false })
    }
  },

  openConversation(event) {
    const item = this.findConversation(Number(event.currentTarget.dataset.id))
    if (!item) {
      return
    }
    if (item.conversationType === 'PRIVATE' && item.peerUserId) {
      wx.navigateTo({
        url: buildMessageChatRoute({
          type: 'PRIVATE',
          peerUserId: item.peerUserId,
          title: item.title
        })
      })
      return
    }
    if (item.conversationType === 'GROUP' && item.competitionId) {
      wx.navigateTo({
        url: buildMessageChatRoute({
          type: 'GROUP',
          competitionId: item.competitionId,
          title: item.title
        })
      })
    }
  },

  async handlePin(event) {
    const item = this.findConversation(Number(event.currentTarget.dataset.id))
    if (!item) {
      return
    }
    const session = getSession()
    this.setData({ actionKey: `${item.id}:pin`, error: '' })
    try {
      await setConversationPinned(item.id, session.userId, !item.pinned)
      await this.loadMessageCenter()
    } catch (error) {
      this.setData({
        error: error instanceof Error ? error.message : '更新置顶状态失败'
      })
    } finally {
      this.setData({ actionKey: '' })
    }
  },

  async handleMute(event) {
    const item = this.findConversation(Number(event.currentTarget.dataset.id))
    if (!item) {
      return
    }
    const session = getSession()
    this.setData({ actionKey: `${item.id}:mute`, error: '' })
    try {
      await setConversationMuted(item.id, session.userId, !item.muted)
      await this.loadMessageCenter()
    } catch (error) {
      this.setData({
        error: error instanceof Error ? error.message : '更新免打扰状态失败'
      })
    } finally {
      this.setData({ actionKey: '' })
    }
  },

  async handleClear(event) {
    const item = this.findConversation(Number(event.currentTarget.dataset.id))
    if (!item) {
      return
    }
    const session = getSession()
    this.setData({ actionKey: `${item.id}:clear`, error: '' })
    try {
      await clearConversation(item.id, session.userId)
      await this.loadMessageCenter()
    } catch (error) {
      this.setData({
        error: error instanceof Error ? error.message : '清空会话失败'
      })
    } finally {
      this.setData({ actionKey: '' })
    }
  }
})
