import { request } from './http'

export type LoginPayload = {
  studentNo: string
  password: string
  roleCode: 'STUDENT' | 'TEACHER' | 'ADMIN'
}

export type LoginResult = {
  token: string
  userId: number
  roleCode: 'STUDENT' | 'TEACHER' | 'ADMIN'
}

export async function login(payload: LoginPayload) {
  return request<LoginResult>('/api/app/auth/login', {
    method: 'POST',
    body: payload
  })
}
