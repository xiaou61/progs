import { describe, expect, it } from 'vitest'
import {
  buildRolePayload,
  createEmptyRoleForm,
  permissionCatalog,
  validateResetPassword,
  validateRoleForm
} from '@/utils/role-governance'

describe('role governance utils', () => {
  it('should normalize role form into backend payload', () => {
    const payload = buildRolePayload({
      roleCode: ' judge ',
      roleName: ' 赛事评委 ',
      description: ' 负责评审与结果发布 ',
      permissionCodes: [' review_manage ', 'SCORE_PUBLISH', 'review_manage']
    })

    expect(payload).toEqual({
      roleCode: 'JUDGE',
      roleName: '赛事评委',
      description: '负责评审与结果发布',
      permissionCodes: ['REVIEW_MANAGE', 'SCORE_PUBLISH']
    })
  })

  it('should validate role form and password rule', () => {
    const emptyForm = createEmptyRoleForm()

    expect(validateRoleForm(emptyForm)).toBe('角色编码不能为空')
    expect(validateResetPassword('1234567')).toBe('新密码长度不能少于 8 位')
    expect(validateResetPassword('Abcd5678')).toBe('')
    expect(permissionCatalog.some((item) => item.code === 'ROLE_MANAGE')).toBe(true)
  })
})
