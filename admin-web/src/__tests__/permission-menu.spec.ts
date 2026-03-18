import { describe, expect, it } from 'vitest'
import { buildMenus } from '../utils/menus'

describe('permission menus', () => {
  it('admin should see user and campus menus', () => {
    const menus = buildMenus(['USER_MANAGE', 'CAMPUS_MANAGE'])
    expect(menus).toContain('users')
    expect(menus).toContain('campuses')
  })
})
