import { describe, expect, it } from 'vitest'
import { buildAdminNavigation, canAccessAdminRoute } from '@/utils/admin-permissions'

describe('admin permissions', () => {
  it('should filter admin navigation by permission codes', () => {
    const navigation = buildAdminNavigation(['COMPETITION_MANAGE', 'LOG_VIEW'])

    expect(navigation.some((item) => item.path === '/competition/editor')).toBe(true)
    expect(navigation.some((item) => item.path === '/system/logs')).toBe(true)
    expect(navigation.some((item) => item.path === '/system/users')).toBe(false)
  })

  it('should determine route accessibility by permission codes', () => {
    expect(canAccessAdminRoute('/system/users', ['USER_MANAGE'])).toBe(true)
    expect(canAccessAdminRoute('/system/users', ['LOG_VIEW'])).toBe(false)
    expect(canAccessAdminRoute('/', [])).toBe(true)
  })
})
