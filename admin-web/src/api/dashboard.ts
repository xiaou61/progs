import { request, requestText } from '@/api/http'

export type DashboardOverview = {
  totalCompetitionCount: number
  publishedCompetitionCount: number
  draftCompetitionCount: number
  offlineCompetitionCount: number
  totalRegistrationCount: number
  totalSubmissionCount: number
  totalAwardCount: number
  totalAwardPoints: number
  teacherCount: number
  studentCount: number
}

export type DashboardDistributionItem = {
  status: string
  label: string
  count: number
}

export type DashboardCompetitionItem = {
  competitionId: number
  organizerId: number
  title: string
  status: string
  registrationCount: number
  submissionCount: number
  awardCount: number
  awardPoints: number
  recommended: boolean
  pinned: boolean
  startAt: string
  endAt: string
}

export type AdminDashboardSummary = {
  overview: DashboardOverview
  statusDistribution: DashboardDistributionItem[]
  topCompetitions: DashboardCompetitionItem[]
}

export async function fetchAdminDashboardSummary(): Promise<AdminDashboardSummary> {
  return request<AdminDashboardSummary>('/api/admin/dashboard/overview')
}

export async function exportAdminDashboardCsv(): Promise<string> {
  return requestText('/api/admin/dashboard/export')
}
