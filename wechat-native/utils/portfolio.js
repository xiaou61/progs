const { buildCompetitionRoute, formatCompetitionDateTime } = require('./competition')
const { buildAwardCards } = require('./result')

function resolvePortfolioTab(tab) {
  return tab === 'awards' ? 'awards' : 'works'
}

function resolveWorkFileName(fileUrl) {
  if (!fileUrl) {
    return '未命名文件'
  }
  const segments = String(fileUrl).split('/')
  return segments[segments.length - 1] || String(fileUrl)
}

function buildCompetitionTitleMap(competitions) {
  return Object.fromEntries((competitions || []).map((item) => [item.id, item.title]))
}

function buildWorkCards(options) {
  const submissions = (options && options.submissions) || []
  const titleMap = buildCompetitionTitleMap((options && options.competitions) || [])
  return submissions.map((item) => ({
    ...item,
    competitionTitle: titleMap[item.competitionId] || `比赛 #${item.competitionId}`,
    versionText: `v${item.versionNo}`,
    fileName: resolveWorkFileName(item.fileUrl),
    submittedAtText: formatCompetitionDateTime(item.submittedAt),
    actionRoute: buildCompetitionRoute('submission', item.competitionId)
  }))
}

function buildPortfolioState(options) {
  const activeTab = resolvePortfolioTab(options && options.tab)
  const workCards = buildWorkCards(options)
  const awardCards = buildAwardCards(options).map((item) => ({
    ...item,
    actionRoute: buildCompetitionRoute('result', item.competitionId)
  }))
  const sections = activeTab === 'awards'
    ? [
        { key: 'awards', title: '我的获奖', emptyText: '当前还没有获奖结果。' },
        { key: 'works', title: '我的作品', emptyText: '当前还没有作品记录。' }
      ]
    : [
        { key: 'works', title: '我的作品', emptyText: '当前还没有作品记录。' },
        { key: 'awards', title: '我的获奖', emptyText: '当前还没有获奖结果。' }
      ]

  return {
    activeTab,
    sections,
    workCards,
    awardCards
  }
}

module.exports = {
  buildPortfolioState,
  buildWorkCards,
  resolvePortfolioTab
}
