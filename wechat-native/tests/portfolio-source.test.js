const test = require('node:test')
const assert = require('node:assert/strict')
const { readFileSync } = require('node:fs')
const { resolve } = require('node:path')

test('portfolio utilities should build work and award cards with tab-aware ordering', () => {
  const { buildPortfolioState } = require('../utils/portfolio')

  const state = buildPortfolioState({
    tab: 'awards',
    competitions: [
      { id: 8, title: '蓝桥杯' },
      { id: 9, title: '创新挑战赛' }
    ],
    submissions: [
      {
        id: 101,
        competitionId: 8,
        userId: 2001,
        fileUrl: 'https://files.example.com/work-v3.pptx',
        versionNo: 3,
        submittedAt: '2026-03-21T10:00:00'
      }
    ],
    results: [
      {
        id: 201,
        competitionId: 9,
        score: 96,
        rank: 1,
        awardName: '一等奖',
        points: 30,
        publishedAt: '2026-03-21T12:00:00'
      }
    ]
  })

  assert.equal(state.activeTab, 'awards')
  assert.equal(state.sections[0].key, 'awards')
  assert.equal(state.sections[1].key, 'works')
  assert.equal(state.workCards[0].competitionTitle, '蓝桥杯')
  assert.equal(state.workCards[0].versionText, 'v3')
  assert.equal(state.workCards[0].actionRoute, '/pages/competition/submission/index?competitionId=8')
  assert.equal(state.awardCards[0].title, '创新挑战赛')
  assert.equal(state.awardCards[0].actionRoute, '/pages/competition/result/index?competitionId=9')
})

test('portfolio page should be registered and connected with overview card navigation', () => {
  const appConfig = readFileSync(resolve(__dirname, '../app.json'), 'utf-8')
  const homeSource = readFileSync(resolve(__dirname, '../pages/home/index.js'), 'utf-8')
  const homeTemplateSource = readFileSync(resolve(__dirname, '../pages/home/index.wxml'), 'utf-8')
  const pageSource = readFileSync(resolve(__dirname, '../pages/competition/portfolio/index.js'), 'utf-8')
  const serviceSource = readFileSync(resolve(__dirname, '../services/submission.js'), 'utf-8')

  assert.match(appConfig, /pages\/competition\/portfolio\/index/)
  assert.match(homeSource, /openOverviewCard\(/)
  assert.match(homeSource, /resolveOverviewRoute/)
  assert.match(homeTemplateSource, /bindtap="openOverviewCard"/)
  assert.match(homeTemplateSource, /data-key="{{item.key}}"/)
  assert.match(serviceSource, /fetchUserSubmissions/)
  assert.match(pageSource, /fetchUserSubmissions/)
  assert.match(pageSource, /fetchStudentOverview/)
  assert.match(pageSource, /fetchCompetitions/)
  assert.match(pageSource, /tab/)
  assert.match(pageSource, /openWorkCard\(/)
  assert.match(pageSource, /openAwardCard\(/)
})
