import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { describe, expect, it } from 'vitest'

describe('review workbench download', () => {
  it('supports clicking a work card to download its file', () => {
    const source = readFileSync(
      resolve(process.cwd(), 'src/views/review/ReviewWorkbenchPage.vue'),
      'utf-8'
    )

    expect(source).toContain('downloadTaskFile(')
    expect(source).toContain('@click="downloadTaskFile(item)"')
    expect(source).toContain('@click.stop="pickTask(item)"')
    expect(source).toContain('@click.stop="fillStudentId(item.studentId)"')
  })
})
