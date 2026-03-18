import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { describe, expect, it } from 'vitest'

describe('review workbench copy', () => {
  it('does not rely on hardcoded competition or reviewer defaults', () => {
    const source = readFileSync(
      resolve(process.cwd(), 'src/views/review/ReviewWorkbenchPage.vue'),
      'utf-8'
    )

    expect(source).not.toContain('const competitionId = ref(1)')
    expect(source).not.toContain("reviewerName: '王老师'")
  })
})
