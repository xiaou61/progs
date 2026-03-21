import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { describe, expect, it } from 'vitest'

describe('registration manage copy', () => {
  it('does not require manual student user id entry', () => {
    const source = readFileSync(
      resolve(process.cwd(), 'src/views/competition/RegistrationManagePage.vue'),
      'utf-8'
    )

    expect(source).not.toContain('请输入学生用户编号')
    expect(source).not.toContain('prompt(')
    expect(source).toContain('待老师确认')
    expect(source).toContain('确认签到')
    expect(source).toContain('驳回签到申请')
  })
})
