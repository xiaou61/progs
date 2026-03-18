import type { SaveRolePayload } from '@/api/roles'

export type RoleFormValues = {
  roleCode: string
  roleName: string
  description: string
  permissionCodes: string[]
}

export const permissionCatalog = [
  { code: 'USER_MANAGE', label: '用户治理' },
  { code: 'ROLE_MANAGE', label: '角色权限' },
  { code: 'CAMPUS_MANAGE', label: '校区管理' },
  { code: 'COMPETITION_MANAGE', label: '比赛管理' },
  { code: 'REGISTRATION_MANAGE', label: '报名管理' },
  { code: 'REVIEW_MANAGE', label: '评审管理' },
  { code: 'SCORE_PUBLISH', label: '结果发布' },
  { code: 'RESULT_VIEW', label: '结果查看' },
  { code: 'MESSAGE_USE', label: '消息中心' },
  { code: 'SYSTEM_MANAGE', label: '系统设置' },
  { code: 'LOG_VIEW', label: '日志查看' }
] as const

export function createEmptyRoleForm(): RoleFormValues {
  return {
    roleCode: '',
    roleName: '',
    description: '',
    permissionCodes: []
  }
}

export function validateRoleForm(form: RoleFormValues) {
  const roleCode = form.roleCode.trim()
  const roleName = form.roleName.trim()
  if (!roleCode) {
    return '角色编码不能为空'
  }
  if (!/^[A-Za-z][A-Za-z0-9_]{1,31}$/.test(roleCode)) {
    return '角色编码格式不合法'
  }
  if (!roleName) {
    return '角色名称不能为空'
  }
  if (normalizePermissionCodes(form.permissionCodes).length === 0) {
    return '请至少选择一个权限'
  }
  return ''
}

export function buildRolePayload(form: RoleFormValues): SaveRolePayload {
  return {
    roleCode: form.roleCode.trim().toUpperCase(),
    roleName: form.roleName.trim(),
    description: form.description.trim(),
    permissionCodes: normalizePermissionCodes(form.permissionCodes)
  }
}

export function validateResetPassword(password: string) {
  return password.trim().length < 8 ? '新密码长度不能少于 8 位' : ''
}

export function normalizePermissionCodes(permissionCodes: string[]) {
  return permissionCodes
    .map((code) => code.trim().toUpperCase())
    .filter((code) => code.length > 0)
    .filter((code, index, list) => list.indexOf(code) === index)
}
