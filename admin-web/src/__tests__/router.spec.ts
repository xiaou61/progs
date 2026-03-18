import { describe, expect, it } from 'vitest'
import { routes } from '../router'

describe('admin routes', () => {
  it('contains login route and authenticated layout children', () => {
    expect(routes.some((item) => item.path === '/login')).toBe(true)

    const layoutRoute = routes.find((item) => item.path === '/')
    expect(layoutRoute).toBeTruthy()
    expect(layoutRoute?.meta?.requiresAuth).toBe(true)
    expect(layoutRoute?.children?.some((item) => item.path === '')).toBe(true)
    expect(layoutRoute?.children?.some((item) => item.path === 'competition/editor')).toBe(true)
    expect(layoutRoute?.children?.some((item) => item.path === 'competition/registrations')).toBe(true)
    expect(layoutRoute?.children?.some((item) => item.path === 'system/audit')).toBe(true)
    expect(layoutRoute?.children?.some((item) => item.path === 'system/logs')).toBe(true)
  })
})
