const test = require('node:test')
const assert = require('node:assert/strict')
const { readFileSync } = require('node:fs')
const { resolve } = require('node:path')

test('cancel permission should be disabled after signup deadline', () => {
  const { resolveCancelPermission } = require('../utils/registration-permission')

  assert.deepEqual(
    resolveCancelPermission(
      { signupEndAt: '2026-03-21T16:00:00' },
      { status: 'REGISTERED' },
      '2026-03-21T15:59:00'
    ),
    {
      canCancel: true,
      hint: ''
    }
  )

  assert.deepEqual(
    resolveCancelPermission(
      { signupEndAt: '2026-03-21T16:00:00' },
      { status: 'REGISTERED' },
      '2026-03-21T16:01:00'
    ),
    {
      canCancel: false,
      hint: '报名已截止，不能取消报名'
    }
  )
})

test('competition register page should gate cancel action by signup deadline', () => {
  const registerSource = readFileSync(resolve(__dirname, '../pages/competition/register/index.js'), 'utf-8')
  const registerTemplateSource = readFileSync(resolve(__dirname, '../pages/competition/register/index.wxml'), 'utf-8')

  assert.match(registerSource, /canCancel|cancelHint|signupEndAt/)
  assert.match(registerTemplateSource, /canCancel|cancelHint/)
})
