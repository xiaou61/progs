import { describe, expect, it } from 'vitest'

describe('admin shell menus', () => {
  it('groups routes into left navigation sections', async () => {
    const { ADMIN_MENU_SECTIONS, resolveMenuTitleByPath } = await import('../utils/admin-shell')

    expect(ADMIN_MENU_SECTIONS.some((section) => section.items.some((item) => item.path === '/'))).toBe(true)
    expect(ADMIN_MENU_SECTIONS.some((section) => section.items.some((item) => item.path === '/competition/editor'))).toBe(true)
    expect(ADMIN_MENU_SECTIONS.some((section) => section.items.some((item) => item.path === '/system/banner'))).toBe(true)
    expect(resolveMenuTitleByPath('/system/config')).toBe('系统配置')
  })
})
