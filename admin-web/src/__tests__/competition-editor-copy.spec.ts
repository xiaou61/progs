import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { describe, expect, it } from 'vitest'

describe('competition editor copy', () => {
  it('does not require manual organizer id entry', () => {
    const source = readFileSync(
      resolve(process.cwd(), 'src/views/competition/CompetitionEditorPage.vue'),
      'utf-8'
    )

    expect(source).not.toContain('organizerId: 1001')
    expect(source).not.toContain('请输入发起人编号')
  })
})
