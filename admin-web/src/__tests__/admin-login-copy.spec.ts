import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { describe, expect, it } from 'vitest'

describe('admin login copy', () => {
  it('does not expose demo credentials or preset actions', () => {
    const source = readFileSync(
      resolve(process.cwd(), 'src/views/login/LoginPage.vue'),
      'utf-8'
    )

    expect(source).not.toContain('演示账号')
    expect(source).not.toContain('演示密码')
    expect(source).not.toContain("studentNo: 'A20260001'")
    expect(source).not.toContain("password: 'Abcd1234'")
    expect(source).not.toContain('applyPreset')
  })
})
