import { request } from './http'

export type CampusItem = {
  id: number
  campusCode: string
  campusName: string
  status: string
}

export type SaveCampusPayload = {
  campusCode: string
  campusName: string
  status: string
}

export async function fetchCampuses(): Promise<CampusItem[]> {
  return request<CampusItem[]>('/api/admin/campuses')
}

export async function createCampus(payload: SaveCampusPayload): Promise<CampusItem> {
  return request<CampusItem>('/api/admin/campuses', {
    method: 'POST',
    body: payload
  })
}

export async function updateCampus(campusId: number, payload: SaveCampusPayload): Promise<CampusItem> {
  return request<CampusItem>(`/api/admin/campuses/${campusId}`, {
    method: 'PUT',
    body: payload
  })
}
