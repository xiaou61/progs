const { formatCompetitionDateTime } = require('./competition')

function buildAwardCards(options) {
  const competitionId = options && options.competitionId ? Number(options.competitionId) : 0
  const results = (options && options.results) || []
  const competitions = (options && options.competitions) || []
  const titleMap = Object.fromEntries(competitions.map((item) => [item.id, item.title]))
  const filteredResults = competitionId > 0
    ? results.filter((item) => item.competitionId === competitionId)
    : results

  return filteredResults.map((item) => ({
    ...item,
    title: titleMap[item.competitionId] || `比赛 #${item.competitionId}`,
    rankText: `第 ${item.rank} 名`,
    publishedAtText: formatCompetitionDateTime(item.publishedAt)
  }))
}

module.exports = {
  buildAwardCards
}
