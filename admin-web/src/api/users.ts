import { request, requestText } from './http'

export type UserItem = {
  id: number
  studentNo: string
  realName: string
  phone: string
  roleCode: string
  status: string
  violationMarked?: boolean
  violationReason?: string | null
}

export type OperationLogItem = {
  id: number
  operatorName: string
  action: string
  target: string
  detail: string | null
  createdAt: string
}

export type UserLogQuery = {
  operatorName?: string
  action?: string
  target?: string
}

export async function fetchUsers(): Promise<UserItem[]> {
  return request<UserItem[]>('/api/admin/users')
}

export async function freezeUser(userId: number, reason: string) {
  const result = await request<{ updated: boolean }>(`/api/admin/users/${userId}/freeze`, {
    method: 'POST',
    body: {
      reason
    }
  })
  return result.updated
}

export async function unfreezeUser(userId: number) {
  const result = await request<{ updated: boolean }>(`/api/admin/users/${userId}/unfreeze`, {
    method: 'POST'
  })
  return result.updated
}

export async function resetUserPassword(userId: number, newPassword: string) {
  const result = await request<{ updated: boolean }>(`/api/admin/users/${userId}/reset-password`, {
    method: 'POST',
    body: {
      newPassword
    }
  })
  return result.updated
}

export async function assignUserRole(userId: number, roleCode: string): Promise<UserItem> {
  return request<UserItem>(`/api/admin/users/${userId}/role`, {
    method: 'POST',
    body: {
      roleCode
    }
  })
}

export async function markUserViolation(userId: number, reason: string) {
  const result = await request<{ updated: boolean }>(`/api/admin/users/${userId}/violation`, {
    method: 'POST',
    body: {
      violating: true,
      reason
    }
  })
  return result.updated
}

export async function clearUserViolation(userId: number, reason: string) {
  const result = await request<{ updated: boolean }>(`/api/admin/users/${userId}/violation`, {
    method: 'POST',
    body: {
      violating: false,
      reason
    }
  })
  return result.updated
}

export async function fetchUserLogs(query: UserLogQuery = {}): Promise<OperationLogItem[]> {
  return request<OperationLogItem[]>(`/api/admin/logs${buildQuery(query)}`)
}

export async function exportUserLogsCsv(query: UserLogQuery = {}) {
  return requestText(`/api/admin/logs/export${buildQuery(query)}`)
}

function buildQuery(query: UserLogQuery) {
  const searchParams = new URLSearchParams()

  if (query.operatorName?.trim()) {
    searchParams.set('operatorName', query.operatorName.trim())
  }
  if (query.action?.trim()) {
    searchParams.set('action', query.action.trim())
  }
  if (query.target?.trim()) {
    searchParams.set('target', query.target.trim())
  }

  const serialized = searchParams.toString()
  return serialized ? `?${serialized}` : ''
}
