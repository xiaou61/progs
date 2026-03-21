const test = require('node:test')
const assert = require('node:assert/strict')

test('result utils should keep all results when competition id is empty', () => {
  const { buildAwardCards } = require('../utils/result')

  const cards = buildAwardCards({
    competitionId: 0,
    results: [
      {
        id: 1,
        competitionId: 8,
        studentNo: 'S20260001',
        studentName: '张同学',
        awardName: '一等奖',
        rank: 1,
        score: 96,
        points: 30,
        publishedAt: '2026-03-18T10:20:00'
      },
      {
        id: 2,
        competitionId: 9,
        awardName: '二等奖',
        rank: 2,
        score: 90,
        points: 20,
        publishedAt: '2026-03-18T12:00:00'
      }
    ],
    competitions: [
      { id: 8, title: '创新赛 A' },
      { id: 9, title: '创新赛 B' }
    ]
  })

  assert.equal(cards.length, 2)
  assert.equal(cards[0].title, '创新赛 A')
  assert.equal(cards[0].winnerText, '张同学 · S20260001')
  assert.equal(cards[1].title, '创新赛 B')
})

test('result utils should filter to a single competition when competition id is provided', () => {
  const { buildAwardCards } = require('../utils/result')

  const cards = buildAwardCards({
    competitionId: 8,
    results: [
      {
        id: 1,
        competitionId: 8,
        studentNo: 'S20260001',
        studentName: '张同学',
        awardName: '一等奖',
        rank: 1,
        score: 96,
        points: 30,
        publishedAt: '2026-03-18T10:20:00'
      },
      {
        id: 2,
        competitionId: 9,
        awardName: '二等奖',
        rank: 2,
        score: 90,
        points: 20,
        publishedAt: '2026-03-18T12:00:00'
      }
    ],
    competitions: [
      { id: 8, title: '创新赛 A' },
      { id: 9, title: '创新赛 B' }
    ]
  })

  assert.equal(cards.length, 1)
  assert.equal(cards[0].competitionId, 8)
  assert.equal(cards[0].title, '创新赛 A')
  assert.equal(cards[0].rankText, '第 1 名')
  assert.equal(cards[0].winnerText, '张同学 · S20260001')
  assert.equal(cards[0].publishedAtText, '2026-03-18 10:20')
})
