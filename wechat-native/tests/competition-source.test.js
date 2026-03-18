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
  const registerTemplateSource = readFileSync(resolve(__dirname, '../pages/competition/register/index.wxml'), 'utf-8')
  const checkinSource = readFileSync(resolve(__dirname, '../pages/competition/checkin/index.js'), 'utf-8')
  const submissionSource = readFileSync(resolve(__dirname, '../pages/competition/submission/index.js'), 'utf-8')
  const submissionTemplateSource = readFileSync(
    resolve(__dirname, '../pages/competition/submission/index.wxml'),
    'utf-8'
  )
  const homeTemplateSource = readFileSync(resolve(__dirname, '../pages/home/index.wxml'), 'utf-8')

  assert.equal(detailSource.includes('competitionId: 1'), false)
  assert.equal(registerSource.includes("competitionTitle: '比赛 #1'"), false)
  assert.equal(checkinSource.includes("competitionTitle: '比赛 #1'"), false)
  assert.equal(submissionSource.includes('https://example.com/work-v1.pptx'), false)
  assert.equal(submissionTemplateSource.includes('请输入作品文件 URL'), false)
  assert.equal(homeTemplateSource.includes('用户 #{{userId}}'), false)
  assert.equal(registerTemplateSource.includes('用户 #{{userId}}'), false)
})
