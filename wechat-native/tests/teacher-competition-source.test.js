const test = require('node:test')
const assert = require('node:assert/strict')
const { readFileSync } = require('node:fs')
const { resolve } = require('node:path')

test('teacher competition editor should update selected competition when saving draft', () => {
  const source = readFileSync(resolve(__dirname, '../pages/teacher/competition-editor/index.js'), 'utf-8')

  assert.match(
    source,
    /async submitDraft\(\)[\s\S]*this\.data\.selectedCompetitionId[\s\S]*status:\s*'DRAFT'/
  )
})

test('teacher competition editor should use date and time pickers instead of manual datetime input', () => {
  const source = readFileSync(resolve(__dirname, '../pages/teacher/competition-editor/index.js'), 'utf-8')
  const templateSource = readFileSync(resolve(__dirname, '../pages/teacher/competition-editor/index.wxml'), 'utf-8')

  assert.equal(templateSource.includes('时间请按 YYYY-MM-DDTHH:mm:ss 填写。'), false)
  assert.equal(templateSource.includes('例如 2026-03-18T09:00:00'), false)
  assert.match(templateSource, /picker mode="date"/)
  assert.match(templateSource, /picker mode="time"/)
  assert.match(templateSource, /bindchange="handleDateChange"/)
  assert.match(templateSource, /bindchange="handleTimeChange"/)
  assert.match(source, /function buildTimePickerState\(/)
  assert.match(source, /handleDateChange\(event\)/)
  assert.match(source, /handleTimeChange\(event\)/)
})

test('teacher competition editor should include feature flags in the main save payload', () => {
  const source = readFileSync(resolve(__dirname, '../pages/teacher/competition-editor/index.js'), 'utf-8')

  assert.match(source, /buildPayload\(\)[\s\S]*recommended:\s*this\.data\.featureForm\.recommended/)
  assert.match(source, /buildPayload\(\)[\s\S]*pinned:\s*this\.data\.featureForm\.pinned/)
})
