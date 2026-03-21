function normalizeDateValue(value) {
  if (!value) {
    return null
  }
  if (value instanceof Date) {
    return Number.isNaN(value.getTime()) ? null : value
  }

  const rawValue = String(value).trim()
  if (!rawValue) {
    return null
  }

  const normalizedValue = rawValue.includes('T') ? rawValue : rawValue.replace(' ', 'T')
  const dateValue = new Date(normalizedValue)
  return Number.isNaN(dateValue.getTime()) ? null : dateValue
}

function resolveCancelPermission(competition, registration, now = new Date()) {
  if (!registration || registration.status !== 'REGISTERED') {
    return {
      canCancel: false,
      hint: ''
    }
  }
  if (!competition || !competition.signupEndAt) {
    return {
      canCancel: false,
      hint: '比赛信息加载中，请稍后重试'
    }
  }

  const signupEndAt = normalizeDateValue(competition.signupEndAt)
  if (!signupEndAt) {
    return {
      canCancel: false,
      hint: '报名截止时间异常，请联系管理员'
    }
  }

  const currentTime = normalizeDateValue(now) || new Date()
  if (currentTime.getTime() > signupEndAt.getTime()) {
    return {
      canCancel: false,
      hint: '报名已截止，不能取消报名'
    }
  }

  return {
    canCancel: true,
    hint: ''
  }
}

module.exports = {
  normalizeDateValue,
  resolveCancelPermission
}
