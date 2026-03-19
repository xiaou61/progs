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
