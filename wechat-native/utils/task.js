const { formatDateTime } = require('./format')

function resolveDailyCheckinLabel(done) {
  return done ? '今日已签到' : '立即签到'
}

function resolveShareTaskLabel(done) {
  return done ? '今日已分享' : '分享比赛得积分'
}

function summarizeTaskProgress(task) {
  const completedCount = Number(Boolean(task.dailyCheckinDone)) + Number(Boolean(task.competitionShareDone))
  return `今日已完成 ${completedCount}/2 项任务，累计获得 ${task.todayTaskPoints} 积分`
}

function buildOverviewCards(overview) {
  return [
    { label: '我的比赛', value: `${overview.registeredCompetitionCount} 场` },
    { label: '已交作品', value: `${overview.submittedWorkCount} 份` },
    { label: '获奖次数', value: `${overview.awardCount} 次` },
    { label: '总积分', value: `${overview.totalPoints} 分` }
  ]
}

function formatTaskTime(value) {
  return value ? formatDateTime(value) : '今日未完成'
}

module.exports = {
  buildOverviewCards,
  formatTaskTime,
  resolveDailyCheckinLabel,
  resolveShareTaskLabel,
  summarizeTaskProgress
}
