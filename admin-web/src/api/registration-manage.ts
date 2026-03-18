import { request } from './http'

export type RegistrationManageItem = {
  id: number
  competitionId: number
  userId: number
  status: 'REGISTERED' | 'CANCELLED' | 'REJECTED'
  attendanceStatus: 'PENDING' | 'PRESENT' | 'ABSENT'
  remark: string | null
}

export type ManualRegistrationPayload = {
  competitionId: number
  userId: number
  remark: string
}

export async function fetchCompetitionRegistrations(competitionId: number) {
  return request<RegistrationManageItem[]>(`/api/admin/registrations/competition/${competitionId}`)
}

export async function manualAddRegistration(payload: ManualRegistrationPayload) {
  const result = await request<{ registrationId: number }>('/api/admin/registrations/manual', {
    method: 'POST',
    body: payload
  })
  return result.registrationId
}

export async function rejectRegistration(registrationId: number, reason: string) {
  const result = await request<{ rejected: boolean }>(`/api/admin/registrations/${registrationId}/reject`, {
    method: 'POST',
    body: { reason }
  })
  return result.rejected
}

export async function markRegistrationAttendance(
  registrationId: number,
  attendanceStatus: 'PRESENT' | 'ABSENT'
) {
  const result = await request<{ marked: boolean }>(`/api/admin/registrations/${registrationId}/attendance`, {
    method: 'POST',
    body: { attendanceStatus }
  })
  return result.marked
}
