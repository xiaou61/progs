import { request } from './http'

import type { CompetitionParticipantType, PublishCompetitionPayload } from './competition'

export type CompetitionManageItem = {
  id: number
  organizerId: number
  title: string
  description: string
  signupStartAt: string
  signupEndAt: string
  startAt: string
  endAt: string
  quota: number
  status: string
  recommended: boolean
  pinned: boolean
  participantType: CompetitionParticipantType
  advisorTeacherId: number | null
  advisorTeacherName: string | null
}

export type CompetitionDraftPayload = PublishCompetitionPayload

export type CompetitionUpdatePayload = CompetitionDraftPayload & {
  status: 'DRAFT' | 'PUBLISHED' | 'OFFLINE'
}

export type CompetitionFeaturePayload = {
  recommended: boolean
  pinned: boolean
}

export async function fetchManagedCompetitions() {
  return request<CompetitionManageItem[]>('/api/admin/competitions')
}

export async function saveCompetitionDraft(payload: CompetitionDraftPayload) {
  const result = await request<{ competitionId: number }>('/api/admin/competitions/draft', {
    method: 'POST',
    body: payload
  })
  return result.competitionId
}

export async function updateCompetition(competitionId: number, payload: CompetitionUpdatePayload) {
  return request<CompetitionManageItem>(`/api/admin/competitions/${competitionId}`, {
    method: 'PUT',
    body: payload
  })
}

export async function updateCompetitionFeature(competitionId: number, payload: CompetitionFeaturePayload) {
  return request<CompetitionManageItem>(`/api/admin/competitions/${competitionId}/feature`, {
    method: 'POST',
    body: payload
  })
}

export async function offlineCompetition(competitionId: number) {
  const result = await request<{ offline: boolean }>(`/api/admin/competitions/${competitionId}/offline`, {
    method: 'POST'
  })
  return result.offline
}
