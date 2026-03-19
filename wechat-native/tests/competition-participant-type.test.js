const test = require('node:test')
const assert = require('node:assert/strict')
const { readFileSync } = require('node:fs')
const { resolve } = require('node:path')

test('miniapp competition pages should expose participant type and advisor teacher info', () => {
  const detailSource = readFileSync(resolve(__dirname, '../pages/competition/detail/index.js'), 'utf-8')
  const detailTemplateSource = readFileSync(resolve(__dirname, '../pages/competition/detail/index.wxml'), 'utf-8')
  const registerSource = readFileSync(resolve(__dirname, '../pages/competition/register/index.js'), 'utf-8')
  const registerTemplateSource = readFileSync(resolve(__dirname, '../pages/competition/register/index.wxml'), 'utf-8')

  assert.match(detailSource, /participantType|advisorTeacherName/)
  assert.match(detailTemplateSource, /participantType|advisorTeacherName/)
  assert.match(registerSource, /participantType/)
  assert.match(registerTemplateSource, /participantType|advisorTeacherName/)
  assert.match(registerSource, /STUDENT_ONLY|TEACHER_ONLY/)
})
