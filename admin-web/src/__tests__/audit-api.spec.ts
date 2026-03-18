import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { fetchAuditRules, fetchViolationRecords } from '@/api/audit'

describe('audit api', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn())
  })

  afterEach(() => {
    vi.unstubAllGlobals()
    vi.restoreAllMocks()
  })

  it('should request audit rules and violation records', async () => {
    const fetchMock = vi.mocked(fetch)
    fetchMock
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          code: 0,
          message: 'ok',
          data: {
            sensitiveWords: ['作弊', '刷分'],
            allowedSubmissionExtensions: ['pdf', 'docx'],
            blockedSubmissionExtensions: ['exe', 'bat']
          }
        })
      } as Response)
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          code: 0,
          message: 'ok',
          data: [
            {
              id: 11,
              scene: 'MESSAGE',
              bizId: 8,
              userId: 2001,
              userName: '张同学',
              reason: '消息内容包含敏感词',
              hitWords: ['刷分'],
              contentSnippet: '这里有刷分攻略，快看一下。',
              createdAt: '2026-03-17T22:00:00'
            }
          ]
        })
      } as Response)

    const rules = await fetchAuditRules()
    const records = await fetchViolationRecords()

    expect(fetchMock).toHaveBeenNthCalledWith(1, '/api/admin/audit/rules', { method: 'GET' })
    expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/admin/audit/violations', { method: 'GET' })
    expect(rules.sensitiveWords).toContain('作弊')
    expect(records[0]?.scene).toBe('MESSAGE')
  })
})
