import type { CreateUserPayload } from '@/api/users'

export type UserCreateFormValues = {
  studentNo: string
  realName: string
  phone: string
  roleCode: string
  password: string
}

export function createEmptyUserCreateForm(): UserCreateFormValues {
  return {
    studentNo: '',
    realName: '',
    phone: '',
    roleCode: 'STUDENT',
    password: ''
  }
}

export function validateCreateUserForm(form: UserCreateFormValues) {
  if (!form.studentNo.trim()) {
    return '学号不能为空'
  }
  if (!form.realName.trim()) {
    return '姓名不能为空'
  }
  if (!form.phone.trim()) {
    return '手机号不能为空'
  }
  if (!form.roleCode.trim()) {
    return '角色不能为空'
  }
  if (form.password.trim().length < 8) {
    return '初始密码长度不能少于 8 位'
  }
  return ''
}

export function buildCreateUserPayload(form: UserCreateFormValues): CreateUserPayload {
  return {
    studentNo: form.studentNo.trim(),
    realName: form.realName.trim(),
    phone: form.phone.trim(),
    roleCode: form.roleCode.trim().toUpperCase(),
    password: form.password
  }
}
