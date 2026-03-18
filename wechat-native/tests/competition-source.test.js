const test = require('node:test')
const assert = require('node:assert/strict')
const { readFileSync } = require('node:fs')
const { resolve } = require('node:path')

test('competition utilities should not fall back to a hardcoded competition id', () => {
  const { resolveCompetitionId } = require('../utils/competition')

  assert.equal(resolveCompetitionId(undefined), 0)
  assert.equal(resolveCompetitionId({ competitionId: '12' }), 12)
})

test('competition pages should not keep demo competition defaults or sample submission url', () => {
  const detailSource = readFileSync(resolve(__dirname, '../pages/competition/detail/index.js'), 'utf-8')
  const registerSource = readFileSync(resolve(__dirname, '../pages/competition/register/index.js'), 'utf-8')
  const checkinSource = readFileSync(resolve(__dirname, '../pages/competition/checkin/index.js'), 'utf-8')
  const submissionSource = readFileSync(resolve(__dirname, '../pages/competition/submission/index.js'), 'utf-8')

  assert.equal(detailSource.includes('competitionId: 1'), false)
  assert.equal(registerSource.includes("competitionTitle: '比赛 #1'"), false)
  assert.equal(checkinSource.includes("competitionTitle: '比赛 #1'"), false)
  assert.equal(submissionSource.includes('https://example.com/work-v1.pptx'), false)
})
