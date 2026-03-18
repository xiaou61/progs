import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { describe, expect, it } from 'vitest'

describe('miniapp login copy', () => {
  it('does not expose demo buttons or preset credentials', () => {
    const jsSource = readFileSync(
      resolve(process.cwd(), '../wechat-native/pages/login/index.js'),
      'utf-8'
    )
    const templateSource = readFileSync(
      resolve(process.cwd(), '../wechat-native/pages/login/index.wxml'),
      'utf-8'
    )

    expect(jsSource).not.toContain("studentNo: 'S20260001'")
    expect(jsSource).not.toContain("password: 'Abcd1234'")
    expect(jsSource).not.toContain('applyPreset')
    expect(templateSource).not.toContain('学生演示账号')
    expect(templateSource).not.toContain('老师演示账号')
  })
})
