import { request } from './http'

export type CampusItem = {
  id: number
  campusCode: string
  campusName: string
  status: string
}

export async function fetchCampuses(): Promise<CampusItem[]> {
  return request<CampusItem[]>('/api/admin/campuses')
}
