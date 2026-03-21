const { fetchCompetitionDetail } = require('../../../services/competition')
const {
  cancelRegistration,
  fetchUserRegistration,
  registerCompetition
} = require('../../../services/registration')
const { getRoleCode, getSession, requireLogin } = require('../../../utils/auth')
const { resolveCompetitionId } = require('../../../utils/competition')
const { resolveCancelPermission } = require('../../../utils/registration-permission')

function buildStatusText(registration) {
  if (!registration) {
    return '未报名'
  }
  if (registration.status === 'REGISTERED') {
    return '已报名'
  }
  if (registration.status === 'CANCELLED') {
    return '已取消'
  }
  return '已驳回'
}

function buildAttendanceText(registration) {
  if (!registration) {
    return '待签到'
  }
  if (registration.attendanceStatus === 'PRESENT') {
    return '已到场'
  }
  if (registration.attendanceStatus === 'ABSENT') {
    return '已缺席'
  }
  return '待签到'
}

function resolveAccountLabel(session) {
  if (!session) {
    return '未登录用户'
  }
  const realName = typeof session.realName === 'string' ? session.realName.trim() : ''
  if (realName) {
    return realName
  }
  const studentNo = typeof session.studentNo === 'string' ? session.studentNo.trim() : ''
  if (studentNo) {
    return studentNo
  }
  return `用户 ${session.userId}`
}

function resolveParticipantTypeLabel(participantType) {
  return participantType === 'TEACHER_ONLY' ? '仅老师参加' : '仅学生参加'
}

function resolveRegisterPermission(roleCode, competition) {
  if (!competition) {
    return {
      canRegister: false,
      hint: '比赛信息加载中，请稍后重试'
    }
  }
  if (competition.participantType === 'TEACHER_ONLY' && roleCode !== 'TEACHER') {
    return {
      canRegister: false,
      hint: '当前比赛仅限老师报名'
    }
  }
  if (competition.participantType === 'STUDENT_ONLY' && roleCode !== 'STUDENT') {
    return {
      canRegister: false,
      hint: '当前比赛仅限学生报名'
    }
  }
  return {
    canRegister: true,
    hint: ''
  }
}

Page({
  data: {
    competitionId: 0,
    competitionTitle: '未选择比赛',
    competitionDesc: '请从比赛列表进入后再完成报名。',
    competition: null,
    participantType: 'STUDENT_ONLY',
    participantTypeLabel: '仅学生参加',
    advisorTeacherName: '',
    loading: false,
    submitting: false,
    error: '',
    success: '',
    userId: 0,
    roleCode: 'STUDENT',
    accountLabel: '',
    registration: null,
    statusText: '未报名',
    attendanceText: '待签到',
    hasActiveRegistration: false,
    canRegister: false,
    registerHint: '比赛信息加载中，请稍后重试',
    canCancel: false,
    cancelHint: ''
  },

  onLoad(options) {
    this.setData({
      competitionId: resolveCompetitionId(options)
    })
  },

  onShow() {
    if (!this.data.competitionId) {
      this.setData({
        error: '缺少比赛编号，请从比赛列表重新进入。'
      })
      return
    }
    const redirectUrl = `/pages/competition/register/index?competitionId=${this.data.competitionId}`
    if (!requireLogin(redirectUrl)) {
      return
    }

    const session = getSession()
    this.setData({
      userId: session.userId,
      roleCode: getRoleCode(),
      accountLabel: resolveAccountLabel(session)
    })
    this.loadPageData()
  },

  async loadPageData() {
    await Promise.all([this.loadCompetition(), this.loadRegistration()])
  },

  async loadCompetition() {
    this.setData({
      loading: true,
      error: ''
    })

    try {
      const current = await fetchCompetitionDetail(this.data.competitionId)
      const permission = resolveRegisterPermission(this.data.roleCode, current)
      const cancelPermission = resolveCancelPermission(current, this.data.registration)
      this.setData({
        competition: current,
        competitionTitle: current.title,
        competitionDesc: current.description,
        participantType: current.participantType || 'STUDENT_ONLY',
        participantTypeLabel: resolveParticipantTypeLabel(current.participantType),
        advisorTeacherName: current.advisorTeacherName || '',
        canRegister: permission.canRegister,
        registerHint: permission.hint,
        canCancel: cancelPermission.canCancel,
        cancelHint: cancelPermission.hint
      })
    } catch (error) {
      this.setData({
        error: error instanceof Error ? error.message : '加载比赛信息失败'
      })
    } finally {
      this.setData({ loading: false })
    }
  },

  async loadRegistration() {
    const session = getSession()
    try {
      const registration = await fetchUserRegistration(this.data.competitionId, session.userId)
      const cancelPermission = resolveCancelPermission(this.data.competition, registration)
      this.setData({
        registration,
        statusText: buildStatusText(registration),
        attendanceText: buildAttendanceText(registration),
        hasActiveRegistration: Boolean(registration && registration.status === 'REGISTERED'),
        canCancel: cancelPermission.canCancel,
        cancelHint: cancelPermission.hint
      })
    } catch (error) {
      this.setData({
        error: error instanceof Error ? error.message : '加载报名状态失败'
      })
    }
  },

  async submitRegistration() {
    if (!this.data.canRegister) {
      this.setData({
        error: this.data.registerHint,
        success: ''
      })
      return
    }
    const session = getSession()
    this.setData({
      submitting: true,
      error: '',
      success: ''
    })

    try {
      const registrationId = await registerCompetition({
        competitionId: this.data.competitionId,
        userId: session.userId
      })
      this.setData({
        success: `报名成功，记录编号 #${registrationId}`
      })
      await this.loadRegistration()
    } catch (error) {
      this.setData({
        error: error instanceof Error ? error.message : '提交报名失败'
      })
    } finally {
      this.setData({ submitting: false })
    }
  },

  async submitCancel() {
    const session = getSession()
    if (!this.data.registration) {
      return
    }
    if (!this.data.canCancel) {
      this.setData({
        error: this.data.cancelHint || '当前不可取消报名',
        success: ''
      })
      return
    }

    this.setData({
      submitting: true,
      error: '',
      success: ''
    })

    try {
      await cancelRegistration(this.data.registration.id, session.userId)
      this.setData({
        success: `已取消报名，记录编号 #${this.data.registration.id}`
      })
      await this.loadRegistration()
    } catch (error) {
      this.setData({
        error: error instanceof Error ? error.message : '取消报名失败'
      })
    } finally {
      this.setData({ submitting: false })
    }
  }
})
