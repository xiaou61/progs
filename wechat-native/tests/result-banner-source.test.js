const test = require('node:test')
const assert = require('node:assert/strict')
const { readFileSync } = require('node:fs')
const { resolve } = require('node:path')

test('result page should use competition leaderboard api and banner should prefer image rendering', () => {
  const resultPageSource = readFileSync(resolve(__dirname, '../pages/competition/result/index.js'), 'utf-8')
  const resultTemplateSource = readFileSync(resolve(__dirname, '../pages/competition/result/index.wxml'), 'utf-8')
  const resultServiceSource = readFileSync(resolve(__dirname, '../services/result.js'), 'utf-8')
  const homeTemplateSource = readFileSync(resolve(__dirname, '../pages/home/index.wxml'), 'utf-8')

  assert.match(resultPageSource, /fetchCompetitionResults/)
  assert.doesNotMatch(resultPageSource, /fetchStudentOverview\(session\.userId\)/)
  assert.match(resultTemplateSource, /winnerText/)
  assert.match(resultServiceSource, /\/api\/app\/results\/competition\//)
  assert.match(homeTemplateSource, /<image wx:if="{{item.imageUrl}}"/)
})
