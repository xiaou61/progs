import { request } from './http'

export type RoleItem = {
  id: number
  roleCode: string
  roleName: string
  description: string | null
  permissionCodes: string[]
  builtIn: boolean
  status: string
  userCount: number
}

export type SaveRolePayload = {
  roleCode?: string
  roleName: string
  description?: string
  permissionCodes: string[]
}

export async function fetchRoles(): Promise<RoleItem[]> {
  return request<RoleItem[]>('/api/admin/roles')
}

export async function createRole(payload: SaveRolePayload): Promise<RoleItem> {
  return request<RoleItem>('/api/admin/roles', {
    method: 'POST',
    body: payload
  })
}

export async function updateRole(roleCode: string, payload: SaveRolePayload): Promise<RoleItem> {
  return request<RoleItem>(`/api/admin/roles/${roleCode}`, {
    method: 'PUT',
    body: payload
  })
}
