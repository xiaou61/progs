import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import {
  fetchCompetitionRegistrations,
  manualAddRegistration,
  markRegistrationAttendance,
  reviewRegistrationCheckin,
  rejectRegistration
} from '@/api/registration-manage'

describe('registration manage api', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn())
  })

  afterEach(() => {
    vi.unstubAllGlobals()
    vi.restoreAllMocks()
  })

  it('should fetch competition registrations and manually add a registration', async () => {
    const fetchMock = vi.mocked(fetch)
    fetchMock
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          code: 0,
          message: 'ok',
          data: [
            {
              id: 41,
              competitionId: 9,
              userId: 2001,
              status: 'REGISTERED',
              attendanceStatus: 'PENDING',
              remark: null,
              checkinStatus: 'PENDING',
              checkinMethod: 'QRCODE',
              checkinSubmittedAt: '2026-03-21T09:30:00',
              checkinReviewedAt: null,
              checkinReviewRemark: null
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
            registrationId: 42
          }
        })
      } as Response)

    const registrations = await fetchCompetitionRegistrations(9)
    const registrationId = await manualAddRegistration({
      competitionId: 9,
      userId: 2002,
      remark: '后台补录'
    })

    expect(fetchMock).toHaveBeenNthCalledWith(1, '/api/admin/registrations/competition/9', {
      method: 'GET'
    })
    expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/admin/registrations/manual', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        competitionId: 9,
        userId: 2002,
        remark: '后台补录'
      })
    })
    expect(registrations[0]?.status).toBe('REGISTERED')
    expect(registrationId).toBe(42)
  })

  it('should reject registration, review checkin and mark attendance', async () => {
    const fetchMock = vi.mocked(fetch)
    fetchMock
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          code: 0,
          message: 'ok',
          data: {
            rejected: true
          }
        })
      } as Response)
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          code: 0,
          message: 'ok',
          data: {
            reviewed: true
          }
        })
      } as Response)
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          code: 0,
          message: 'ok',
          data: {
            marked: true
          }
        })
      } as Response)

    const rejected = await rejectRegistration(41, '资料不完整')
    const reviewed = await reviewRegistrationCheckin(41, 'APPROVED')
    const marked = await markRegistrationAttendance(42, 'PRESENT')

    expect(fetchMock).toHaveBeenNthCalledWith(1, '/api/admin/registrations/41/reject', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        reason: '资料不完整'
      })
    })
    expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/admin/registrations/41/checkin-review', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        status: 'APPROVED'
      })
    })
    expect(fetchMock).toHaveBeenNthCalledWith(3, '/api/admin/registrations/42/attendance', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        attendanceStatus: 'PRESENT'
      })
    })
    expect(rejected).toBe(true)
    expect(reviewed).toBe(true)
    expect(marked).toBe(true)
  })
})
