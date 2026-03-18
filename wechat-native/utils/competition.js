const { formatDateTime, formatDateTimeRange, toPositiveNumber } = require('./format')

const competitionRouteMap = {
  detail: '/pages/competition/detail/index',
  register: '/pages/competition/register/index',
  checkin: '/pages/competition/checkin/index',
  submission: '/pages/competition/submission/index',
  result: '/pages/competition/result/index'
}

function buildCompetitionRoute(target, competitionId) {
  return `${competitionRouteMap[target]}?competitionId=${competitionId}`
}

function formatCompetitionWindow(startAt, endAt) {
  return formatDateTimeRange(startAt, endAt)
}

function resolveCompetitionStatusLabel(item, now) {
  const currentTime = now instanceof Date ? now : new Date()
  const signupStartAt = new Date(item.signupStartAt)
  const signupEndAt = new Date(item.signupEndAt)
  const startAt = new Date(item.startAt)
  const endAt = new Date(item.endAt)

  if (currentTime < signupStartAt) {
    return '即将报名'
  }
  if (currentTime <= signupEndAt) {
    return '报名中'
  }
  if (currentTime >= startAt && currentTime <= endAt) {
    return '比赛进行中'
  }
  return '已截止'
}

function resolveCompetitionId(options) {
  const rawCompetitionId = options && options.competitionId
  return toPositiveNumber(Array.isArray(rawCompetitionId) ? rawCompetitionId[0] : rawCompetitionId, 1)
}

module.exports = {
  buildCompetitionRoute,
  formatCompetitionDateTime: formatDateTime,
  formatCompetitionWindow,
  resolveCompetitionId,
  resolveCompetitionStatusLabel
}
