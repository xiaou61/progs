const {
  fetchGroupMessages,
  fetchPrivateMessages,
  sendGroupMessage,
  sendPrivateMessage
} = require('../../../services/message')
const { getSession, requireLogin } = require('../../../utils/auth')

Page({
  data: {
    chatType: 'PRIVATE',
    peerUserId: 0,
    competitionId: 0,
    title: '会话详情',
    loading: false,
    sending: false,
    error: '',
    draft: '',
    messages: []
  },

  onLoad(options) {
    const title = typeof options.title === 'string' ? decodeURIComponent(options.title) : '会话详情'
    this.setData({
      chatType: options.type === 'GROUP' ? 'GROUP' : 'PRIVATE',
      peerUserId: Number(options.peerUserId || 0),
      competitionId: Number(options.competitionId || 0),
      title
    })
    wx.setNavigationBarTitle({
      title
    })
  },

  onShow() {
    const redirectUrl = this.data.chatType === 'GROUP'
      ? `/pages/message/chat/index?type=GROUP&competitionId=${this.data.competitionId}&title=${encodeURIComponent(this.data.title)}`
      : `/pages/message/chat/index?type=PRIVATE&peerUserId=${this.data.peerUserId}&title=${encodeURIComponent(this.data.title)}`

    if (!requireLogin(redirectUrl)) {
      return
    }
    this.loadMessages()
  },

  updateDraft(event) {
    this.setData({
      draft: event.detail.value
    })
  },

  async loadMessages() {
    const session = getSession()
    this.setData({
      loading: true,
      error: ''
    })

    try {
      const messages = this.data.chatType === 'PRIVATE'
        ? await fetchPrivateMessages(session.userId, this.data.peerUserId)
        : await fetchGroupMessages(this.data.competitionId, session.userId)
      this.setData({ messages })
    } catch (error) {
      this.setData({
        error: error instanceof Error ? error.message : '加载会话失败'
      })
    } finally {
      this.setData({ loading: false })
    }
  },

  async handleSend() {
    const content = this.data.draft.trim()
    if (!content || this.data.sending) {
      return
    }

    const session = getSession()
    this.setData({
      sending: true,
      error: ''
    })

    try {
      if (this.data.chatType === 'PRIVATE') {
        await sendPrivateMessage({
          senderUserId: session.userId,
          receiverUserId: this.data.peerUserId,
          content
        })
      } else {
        await sendGroupMessage({
          competitionId: this.data.competitionId,
          senderUserId: session.userId,
          content
        })
      }
      this.setData({ draft: '' })
      await this.loadMessages()
    } catch (error) {
      this.setData({
        error: error instanceof Error ? error.message : '发送消息失败'
      })
    } finally {
      this.setData({ sending: false })
    }
  }
})
