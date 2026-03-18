const test = require('node:test')
const assert = require('node:assert/strict')

test('competition utilities should format route and status text', () => {
  const {
    buildCompetitionRoute,
    formatCompetitionWindow,
    resolveCompetitionStatusLabel
  } = require('../utils/competition')

  assert.equal(buildCompetitionRoute('detail', 8), '/pages/competition/detail/index?competitionId=8')
  assert.equal(
    formatCompetitionWindow('2026-03-18T09:00:00', '2026-03-20T18:30:00'),
    '2026-03-18 09:00 - 2026-03-20 18:30'
  )

  const label = resolveCompetitionStatusLabel(
    {
      signupStartAt: '2026-03-18T09:00:00',
      signupEndAt: '2026-03-20T18:30:00',
      startAt: '2026-03-21T09:00:00',
      endAt: '2026-03-22T18:30:00'
    },
    new Date('2026-03-19T10:00:00')
  )

  assert.equal(label, '报名中')
})

test('home and task utilities should expose menu metadata and task labels', () => {
  const { buildHomeMenus, HOME_MENU_TITLE_MAP, resolveHomeRoute } = require('../utils/home')
  const {
    buildOverviewCards,
    formatTaskTime,
    resolveDailyCheckinLabel,
    resolveShareTaskLabel,
    summarizeTaskProgress
  } = require('../utils/task')

  assert.deepEqual(buildHomeMenus('TEACHER'), [
    'teacher-dashboard',
    'competition-list',
    'message-center',
    'my-points',
    'competition-manage',
    'my-profile'
  ])
  assert.equal(resolveHomeRoute('my-profile'), '/pages/profile/index')
  assert.equal(HOME_MENU_TITLE_MAP['competition-manage'], '发布比赛')
  assert.equal(resolveDailyCheckinLabel(false), '立即签到')
  assert.equal(resolveShareTaskLabel(true), '今日已分享')
  assert.equal(
    summarizeTaskProgress({
      dailyCheckinDone: true,
      competitionShareDone: false,
      todayTaskPoints: 3
    }),
    '今日已完成 1/2 项任务，累计获得 3 积分'
  )
  assert.equal(formatTaskTime('2026-03-19T10:20:00'), '2026-03-19 10:20')
  assert.deepEqual(
    buildOverviewCards({
      registeredCompetitionCount: 4,
      submittedWorkCount: 3,
      awardCount: 2,
      totalPoints: 18
    }),
    [
      { label: '我的比赛', value: '4 场' },
      { label: '已交作品', value: '3 份' },
      { label: '获奖次数', value: '2 次' },
      { label: '总积分', value: '18 分' }
    ]
  )
})

test('message utilities should build chat route and conversation metadata', () => {
  const {
    buildConversationBadges,
    buildConversationPreview,
    buildMessageChatRoute
  } = require('../utils/message')

  assert.equal(
    buildMessageChatRoute({
      type: 'PRIVATE',
      peerUserId: 1002,
      title: '王老师'
    }),
    '/pages/message/chat/index?type=PRIVATE&peerUserId=1002&title=%E7%8E%8B%E8%80%81%E5%B8%88'
  )
  assert.deepEqual(buildConversationBadges({ pinned: true, muted: true }), ['置顶', '免打扰'])
  assert.equal(buildConversationPreview({ lastMessage: '' }), '会话已清空，等待新消息')
})
