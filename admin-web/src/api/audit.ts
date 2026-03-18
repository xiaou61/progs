import { request } from './http'

export type AuditRuleSummary = {
  sensitiveWords: string[]
  allowedSubmissionExtensions: string[]
  blockedSubmissionExtensions: string[]
}

export type ViolationRecordItem = {
  id: number
  scene: string
  bizId: number | null
  userId: number | null
  userName: string
  reason: string
  hitWords: string[]
  contentSnippet: string
  createdAt: string
}

export async function fetchAuditRules(): Promise<AuditRuleSummary> {
  return request<AuditRuleSummary>('/api/admin/audit/rules')
}

export async function fetchViolationRecords(): Promise<ViolationRecordItem[]> {
  return request<ViolationRecordItem[]>('/api/admin/audit/violations')
}
