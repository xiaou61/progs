import { request } from './http'

export type CompetitionParticipantType = 'STUDENT_ONLY' | 'TEACHER_ONLY'

export type PublishCompetitionPayload = {
  organizerId: number
  title: string
  description: string
  signupStartAt: string
  signupEndAt: string
  startAt: string
  endAt: string
  quota: number
  participantType: CompetitionParticipantType
  advisorTeacherId: number | null
}

export async function publishCompetition(payload: PublishCompetitionPayload): Promise<number> {
  const result = await request<{ competitionId: number }>('/api/admin/competitions', {
    method: 'POST',
    body: payload
  })
  return result.competitionId
}
