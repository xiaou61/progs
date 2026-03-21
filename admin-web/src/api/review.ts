import { request } from './http'

export type ReviewTaskItem = {
  competitionId: number
  submissionId: number
  studentId: number
  fileUrl: string
  versionNo: number
  submittedAt: string | null
  reviewerName: string | null
  status: string
  reviewComment: string | null
  suggestedScore: number | null
  reviewedAt: string | null
}

export type CompetitionScoreItem = {
  id: number
  competitionId: number
  studentId: number
  score: number
  rank: number
  awardName: string
  points: number
  publishedAt: string
  reviewerName: string | null
  reviewComment: string | null
  certificateNo: string | null
  certificateTitle: string | null
}

export type SubmitReviewPayload = {
  competitionId: number
  submissionId: number
  studentId: number
  reviewerName: string
  reviewComment: string
  suggestedScore: number
}

export type PublishResultPayload = {
  competitionId: number
  studentId: number
  score: number
  rank: number
  awardName: string
  points: number
  reviewerName?: string
  reviewComment?: string
}

export async function fetchReviewTasks(competitionId: number): Promise<ReviewTaskItem[]> {
  return request<ReviewTaskItem[]>(`/api/admin/reviews/tasks?competitionId=${competitionId}`)
}

export async function fetchCompetitionScores(competitionId: number): Promise<CompetitionScoreItem[]> {
  return request<CompetitionScoreItem[]>(`/api/admin/scores/competition/${competitionId}`)
}

export async function submitReviewTask(payload: SubmitReviewPayload): Promise<boolean> {
  const result = await request<{ reviewed: boolean }>('/api/admin/reviews/submit', {
    method: 'POST',
    body: payload
  })
  return result.reviewed
}

export async function publishCompetitionResult(payload: PublishResultPayload): Promise<number> {
  const result = await request<{ scoreId: number }>('/api/admin/scores/publish', {
    method: 'POST',
    body: payload
  })
  return result.scoreId
}
