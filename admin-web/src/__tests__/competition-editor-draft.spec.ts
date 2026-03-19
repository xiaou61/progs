import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { describe, expect, it } from 'vitest'

describe('competition editor draft behavior', () => {
  it('updates the selected competition instead of creating a new draft', () => {
    const source = readFileSync(
      resolve(process.cwd(), 'src/views/competition/CompetitionEditorPage.vue'),
      'utf-8'
    )

    expect(source).toMatch(/async function submitDraft\(\)[\s\S]*selectedCompetitionId\.value[\s\S]*status:\s*'DRAFT'/)
  })
})
