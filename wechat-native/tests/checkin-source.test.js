const test = require('node:test')
const assert = require('node:assert/strict')
const { readFileSync } = require('node:fs')
const { resolve } = require('node:path')

test('checkin page should describe approval-based workflow', () => {
  const source = readFileSync(resolve(__dirname, '../pages/competition/checkin/index.js'), 'utf-8')
  const templateSource = readFileSync(resolve(__dirname, '../pages/competition/checkin/index.wxml'), 'utf-8')

  assert.match(source, /提交签到申请/)
  assert.match(source, /待老师确认/)
  assert.match(source, /申请已驳回，可重新提交/)
  assert.match(templateSource, /submitButtonText/)
  assert.match(source, /fetchUserRegistration/)
  assert.match(source, /reviewRemark:/)
})
