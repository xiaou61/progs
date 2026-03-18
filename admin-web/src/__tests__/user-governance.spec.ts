import { describe, expect, it } from 'vitest'
import {
  buildCreateUserPayload,
  createEmptyUserCreateForm,
  validateCreateUserForm
} from '@/utils/user-governance'

describe('user governance form helpers', () => {
  it('validates create user form and normalizes payload', () => {
    const emptyForm = createEmptyUserCreateForm()
    expect(validateCreateUserForm(emptyForm)).toBe('学号不能为空')

    const validForm = {
      studentNo: ' s20260101 ',
      realName: ' 新建学生 ',
      phone: ' 13800000101 ',
      roleCode: 'student',
      password: 'Abcd5678'
    }

    expect(validateCreateUserForm(validForm)).toBe('')
    expect(buildCreateUserPayload(validForm)).toEqual({
      studentNo: 's20260101',
      realName: '新建学生',
      phone: '13800000101',
      roleCode: 'STUDENT',
      password: 'Abcd5678'
    })
  })
})
