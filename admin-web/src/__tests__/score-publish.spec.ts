import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { publishCompetitionResult, submitReviewTask } from '@/api/review'
import { validatePublishResultForm } from '@/utils/score-publish-form'

describe('score publish', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn())
  })

  afterEach(() => {
    vi.unstubAllGlobals()
    vi.restoreAllMocks()
  })

  it('should submit review and publish result to backend api', async () => {
    const fetchMock = vi.mocked(fetch)
    fetchMock
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
            scoreId: 13
          }
        })
      } as Response)

    const reviewed = await submitReviewTask({
      competitionId: 8,
      submissionId: 21,
      studentId: 2001,
      reviewerName: '王老师',
      reviewComment: '结构完整，建议补充落地数据',
      suggestedScore: 94
    })

    const scoreId = await publishCompetitionResult({
      competitionId: 8,
      studentId: 2001,
      score: 96,
      rank: 1,
      awardName: '一等奖',
      points: 30,
      reviewerName: '王老师',
      reviewComment: '结构完整，建议补充落地数据'
    })

    expect(fetchMock).toHaveBeenNthCalledWith(1, '/api/admin/reviews/submit', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        competitionId: 8,
        submissionId: 21,
        studentId: 2001,
        reviewerName: '王老师',
        reviewComment: '结构完整，建议补充落地数据',
        suggestedScore: 94
      })
    })
    expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/admin/scores/publish', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        competitionId: 8,
        studentId: 2001,
        score: 96,
        rank: 1,
        awardName: '一等奖',
        points: 30,
        reviewerName: '王老师',
        reviewComment: '结构完整，建议补充落地数据'
      })
    })
    expect(reviewed).toBe(true)
    expect(scoreId).toBe(13)
  })

  it('should validate publish result form', () => {
    expect(validatePublishResultForm({
      competitionId: 8,
      studentId: 0,
      score: 96,
      rank: 1,
      awardName: '一等奖',
      points: 30
    })).toBe('学生编号不能为空')
  })
})
