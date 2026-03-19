const test = require('node:test')
const assert = require('node:assert/strict')

test('teacher competition service should request teacher-scoped endpoints', async () => {
  delete require.cache[require.resolve('../services/competition')]

  const calls = []
  global.wx = {
    _storage: {
      'campus-competition-session': {
        userId: 3,
        roleCode: 'TEACHER',
        token: 'teacher-token'
      }
    },
    getStorageSync(key) {
      return this._storage[key]
    },
    request(options) {
      calls.push({
        url: options.url,
        method: options.method,
        data: options.data,
        header: options.header
      })
      options.success({
        statusCode: 200,
        data: {
          code: 0,
          data: options.method === 'GET' ? [] : { competitionId: 9, id: 9, offline: true }
        }
      })
    }
  }

  const {
    fetchTeacherManagedCompetitions,
    saveTeacherCompetitionDraft,
    updateTeacherCompetition
  } = require('../services/competition')

  await fetchTeacherManagedCompetitions(3)
  await saveTeacherCompetitionDraft(3, {
    title: '老师端草稿赛',
    description: '草稿描述',
    signupStartAt: '2026-03-19T09:00:00',
    signupEndAt: '2026-03-20T18:00:00',
    startAt: '2026-03-21T09:00:00',
    endAt: '2026-03-22T18:00:00',
    quota: 80
  })
  await updateTeacherCompetition(3, 9, {
    title: '老师端草稿赛（更新）',
    description: '更新后描述',
    signupStartAt: '2026-03-19T09:00:00',
    signupEndAt: '2026-03-21T18:00:00',
    startAt: '2026-03-22T09:00:00',
    endAt: '2026-03-23T18:00:00',
    quota: 100,
    status: 'DRAFT'
  })

  assert.deepEqual(
    calls.map((item) => `${item.method} ${item.url}`),
    [
      'GET http://127.0.0.1:8080/api/app/teachers/3/competitions',
      'POST http://127.0.0.1:8080/api/app/teachers/3/competitions/draft',
      'PUT http://127.0.0.1:8080/api/app/teachers/3/competitions/9'
    ]
  )
  assert.equal(calls[1].header.Authorization, 'Bearer teacher-token')
  assert.equal(calls[2].data.status, 'DRAFT')
})
