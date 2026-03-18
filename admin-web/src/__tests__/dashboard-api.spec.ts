import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { exportAdminDashboardCsv, fetchAdminDashboardSummary } from '@/api/dashboard'

describe('admin dashboard api', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn())
  })

  afterEach(() => {
    vi.unstubAllGlobals()
    vi.restoreAllMocks()
  })

  it('should request admin dashboard summary and csv export', async () => {
    const fetchMock = vi.mocked(fetch)
    fetchMock
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          code: 0,
          message: 'ok',
          data: {
            overview: {
              totalCompetitionCount: 2,
              publishedCompetitionCount: 1,
              draftCompetitionCount: 1,
              offlineCompetitionCount: 0,
              totalRegistrationCount: 1,
              totalSubmissionCount: 1,
              totalAwardCount: 1,
              totalAwardPoints: 30,
              teacherCount: 1,
              studentCount: 1
            },
            statusDistribution: [
              { status: 'PUBLISHED', label: '已发布', count: 1 }
            ],
            topCompetitions: [
              {
                competitionId: 8,
                organizerId: 1001,
                title: '数据大屏实战赛',
                status: 'PUBLISHED',
                registrationCount: 1,
                submissionCount: 1,
                awardCount: 1,
                awardPoints: 30,
                recommended: false,
                pinned: false,
                startAt: '2026-03-18T09:00:00',
                endAt: '2026-03-18T18:00:00'
              }
            ]
          }
        })
      } as Response)
      .mockResolvedValueOnce({
        ok: true,
        text: async () => 'title,status,registrations,submissions,awards,points\n数据大屏实战赛,PUBLISHED,1,1,1,30\n'
      } as Response)

    const summary = await fetchAdminDashboardSummary()
    const csv = await exportAdminDashboardCsv()

    expect(fetchMock).toHaveBeenNthCalledWith(1, '/api/admin/dashboard/overview', { method: 'GET' })
    expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/admin/dashboard/export', { method: 'GET' })
    expect(summary.overview.totalCompetitionCount).toBe(2)
    expect(summary.topCompetitions[0]?.title).toBe('数据大屏实战赛')
    expect(csv).toContain('数据大屏实战赛,PUBLISHED,1,1,1,30')
  })
})
