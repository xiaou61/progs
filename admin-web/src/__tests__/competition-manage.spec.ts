import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import {
  fetchManagedCompetitions,
  offlineCompetition,
  saveCompetitionDraft,
  updateCompetition,
  updateCompetitionFeature
} from '@/api/competition-manage'

describe('competition manage api', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn())
  })

  afterEach(() => {
    vi.unstubAllGlobals()
    vi.restoreAllMocks()
  })

  it('should request managed competitions and save a draft', async () => {
    const fetchMock = vi.mocked(fetch)
    fetchMock
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          code: 0,
          message: 'ok',
          data: [
            {
              id: 9,
              organizerId: 1001,
              title: '校园未来设计赛',
              description: '更新后的比赛描述',
              signupStartAt: '2026-03-17T09:00:00',
              signupEndAt: '2026-03-18T18:00:00',
              startAt: '2026-03-19T09:00:00',
              endAt: '2026-03-20T18:00:00',
              quota: 120,
              status: 'PUBLISHED',
              recommended: true,
              pinned: false,
              participantType: 'STUDENT_ONLY',
              advisorTeacherId: 2001,
              advisorTeacherName: '王老师'
            }
          ]
        })
      } as Response)
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          code: 0,
          message: 'ok',
          data: {
            competitionId: 12
          }
        })
      } as Response)

    const list = await fetchManagedCompetitions()
    const draftId = await saveCompetitionDraft({
      organizerId: 1001,
      title: '校园未来设计赛（草稿）',
      description: '草稿阶段的比赛描述',
      signupStartAt: '2026-03-18T09:00:00',
      signupEndAt: '2026-03-20T18:00:00',
      startAt: '2026-03-21T09:00:00',
      endAt: '2026-03-22T18:00:00',
      quota: 80,
      participantType: 'STUDENT_ONLY',
      advisorTeacherId: 2001
    })

    expect(fetchMock).toHaveBeenNthCalledWith(1, '/api/admin/competitions', { method: 'GET' })
    expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/admin/competitions/draft', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        organizerId: 1001,
        title: '校园未来设计赛（草稿）',
        description: '草稿阶段的比赛描述',
        signupStartAt: '2026-03-18T09:00:00',
        signupEndAt: '2026-03-20T18:00:00',
        startAt: '2026-03-21T09:00:00',
        endAt: '2026-03-22T18:00:00',
        quota: 80,
        participantType: 'STUDENT_ONLY',
        advisorTeacherId: 2001
      })
    })
    expect(list[0]?.recommended).toBe(true)
    expect(draftId).toBe(12)
  })

  it('should update feature flags and offline a competition', async () => {
    const fetchMock = vi.mocked(fetch)
    fetchMock
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          code: 0,
          message: 'ok',
          data: {
            id: 9,
            status: 'PUBLISHED',
            recommended: false,
            pinned: false
          }
        })
      } as Response)
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          code: 0,
          message: 'ok',
          data: {
            id: 9,
            status: 'PUBLISHED',
            recommended: true,
            pinned: true
          }
        })
      } as Response)
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          code: 0,
          message: 'ok',
          data: {
            offline: true
          }
        })
      } as Response)

    await updateCompetition(9, {
      organizerId: 1001,
      title: '校园未来设计赛',
      description: '更新后的比赛描述',
      signupStartAt: '2026-03-17T09:00:00',
      signupEndAt: '2026-03-18T18:00:00',
      startAt: '2026-03-19T09:00:00',
      endAt: '2026-03-20T18:00:00',
      quota: 120,
      status: 'PUBLISHED',
      participantType: 'TEACHER_ONLY',
      advisorTeacherId: null
    })
    await updateCompetitionFeature(9, {
      recommended: true,
      pinned: true
    })
    const offline = await offlineCompetition(9)

    expect(fetchMock).toHaveBeenNthCalledWith(1, '/api/admin/competitions/9', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        organizerId: 1001,
        title: '校园未来设计赛',
        description: '更新后的比赛描述',
        signupStartAt: '2026-03-17T09:00:00',
        signupEndAt: '2026-03-18T18:00:00',
        startAt: '2026-03-19T09:00:00',
        endAt: '2026-03-20T18:00:00',
        quota: 120,
        status: 'PUBLISHED',
        participantType: 'TEACHER_ONLY',
        advisorTeacherId: null
      })
    })
    expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/admin/competitions/9/feature', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        recommended: true,
        pinned: true
      })
    })
    expect(fetchMock).toHaveBeenNthCalledWith(3, '/api/admin/competitions/9/offline', {
      method: 'POST'
    })
    expect(offline).toBe(true)
  })
})
