import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { publishCompetition } from '@/api/competition'
import { buildPublishPayload, validateCompetitionForm } from '@/utils/competition-form'

describe('competition form', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn())
  })

  afterEach(() => {
    vi.unstubAllGlobals()
    vi.restoreAllMocks()
  })

  it('should publish competition to backend api', async () => {
    const fetchMock = vi.mocked(fetch)
    fetchMock.mockResolvedValue({
      ok: true,
      json: async () => ({
        code: 0,
        message: 'ok',
        data: {
          competitionId: 8
        }
      })
    } as Response)

    const form = {
      organizerId: 1001,
      title: '校园创新挑战赛',
      description: '围绕校园问题进行创新设计',
      signupStartAt: '2026-03-18T09:00',
      signupEndAt: '2026-03-20T18:00',
      startAt: '2026-03-21T09:00',
      endAt: '2026-03-21T18:00',
      quota: 200,
      participantType: 'STUDENT_ONLY' as const,
      advisorTeacherId: 2001
    }

    const competitionId = await publishCompetition(buildPublishPayload(form, {
      recommended: true,
      pinned: true
    }))

    expect(fetchMock).toHaveBeenCalledWith('/api/admin/competitions', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        organizerId: 1001,
        title: '校园创新挑战赛',
        description: '围绕校园问题进行创新设计',
        signupStartAt: '2026-03-18T09:00:00',
        signupEndAt: '2026-03-20T18:00:00',
        startAt: '2026-03-21T09:00:00',
        endAt: '2026-03-21T18:00:00',
        quota: 200,
        participantType: 'STUDENT_ONLY',
        advisorTeacherId: 2001,
        recommended: true,
        pinned: true
      })
    })
    expect(competitionId).toBe(8)
  })

  it('should validate publish form before submit', () => {
    expect(validateCompetitionForm({
      organizerId: 1001,
      title: '',
      description: 'desc',
      signupStartAt: '2026-03-20T09:00',
      signupEndAt: '2026-03-18T18:00',
      startAt: '2026-03-21T09:00',
      endAt: '2026-03-21T08:00',
      quota: 0,
      participantType: 'STUDENT_ONLY',
      advisorTeacherId: 0
    })).toBe('比赛名称不能为空')
  })
})
