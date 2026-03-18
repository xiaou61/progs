import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { fetchCampuses } from '@/api/campuses'
import {
  assignUserRole,
  clearUserViolation,
  createUser,
  fetchUsers,
  fetchUserLogs,
  freezeUser,
  markUserViolation,
  resetUserPassword,
  unfreezeUser,
  exportUserLogsCsv
} from '@/api/users'
import { createRole, fetchRoles, updateRole } from '@/api/roles'
import { fetchCompetitionScores, fetchReviewTasks } from '@/api/review'
import { fetchBanners, fetchSystemConfig, updateBanner, updateSystemConfig } from '@/api/system'
import { createCampus, updateCampus } from '@/api/campuses'

describe('admin api clients', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn())
  })

  afterEach(() => {
    vi.unstubAllGlobals()
    vi.restoreAllMocks()
  })

  it('should request users from backend api', async () => {
    const fetchMock = vi.mocked(fetch)
    fetchMock.mockResolvedValue({
      ok: true,
      json: async () => ({
        code: 0,
        message: 'ok',
        data: [
          {
            id: 1,
            studentNo: '20260001',
            realName: '张三',
            phone: '13800000000',
            roleCode: 'STUDENT',
            status: 'ENABLED'
          }
        ]
      })
    } as Response)

    const users = await fetchUsers()

    expect(fetchMock).toHaveBeenCalledWith('/api/admin/users', { method: 'GET' })
    expect(users[0]?.realName).toBe('张三')
  })

  it('should request campuses from backend api', async () => {
    const fetchMock = vi.mocked(fetch)
    fetchMock.mockResolvedValue({
      ok: true,
      json: async () => ({
        code: 0,
        message: 'ok',
        data: [{ id: 1, campusCode: 'MAIN', campusName: '主校区', status: 'ENABLED' }]
      })
    } as Response)

    const campuses = await fetchCampuses()

    expect(fetchMock).toHaveBeenCalledWith('/api/admin/campuses', { method: 'GET' })
    expect(campuses[0]?.campusCode).toBe('MAIN')
  })

  it('should create users through backend api', async () => {
    const fetchMock = vi.mocked(fetch)
    fetchMock.mockResolvedValue({
      ok: true,
      json: async () => ({
        code: 0,
        message: 'ok',
        data: {
          id: 6,
          studentNo: 'S20260101',
          realName: '新建学生',
          phone: '13800000101',
          roleCode: 'STUDENT',
          status: 'ENABLED',
          violationMarked: false,
          violationReason: null
        }
      })
    } as Response)

    const created = await createUser({
      studentNo: 'S20260101',
      realName: '新建学生',
      phone: '13800000101',
      roleCode: 'STUDENT',
      password: 'Abcd5678'
    })

    expect(fetchMock).toHaveBeenCalledWith('/api/admin/users', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        studentNo: 'S20260101',
        realName: '新建学生',
        phone: '13800000101',
        roleCode: 'STUDENT',
        password: 'Abcd5678'
      })
    })
    expect(created.studentNo).toBe('S20260101')
    expect(created.violationMarked).toBe(false)
  })

  it('should create or update campuses through backend api', async () => {
    const fetchMock = vi.mocked(fetch)
    fetchMock
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          code: 0,
          message: 'ok',
          data: { id: 3, campusCode: 'WEST', campusName: '西校区', status: 'ENABLED' }
        })
      } as Response)
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          code: 0,
          message: 'ok',
          data: { id: 3, campusCode: 'WEST', campusName: '西校区新馆', status: 'DISABLED' }
        })
      } as Response)

    const created = await createCampus({
      campusCode: 'WEST',
      campusName: '西校区',
      status: 'ENABLED'
    })
    const updated = await updateCampus(3, {
      campusCode: 'WEST',
      campusName: '西校区新馆',
      status: 'DISABLED'
    })

    expect(fetchMock).toHaveBeenNthCalledWith(1, '/api/admin/campuses', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        campusCode: 'WEST',
        campusName: '西校区',
        status: 'ENABLED'
      })
    })
    expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/admin/campuses/3', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        campusCode: 'WEST',
        campusName: '西校区新馆',
        status: 'DISABLED'
      })
    })
    expect(created.campusCode).toBe('WEST')
    expect(updated.status).toBe('DISABLED')
  })

  it('should request banners and system config from backend api', async () => {
    const fetchMock = vi.mocked(fetch)
    fetchMock
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          code: 0,
          message: 'ok',
          data: [{ id: 1, title: '春季比赛季主视觉', status: 'ENABLED', jumpPath: '/pages/competition/list/index' }]
        })
      } as Response)
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          code: 0,
          message: 'ok',
          data: {
            platformName: '校园师生比赛管理平台',
            mvpPhase: 'Phase 1',
            pointsEnabled: true,
            submissionReuploadEnabled: true
          }
        })
      } as Response)

    const banners = await fetchBanners()
    const config = await fetchSystemConfig()

    expect(fetchMock).toHaveBeenNthCalledWith(1, '/api/admin/banners', { method: 'GET' })
    expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/admin/configs', { method: 'GET' })
    expect(banners[0]?.jumpPath).toBe('/pages/competition/list/index')
    expect(config.platformName).toBe('校园师生比赛管理平台')
  })

  it('should update banners and system config through backend api', async () => {
    const fetchMock = vi.mocked(fetch)
    fetchMock
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          code: 0,
          message: 'ok',
          data: {
            id: 1,
            title: '春季比赛季主视觉-更新',
            status: 'DISABLED',
            jumpPath: '/pages/home/index'
          }
        })
      } as Response)
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          code: 0,
          message: 'ok',
          data: {
            platformName: '校园师生比赛管理平台 Pro',
            mvpPhase: 'Phase 2',
            pointsEnabled: false,
            submissionReuploadEnabled: false
          }
        })
      } as Response)

    const banner = await updateBanner(1, {
      title: '春季比赛季主视觉-更新',
      status: 'DISABLED',
      jumpPath: '/pages/home/index'
    })
    const config = await updateSystemConfig({
      platformName: '校园师生比赛管理平台 Pro',
      mvpPhase: 'Phase 2',
      pointsEnabled: false,
      submissionReuploadEnabled: false
    })

    expect(fetchMock).toHaveBeenNthCalledWith(1, '/api/admin/banners/1', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        title: '春季比赛季主视觉-更新',
        status: 'DISABLED',
        jumpPath: '/pages/home/index'
      })
    })
    expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/admin/configs', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        platformName: '校园师生比赛管理平台 Pro',
        mvpPhase: 'Phase 2',
        pointsEnabled: false,
        submissionReuploadEnabled: false
      })
    })
    expect(banner.status).toBe('DISABLED')
    expect(config.pointsEnabled).toBe(false)
  })

  it('should request review tasks and scores by competition id', async () => {
    const fetchMock = vi.mocked(fetch)
    fetchMock
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          code: 0,
          message: 'ok',
          data: [
            {
              competitionId: 8,
              submissionId: 12,
              studentId: 2001,
              reviewerName: '默认评委组',
              status: 'PENDING',
              reviewComment: null,
              suggestedScore: null,
              reviewedAt: null
            }
          ]
        })
      } as Response)
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          code: 0,
          message: 'ok',
          data: [
            {
              id: 3,
              competitionId: 8,
              studentId: 2001,
              score: 96,
              rank: 1,
              awardName: '一等奖',
              points: 30,
              publishedAt: '2026-03-17T16:00:00',
              reviewerName: '王老师',
              reviewComment: '方案表达完整',
              certificateNo: 'CERT-8-2001-1',
              certificateTitle: '校园创新挑战赛电子奖状'
            }
          ]
        })
      } as Response)

    const tasks = await fetchReviewTasks(8)
    const scores = await fetchCompetitionScores(8)

    expect(fetchMock).toHaveBeenNthCalledWith(1, '/api/admin/reviews/tasks?competitionId=8', { method: 'GET' })
    expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/admin/scores/competition/8', { method: 'GET' })
    expect(tasks[0]?.submissionId).toBe(12)
    expect(tasks[0]?.reviewComment).toBeNull()
    expect(scores[0]?.points).toBe(30)
    expect(scores[0]?.certificateNo).toBe('CERT-8-2001-1')
  })

  it('should request role list and create role to backend api', async () => {
    const fetchMock = vi.mocked(fetch)
    fetchMock
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          code: 0,
          message: 'ok',
          data: [
            {
              id: 3,
              roleCode: 'ADMIN',
              roleName: '管理员',
              description: '系统管理员',
              permissionCodes: ['USER_MANAGE', 'ROLE_MANAGE'],
              builtIn: true,
              status: 'ENABLED',
              userCount: 1
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
            id: 4,
            roleCode: 'JUDGE',
            roleName: '评委',
            description: '负责评分与结果发布',
            permissionCodes: ['REVIEW_MANAGE', 'SCORE_PUBLISH'],
            builtIn: false,
            status: 'ENABLED',
            userCount: 0
          }
        })
      } as Response)

    const roles = await fetchRoles()
    const created = await createRole({
      roleCode: 'JUDGE',
      roleName: '评委',
      description: '负责评分与结果发布',
      permissionCodes: ['REVIEW_MANAGE', 'SCORE_PUBLISH']
    })

    expect(fetchMock).toHaveBeenNthCalledWith(1, '/api/admin/roles', { method: 'GET' })
    expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/admin/roles', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        roleCode: 'JUDGE',
        roleName: '评委',
        description: '负责评分与结果发布',
        permissionCodes: ['REVIEW_MANAGE', 'SCORE_PUBLISH']
      })
    })
    expect(roles[0]?.roleCode).toBe('ADMIN')
    expect(created.roleCode).toBe('JUDGE')
  })

  it('should update role and execute user governance actions', async () => {
    const fetchMock = vi.mocked(fetch)
    fetchMock
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          code: 0,
          message: 'ok',
          data: {
            id: 4,
            roleCode: 'JUDGE',
            roleName: '赛事评委',
            description: '负责评审与结果发布',
            permissionCodes: ['REVIEW_MANAGE', 'SCORE_PUBLISH', 'RESULT_VIEW'],
            builtIn: false,
            status: 'ENABLED',
            userCount: 1
          }
        })
      } as Response)
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          code: 0,
          message: 'ok',
          data: {
            updated: true
          }
        })
      } as Response)
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          code: 0,
          message: 'ok',
          data: {
            updated: true
          }
        })
      } as Response)
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          code: 0,
          message: 'ok',
          data: {
            updated: true
          }
        })
      } as Response)
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          code: 0,
          message: 'ok',
          data: {
            id: 2,
            studentNo: 'S20260001',
            realName: '演示学生',
            phone: '13800000001',
            roleCode: 'JUDGE',
            status: 'ENABLED'
          }
        })
      } as Response)

    const updatedRole = await updateRole('JUDGE', {
      roleName: '赛事评委',
      description: '负责评审与结果发布',
      permissionCodes: ['REVIEW_MANAGE', 'SCORE_PUBLISH', 'RESULT_VIEW']
    })
    const freezeResult = await freezeUser(2, '测试冻结账号')
    const unfreezeResult = await unfreezeUser(2)
    const resetResult = await resetUserPassword(2, 'Abcd5678')
    const assignedUser = await assignUserRole(2, 'JUDGE')

    expect(fetchMock).toHaveBeenNthCalledWith(1, '/api/admin/roles/JUDGE', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        roleName: '赛事评委',
        description: '负责评审与结果发布',
        permissionCodes: ['REVIEW_MANAGE', 'SCORE_PUBLISH', 'RESULT_VIEW']
      })
    })
    expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/admin/users/2/freeze', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        reason: '测试冻结账号'
      })
    })
    expect(fetchMock).toHaveBeenNthCalledWith(3, '/api/admin/users/2/unfreeze', {
      method: 'POST'
    })
    expect(fetchMock).toHaveBeenNthCalledWith(4, '/api/admin/users/2/reset-password', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        newPassword: 'Abcd5678'
      })
    })
    expect(fetchMock).toHaveBeenNthCalledWith(5, '/api/admin/users/2/role', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        roleCode: 'JUDGE'
      })
    })
    expect(updatedRole.permissionCodes).toContain('RESULT_VIEW')
    expect(freezeResult).toBe(true)
    expect(unfreezeResult).toBe(true)
    expect(resetResult).toBe(true)
    expect(assignedUser.roleCode).toBe('JUDGE')
  })

  it('should request violation governance and log export from backend api', async () => {
    const fetchMock = vi.mocked(fetch)
    fetchMock
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          code: 0,
          message: 'ok',
          data: { updated: true }
        })
      } as Response)
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          code: 0,
          message: 'ok',
          data: { updated: true }
        })
      } as Response)
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          code: 0,
          message: 'ok',
          data: [
            {
              id: 9,
              operatorName: '系统管理员',
              action: 'VIOLATION_MARK',
              target: 'S20260001',
              detail: '多次刷分咨询，进入人工复核',
              createdAt: '2026-03-17T22:10:00'
            }
          ]
        })
      } as Response)
      .mockResolvedValueOnce({
        ok: true,
        text: async () => 'operatorName,action,target,detail,createdAt\n系统管理员,VIOLATION_MARK,S20260001,多次刷分咨询，进入人工复核,2026-03-17 22:10:00'
      } as Response)

    const marked = await markUserViolation(2, '多次刷分咨询，进入人工复核')
    const cleared = await clearUserViolation(2, '人工复核完成，解除违规标记')
    const logs = await fetchUserLogs({ action: 'VIOLATION_MARK' })
    const csv = await exportUserLogsCsv({ action: 'VIOLATION_MARK' })

    expect(fetchMock).toHaveBeenNthCalledWith(1, '/api/admin/users/2/violation', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        violating: true,
        reason: '多次刷分咨询，进入人工复核'
      })
    })
    expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/admin/users/2/violation', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        violating: false,
        reason: '人工复核完成，解除违规标记'
      })
    })
    expect(fetchMock).toHaveBeenNthCalledWith(3, '/api/admin/logs?action=VIOLATION_MARK', {
      method: 'GET'
    })
    expect(fetchMock).toHaveBeenNthCalledWith(4, '/api/admin/logs/export?action=VIOLATION_MARK', {
      method: 'GET'
    })
    expect(marked).toBe(true)
    expect(cleared).toBe(true)
    expect(logs[0]?.action).toBe('VIOLATION_MARK')
    expect(csv).toContain('VIOLATION_MARK')
  })
})
