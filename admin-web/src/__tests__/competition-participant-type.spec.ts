import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { describe, expect, it } from 'vitest'

describe('competition participant type behavior', () => {
  it('supports participant type and advisor teacher fields in admin competition editor', () => {
    const editorSource = readFileSync(
      resolve(process.cwd(), 'src/views/competition/CompetitionEditorPage.vue'),
      'utf-8'
    )
    const formSource = readFileSync(
      resolve(process.cwd(), 'src/utils/competition-form.ts'),
      'utf-8'
    )

    expect(editorSource).toMatch(/participantType/)
    expect(editorSource).toMatch(/advisorTeacherId/)
    expect(editorSource).toMatch(/STUDENT_ONLY/)
    expect(formSource).toMatch(/participantType/)
    expect(formSource).toMatch(/advisorTeacherId/)
  })
})
